package com.example.nfc_eink_demo;

//import java.io.File;
//import java.io.FileOutputStream;
//import java.nio.ShortBuffer;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;


public class ImageManipulation {
	


	/**
	 * Rotate the provided bitmap by the given number of degrees (in CW or CCW direction?)
	 * @param src The original bitmap
	 * @param degree The number of degrees by which to rotate
	 * @return A rotated version of the original bitmap
	 */
	public static Bitmap rotate(Bitmap src, float degree) {

		// Create new matrix
		Matrix matrix = new Matrix();
		// setup rotation degree
		matrix.postRotate(degree);

		// return the new bitmap
		return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(),
				matrix, true);
	}// end rotate

	/**
	 * Scale a bitmap to the given height and width
	 * @param src The original bitmap
	 * @param newHeight The new height
	 * @param newWidth The new width
	 * @return The scaled bitmap
	 */
	public static Bitmap getResizedBitmap(Bitmap src, int newHeight, int newWidth) {
		int width = src.getWidth();
		int height = src.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// CREATE A MATRIX FOR THE MANIPULATION
		Matrix matrix = new Matrix();
		// RESIZE THE BIT MAP
		matrix.postScale(scaleWidth, scaleHeight);

		// Generate scaled bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(src, 0, 0, width, height,
				matrix, false);
		
		return resizedBitmap;
	}

	/**
	 * Create a black and white version of the given bitmap
	 * @param src The original bitmap
	 * @return Black and white bitmap
	 */
	public static Bitmap createBlackAndWhite(Bitmap src) {
		int width = src.getWidth();
		int height = src.getHeight();
		// create output bitmap
		Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
		// color information
		int A, R, G, B;
		int pixel;

		// scan through all pixels
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				// get pixel color
				pixel = src.getPixel(x, y);
				A = Color.alpha(pixel);
				R = Color.red(pixel);
				G = Color.green(pixel);
				B = Color.blue(pixel);
				
				int gray = (int) (0.2989 * R + 0.5870 * G + 0.1140 * B);

				// use 128 as threshold, above -> white, below -> black
				if (gray > 128)
					gray = 255;
				else
					gray = 0;
				// set new pixel color to output bitmap
				bmOut.setPixel(x, y, Color.argb(A, gray, gray, gray));
			}
		}
		return bmOut;
	}// end create black and white

	
//	static String saveBitmap(Bitmap bitmap, String dir, String baseName) {
//	try {
//		// File sdcard = Environment.getExternalStorageDirectory();
//		File pictureDir = new File("/sdcard/hello");
//		pictureDir.mkdirs();
//		File f = null;
//		for (int i = 1; i < 200; ++i) {
//			String name = baseName + i + ".png";
//			f = new File(pictureDir, name);
//			if (!f.exists()) {
//				break;
//			}
//		}
//		if (!f.exists()) {
//			String name = f.getAbsolutePath();
//			FileOutputStream fos = new FileOutputStream(name);
//			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
//			fos.flush();
//			fos.close();
//			return name;
//		}
//	} catch (Exception e) {
//	} finally {
//		/*
//		 * if (fos != null) { fos.close(); }
//		 */
//	}
//	return null;
//}
//
//static void bitmapBGRtoRGB(Bitmap bitmap, int width, int height) {
//	int size = width * height;
//	short data[] = new short[size];
//	ShortBuffer buf = ShortBuffer.wrap(data);
//	bitmap.copyPixelsToBuffer(buf);
//	for (int i = 0; i < size; ++i) {
//		// BGR-565 to RGB-565
//		short v = data[i];
//		data[i] = (short) (((v & 0x1f) << 11) | (v & 0x7e0) | ((v & 0xf800) >> 11));
//	}
//	buf.rewind();
//	bitmap.copyPixelsFromBuffer(buf);
//}
	
};