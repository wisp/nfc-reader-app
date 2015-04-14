/**
 * 
 * 	Modified the application, so it doesn't get reopened each time tag is connected
 * The screenshot conversion verified to work
 * 
 * the folder where screenshots are saved might change. (currently path is hardcoded for sdcard/screenshots/) 
 * 
 * This application doesn't take screenshots itself. It uses third party application: 
 * screenshot (by KasterSoft). Set the image to bmp format (no compression) 
 * It is possible to extract images from the frame buffer on rooted phone but I found it problematic. 
 * 
 * Tested on Galaxy Nexus (Samsung) and Nexus S (Samsung)
 */

package com.example.nfc_eink_demo;

///////////////////////////////////////////////////////////////////////////////
// IMPORTS
///////////////////////////////////////////////////////////////////////////////

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

	///////////////////////////////////////////////////////////////////////////////
	//CONSTANTS
	///////////////////////////////////////////////////////////////////////////////
	private static final String TAG = "NFCWriteTag"; // Used in debugging?


	///////////////////////////////////////////////////////////////////////////////
	// FIELDS
	///////////////////////////////////////////////////////////////////////////////


	public TextView myStatusTextView;
	public View progressView;
	public ProgressBar pbCircular;
	public IsoDep isoDep;
	
	private NfcAdapter mNfcAdapter; // Represents phone's NFC transceiver
	private IntentFilter[] mWriteTagFilters; 
	private PendingIntent mNfcPendingIntent; 
	private Context context;
	private ImageView mImage; 
	private int imageIdx;
	private byte[] imageBuffer; // Global for storing image data to relay to tag
	private PowerManager mPowerManager;
	private WakeLock mWakeLock;
	private SendFileToWISP fileSender;
	private Spinner spinner;
	private Spinner sizeSpinner;
	private File currentImage;
	private int height;
	private int width;
	
	private OnItemSelectedListener itemSelectedListener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			//currentImage = new File("/sdcard/screenshots/").listFiles()[(int)id];
			currentImage = new File("/sdcard/DCIM/Camera/").listFiles()[(int)id];
			retrieveScreenShot();
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private OnItemSelectedListener sizeSelectedListener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			if (id == 0) {
				height = 96;
				width = 200;
			} else {
				height = 176;
				width = 264;
			}
			retrieveScreenShot();
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub
			
		}
	};
	

	
	///////////////////////////////////////////////////////////////////////////////
	// METHODS
	///////////////////////////////////////////////////////////////////////////////

	/**
	 * 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		addImagesToSpinner();
		addSizesToSpinner();
		spinner.setOnItemSelectedListener(itemSelectedListener);
		sizeSpinner.setOnItemSelectedListener(sizeSelectedListener);
		context = getApplicationContext();
		mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, getClass().getName());
		myStatusTextView = (TextView) findViewById(R.id.myStatusTextView);
		progressView = findViewById(R.id.progressPane);
		progressView.setVisibility(View.INVISIBLE);
		pbCircular = (ProgressBar) progressView.findViewById(R.id.circularProgressBar);
		pbCircular.setVisibility(View.INVISIBLE);
		mImage = (ImageView) findViewById(R.id.imageView1);
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
						| Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);
		IntentFilter discovery = new IntentFilter(
				NfcAdapter.ACTION_TAG_DISCOVERED);
		fileSender = new SendFileToWISP(this);
		// Intent filters for writing to a tag
		mWriteTagFilters = new IntentFilter[] { discovery };
		
		retrieveScreenShot();
	}
	
	private void addSizesToSpinner() {
		sizeSpinner = (Spinner) findViewById(R.id.sizeSpinner);
		List<String> list = new ArrayList<String>();
        String large = "176 x 264";
		String small = "96 x 200";
        list.add(small);
        list.add(large);
		height = 176; // Set defaults
		width = 264;
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sizeSpinner.setAdapter(dataAdapter);
	}

	private void addImagesToSpinner() {
		spinner = (Spinner) findViewById(R.id.imageSpinner);
		List<String> list = new ArrayList<String>();
		
		File dir = new File("/sdcard/DCIM/Camera");
		File f = new File("/sdcard/DCIM/Camera/info.txt");
		f.delete();
		File g = new File("/sdcard/DCIM/Camera/config.dat");
		g.delete();

		// Find the last modified file, presumably with the latest image
		File[] files = dir.listFiles();
		if (files.length == 0) {
			Log.d("TEST", "No files");
		}
		
		currentImage = files[0]; // Set default image to first file in folder
		
		// Fill spinner with files dynamically
		for (int i = 0; i < files.length; i++) {
			list.add(files[i].getName());
		}
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(dataAdapter);
	}

	/**
	 * 
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * 
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		mWakeLock.acquire(); // Keep the phone awake while app is active

		if (mNfcAdapter != null) {
			if (!mNfcAdapter.isEnabled()) {
				Intent setnfc = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
				startActivity(setnfc);
			}
			mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
					mWriteTagFilters, null);
		} else {
			Toast.makeText(context, "Sorry, No NFC Adapter found.",
					Toast.LENGTH_SHORT).show();
		}// end if

	}// on resume end

	/**
	 * 
	 */
	@Override
	protected void onPause() {
		super.onPause();
		
		mWakeLock.release(); // Let the phone go to sleep
		
		if (mNfcAdapter != null)
			mNfcAdapter.disableForegroundDispatch(this);
	}

	/**
	 * 
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.d(TAG, "onNewIntent Called");
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			// validate that this tag can be written....
			myStatusTextView.setText("tag detected");
			pbCircular.setVisibility(View.VISIBLE);
			Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			Log.d(TAG, detectedTag.toString());
			Log.d(TAG, detectedTag.getTechList().toString());

			this.isoDep = IsoDep.get(detectedTag);

			// Have the background task send the image
			fileSender.execute(imageBuffer);
			fileSender = new SendFileToWISP(this);

		}// end if

	}// end onNewIntent

	/**
	 * Convert screenshot or image to the eink format
	 * 
	 * @todo optimize this function
	 */
	public void retrieveScreenShot() {

		// Find the latest file
		// Delete the info.txt file and config.dat to avoid

		Bitmap image;
		Bitmap image_rotated;
		Bitmap image_rotated_resized;
		Bitmap image_rotated_resized_monochrome;
        Bitmap image_rotated_resized_monochrome_display;

		image = BitmapFactory.decodeFile(currentImage.toString());
		image_rotated = ImageManipulation.rotate(image, 90);
		image_rotated_resized = ImageManipulation.getResizedBitmap(
				image_rotated, height, width);
//		image_rotated_resized = ImageManipulation.getResizedBitmap(
//				image_rotated, 96, 200);

		image_rotated_resized_monochrome = ImageManipulation
				.createBlackAndWhite(image_rotated_resized,0);

        image_rotated_resized_monochrome_display = ImageManipulation
                .createBlackAndWhite(image_rotated_resized,1);
		// Update the screen with the new image.
		mImage.setImageBitmap(ImageManipulation.rotate(
				image_rotated_resized_monochrome_display, 270));

		int bytes = image_rotated_resized_monochrome.getRowBytes()
				* image_rotated_resized_monochrome.getHeight();

		ByteBuffer buffer = ByteBuffer.allocate(bytes);
		image_rotated_resized_monochrome.copyPixelsToBuffer(buffer);
		byte[] array = buffer.array();
		String foo = "";

		String test = "";
		int count = 0;
		int count_bytes = 0;
		byte[] image_array = new byte[image_rotated_resized_monochrome.getHeight() *
		                              image_rotated_resized_monochrome.getWidth() / 8];
//		byte[] image_array = new byte[176 * 264 / 8];
		// TODO Rewrite the following more efficiently (Without string conversion and int parsing, etc)

          //image_array = buffer.array();
        //* comment it out since we implement it on the tag
		// For each line in the image
		for (int m = 0; m < image_rotated_resized_monochrome.getHeight(); m++) {

			// For each pixel of each line
			for (int i = 0; i < image_rotated_resized_monochrome.getWidth(); i++) {

				if (image_rotated_resized_monochrome.getPixel(i, m) == -1) {
					test = test.concat("0");

				}// end if
				else {
					test = test.concat("1");
				}// end else

				count++;

				// Assemble a byte
				if (count == 8) {

					count = 0;
					//@eve: we do not need to reverse the byte
					//test = Utilities.reverse(test);

					// Log.d("TEST", "teststring: " + test);
					byte numberByte = (byte) Integer.parseInt(test, 2);
					// System.out.println(numberByte);
					image_array[count_bytes] = numberByte;
					// numberByte.toString();
					count_bytes++;
					test = "";
				}// end if

			}// end for
		}// end for
        //*/



		Log.d("TEST", Integer.toString(count_bytes));
		imageBuffer = image_array;
		Log.d("TEST", Integer.toString(foo.length()));

        foo = Utilities.getHexParsed(image_array);
		try {
			// TODO Don't use hardcoded /sdcard/ path, or any hardcoded path here!
			BufferedWriter out = new BufferedWriter(new FileWriter(
					"/sdcard/byteArrayImage.txt"));
			out.write(foo);
			out.close();
		} catch (IOException e) {
		}
		

	}// end takescreenshot

};// end class

