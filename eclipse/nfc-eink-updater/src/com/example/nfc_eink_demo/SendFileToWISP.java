package com.example.nfc_eink_demo;

///////////////////////////////////////////////////////////////////////////////
// IMPORTS
///////////////////////////////////////////////////////////////////////////////

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;

import android.os.AsyncTask;


/**
 * This task handles transfer of files to NFC-WISP. It's designed to execute in
 * the background and extends AsyncTask to allow this behavior.
 * 
 * @author anparks
 * 
 */
public class SendFileToWISP extends AsyncTask<byte[], Integer,  Boolean> {
	
	///////////////////////////////////////////////////////////////////////////////
	//CONSTANTS
	///////////////////////////////////////////////////////////////////////////////
	
	//Enumerating indices into INF buffer
	private static final int INF_FLAGS_INDEX = 			0;
	private static final int INF_CHUNK_ADX_INDEX = 		1;
	private static final int INF_DATA_START_INDEX =		2;
	
	//Flag enumeration (R->T and T->R)
	private static final byte INF_FLAG_WRITE_SUCCESS = 0x01;
	private static final byte INF_FLAG_WRITE_COMPLETE = 0x02;
	// = 0x04
	// = 0x08
	// = 0x10
	// = 0x20
	// = 0x40
	// = 0x80
	
	
	//Command IDs for "transport layer"
//	private static final int INF_CMD_ID_TX_COMPLETE =	0x01;
//	private static final int INF_CMD_ID_READ_SINGLE_BLOCK=0x20;
//	private static final int INF_CMD_ID_WRITE_SINGLE_BLOCK=0x21;
//	private static final int INF_CMD_ID_WRITE_N_BLOCKS=0x24;
	

	
	///////////////////////////////////////////////////////////////////////////////
	// FIELDS
	///////////////////////////////////////////////////////////////////////////////
	
	private MainActivity myMain;

	///////////////////////////////////////////////////////////////////////////////
	// METHODS
	///////////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor method, initialize fields from parts of main activity class
	 * @param myMain
	 */
	SendFileToWISP(MainActivity myMain) {
		this.myMain = myMain;
	}

	@Override
	protected Boolean doInBackground(byte[]... arg0) {
		
		// Execute all file transfer tasks in sequence in the background.
		for(int i = 0; i < arg0.length; i++) {
			sendImage(arg0[i]);
		}
		
		return true;
	}

	/**
	 * Transfers the image represented by global array to the tag given. Uses
	 * 14443-4 block writes.
	 * 
	 * @param myImgBuffer
	 *            A byte array to send to the tag
	 *            
	 * @return Success or failure
	 */
	private boolean sendImage(byte[] myImgBuffer) {

		try {

			// Tag needs to be connected after discovered
			this.myMain.isoDep.connect(); 

			// UI update calls have to be run on UI thread
			myMain.runOnUiThread(new Runnable() { public void run() {myMain.myStatusTextView.setText("Connected");} });

			// Set up parameters of a block write
			int blockSize = 4; // Size, in bytes, of each block
			int numBlocksPerWrite = 8; // Number of blocks to send in each write
			int chunkSizeBytes = blockSize * numBlocksPerWrite; // Number of bytes total per write
			int totalChunks = myImgBuffer.length/chunkSizeBytes; // Number of writes required. Max is 256.
			
			// Make sure we're not trying to send too much data (> 256*chunk_size bytes)
			if(totalChunks > 256) {
				myMain.runOnUiThread(new Runnable() { public void run() {myMain.myStatusTextView.setText("Oversized payload");} });
				return false;
			}

			// Initialize iterator for sending file chunks
			int curChunkIndex = 0;
			byte rtFlags = 0;
			boolean fileTransferComplete = false;
			boolean prevWriteSucceeded = false;

			// Write each block in sequence
			// TODO Implement this more robustly by adding a timeout.
			while (this.myMain.isoDep.isConnected() && !fileTransferComplete) {

				// Tell user what chunk we're about to try writing now (UI update calls have to be run on UI thread)
				final String tmpDispStr = new String("Sending Packet #: " + (curChunkIndex + 1) + "/" + totalChunks); // Build display string (must be final)
				myMain.runOnUiThread(new Runnable() { public void run() {myMain.myStatusTextView.setText(tmpDispStr);} });

				// Attempt to write the last requested chunk to the tag 
				byte[] tagResponse = writeNBlocks(this.myMain.isoDep, numBlocksPerWrite,
						curChunkIndex, myImgBuffer, rtFlags);

				// If we got a valid tag response, see what chunk the tag requests next
				if (tagResponse != null && tagResponse.length >= 2) {
					
					// See what flags are set in the tag's reply
					prevWriteSucceeded = ((tagResponse[INF_FLAGS_INDEX] & INF_FLAG_WRITE_SUCCESS) > 0);
					fileTransferComplete = ((tagResponse[INF_FLAGS_INDEX] & INF_FLAG_WRITE_COMPLETE) > 0);
					
					// Extract requested chunk ID from tag's reply
					int requestedChunk = (tagResponse[INF_CHUNK_ADX_INDEX] & 0xFF); // ([byte] & 0xFF) converts from uint properly

					// We'll send this one next (whatever the tag wants)
					curChunkIndex = requestedChunk;
					
				} // end if

			} // end while

			// Tell the user that we're done transferring the file (Must be run on UI thread)
			myMain.runOnUiThread(new Runnable() { public void run() {myMain.myStatusTextView.setText("Transfer complete");} });

		} catch (IOException e) {
			
			// Tell the user that the file transfer failed (Must be run on UI thread)
			myMain.runOnUiThread(new Runnable() { public void run() {myMain.myStatusTextView.setText("Transfer failure");} });

			return false;

		}// end catch

		return true; // Successful

	}// end sendImage

	/**
	 * Use 14443-4 block write to transfer the given number of blocks of the
	 * given source buffer to the given tag
	 * 
	 * @param isodep
	 *            Object representing IO access for a certain 14443-4 tag
	 * @param blockBuffer
	 *            the byte buffer to transmit
	 * @param index
	 *            How far we currently are indexed into that buffer
	 * @param numOfBlocks
	 *            Number of blocks to send at a time
	 * @return Success or failure
	 */
	private byte[] writeNBlocks(IsoDep isodep, int numOfBlocks, int chunkIndex,
			byte[] myImgBuffer, byte flags) {
		byte[] response;

		try {

			byte write_multi_block_command[] = new byte[(4 * numOfBlocks) + 2];
			write_multi_block_command[0] = flags;
			write_multi_block_command[1] = (byte) (chunkIndex);

			for (int curBlock = 0; curBlock < 4 * numOfBlocks; curBlock++) {
				write_multi_block_command[curBlock + INF_DATA_START_INDEX] = myImgBuffer[curBlock
						+ chunkIndex * numOfBlocks * 4];
			}

			response = isodep.transceive(write_multi_block_command);

		} catch (IOException e) {
			// Throws an error anyway, even if correctly written a block
			return null;
		}// end catch

		return response;

	}// end writeOneBlock

}