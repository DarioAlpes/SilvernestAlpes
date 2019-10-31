package com.uk.tsl.utils;

public class HexEncoding
{

	/**
	 * 
	 * @param bytes the bytes to be converted
	 * @return a hex-encoded String 
	 */
	public static String bytesToString(byte[] bytes)
	{
		StringBuilder sb = new StringBuilder();
		for(byte b : bytes)
		{
			sb.append(String.format("%02x", b & 0xff));
		}
		return sb.toString();
	}


	/**
	 * 
	 * @param s hex-encoded string to be converted
	 * @return array of bytes corresponding to the hex-encoded string s
	 */
	public static byte[] stringToBytes(String s)
	{
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2)
	    {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}

	
}
