package com.example.nfc_eink_demo;

public class Utilities {

	/**
	 * Given a byte array, return a string with a hexadecimal representation
	 * 
	 * @param byteArray
	 *            The array of bytes to be converted
	 * @return A string representing each nibble of the byte array as a hex
	 *         character
	 */
	public static String getHexParsed(byte[] byteArray) {
		String HEXMAP = "0123456789ABCDEF";
		if (byteArray == null) {
			return null;
		}
		final StringBuilder hex = new StringBuilder(2 * byteArray.length);
		for (final byte b : byteArray) {
			hex.append("0x");
			hex.append(HEXMAP.charAt((b & 0xF0) >> 4)).append(
					HEXMAP.charAt((b & 0x0F)));
			hex.append(",");
		}
		return hex.toString();
	}// end getHexParsed

	/**
	 * Returns a character-reversed version of a string
	 * 
	 * @param s
	 *            The original string
	 * @return The character-reversed string
	 */
	public static String reverse(String s) {
		String reverseStringVariable = "";
		for (int i = s.length() - 1; i != -1; i--) {
			reverseStringVariable += s.charAt(i);
		}
		return reverseStringVariable;
	}// end reverse
	
};