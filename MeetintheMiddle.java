/*
 * @filename MeetintheMiddle.java
 * @author Rishabh Sawhney
 * @date 2 Febuary 2016
 */

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import java.math.BigInteger;
import java.lang.Object;
import java.util.Arrays;
import java.io.*;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.xml.bind.DatatypeConverter;

public class MeetintheMiddle
{
	public static void main( String[] args )
	{
		try
		{
			// take in the inputs
			byte[] plaintext = DatatypeConverter.parseHexBinary(args[0]);
			byte[] ciphertext = DatatypeConverter.parseHexBinary(args[1]);

			// Test
			System.out.println(DatatypeConverter.printHexBinary(plaintext));
			System.out.println(DatatypeConverter.printHexBinary(ciphertext));

			byte[] key = DatatypeConverter.parseHexBinary("0000111111111100000022222222");
			String strtmp, strtmp1;
			
			// Test 
			System.out.println(DatatypeConverter.printHexBinary(key));
			// declare a hash map
			HashMap<String, String> hmap = new HashMap<String, String>();

			// for output
			PrintWriter writer = new PrintWriter("key.txt", "UTF-8");
			long count = 0;
			System.out.println("Trying all Key1s...");
			
			key[0] = 0x00;
			do
			{
				key[0]++;
				
				key[1] = 0x00;
				do
				{
					key[1]++;
					// for every possible combination of K1
					// now to encrypt using all possible key
					byte[][] bKey = generateKey(DatatypeConverter.printHexBinary(key));	// generate key

					// encrypt using key1
					// c1 = E(plaintext, key1)
					byte[] cipher1 = encryptordecrypt( bKey[0], Cipher.ENCRYPT_MODE, plaintext, 0);

					// Test
					strtmp1 = DatatypeConverter.printHexBinary(cipher1);

					// add <c1, key1i> to hashmap
					strtmp = DatatypeConverter.printHexBinary(key);
					hmap.put(strtmp1, strtmp.substring(0, strtmp.length()/2));

					//System.out.println(++count);

				} while (key[1] != 0x00);
				//System.out.println("This should appear every 256");
			} while (key[0] != 0x00);


			System.out.println(hmap.size());
			count = 0;
			System.out.println("Trying all Key2s...");

			key[7] = 0x00;
			do
			{
				key[7]++;
				key[8] = 0x00;
				do
				{
					key[8]++;
					key[9] = 0x00;
					do
					{
						key[9]++;
						// for every possible combination of K2
						// now to decrypt using all possible keys
						byte[][] bKey = generateKey(DatatypeConverter.printHexBinary(key));	// generate key

						// decrypt using key2
						// p2 = D (ciphertext, key2)
						byte[] plain2 = encryptordecrypt( bKey[1], Cipher.DECRYPT_MODE, ciphertext, 1);

						// Test
						strtmp1 = DatatypeConverter.printHexBinary(plain2);

						// if p2 in hashmap
						if (hmap.containsKey(strtmp1))
						{
							String key1 = hmap.get(strtmp1);
							strtmp = DatatypeConverter.printHexBinary(key);
							String key2 = strtmp.substring(strtmp.length()/2);
							writer.print(key1);
							System.out.print(key1);
							writer.println(key2);
							System.out.println(key2);
						}
						//else
							//System.out.println("No match in HashMap.");

						//System.out.println(++count);
					} while (key[9] != 0x00);
				} while (key[8] != 0x00);
			} while (key[7] != 0x00);

			writer.close();
		}

		catch (Throwable e) 
		{ 
			e.printStackTrace();
		}
	}

	// The actual encrypt/decrypt function
	public static byte[] encryptordecrypt(byte[] key, int mode, byte[] inputText, int DESnumber ) throws Throwable
	{
		// the actual encryption/decryption takes place here
		// check DESnumber
		if ((DESnumber < 0) || (DESnumber > 1))
			System.out.println("\nInvalid entry for DESnumber in encrypt function.\n");

		// Declare the ciphers
		Cipher cipher1 = Cipher.getInstance("DES/ECB/PKCS5Padding");	// When DESnumber == 0
		Cipher cipher2 = Cipher.getInstance("DES/ECB/NoPadding");	// When DESnumber == 1

		// Declare DESKeySpec
		DESKeySpec deskeyspec = new DESKeySpec(key);

		// Declare Secret Key Factory
		SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("DES");

		// Declare Secret Key
		SecretKey secretkey = secretKeyFactory.generateSecret(deskeyspec);

		// Declare byte array to return
		byte[] returnText;
		
		// check the mode
		if (mode == Cipher.ENCRYPT_MODE)
		{
			// check the DESnumber
			if (DESnumber == 0)
			{
				// its the first DES encryption
				cipher1.init(Cipher.ENCRYPT_MODE, secretkey);
				returnText = cipher1.doFinal(inputText);
			}
			else
			{
				// its the second DES encryption
				cipher2.init(Cipher.ENCRYPT_MODE, secretkey);
				returnText = cipher2.doFinal(inputText);
			}
		}
		else 
		{
			// check the DESnumber
			if (DESnumber == 1)
			{
				// its the first decryption
				cipher2.init(Cipher.DECRYPT_MODE, secretkey);
				returnText = cipher2.doFinal(inputText);
			}
			else
			{
				// its the second DES decryption
				cipher1.init(Cipher.DECRYPT_MODE, secretkey);
				returnText = cipher1.doFinal(inputText);
			}
		}

		return returnText;

	}

	// Function to generate 2 56 bit byte Arrays from a String of 112 bits
	public static byte[][] generateKey(String key)
	{
		/* @param Input is a Hex String of the 112 bit key
		 * @discussion Divides the 112 bit key into 2 smaller 56 bit keys and then adds a parity bit every 7 bits
		 * @return Returns a byte[] array containing the generated raw key
		 */
		// Divide the 112 bits to two keys of 56 bits each
		String[] splitKey = {key.substring( 0, key.length()/2),  key.substring(key.length()/2)};
		String[] parity_corrected_key = {"", ""};;
		// Check the lengths
		if ((splitKey[0].length() != key.length()/2) || (splitKey[1].length() != key.length()/2))
			System.out.println ("The keys did not split properly.\n");
		

		// convert hex string to binary string
		String[] binKey = { hexToBinary(splitKey[0]), hexToBinary(splitKey[1])};
			
		// split the 56 bits into 8 groups of 7
		for (int i = 0; i < 2; i++)
		{
			
			String[] bytes = { binKey[i].substring(0, binKey[i].length()/8),
				binKey[i].substring(binKey[i].length()/8, 2*binKey[i].length()/8),
				binKey[i].substring(2*binKey[i].length()/8, 3*binKey[i].length()/8),
				binKey[i].substring(3*binKey[i].length()/8, 4*binKey[i].length()/8),
				binKey[i].substring(4*binKey[i].length()/8, 5*binKey[i].length()/8),
				binKey[i].substring(5*binKey[i].length()/8, 6*binKey[i].length()/8),
				binKey[i].substring(6*binKey[i].length()/8, 7*binKey[i].length()/8),
				binKey[i].substring(7*binKey[i].length()/8) };
			
			String[] parity_corrected_bytes = { oddParityRule(bytes[0]),
				oddParityRule(bytes[1]),
				oddParityRule(bytes[2]),
				oddParityRule(bytes[3]),
				oddParityRule(bytes[4]),
				oddParityRule(bytes[5]),
				oddParityRule(bytes[6]),
				oddParityRule(bytes[7]) };

			for (int j = 0; j < 8; j++)
			{
				// Print the parity corrected bytes
				parity_corrected_key[i] = parity_corrected_key[i] + parity_corrected_bytes[j];
			}
		}
		
		// convert bin string to hex string
		String[] hexKey = { binaryToHex(parity_corrected_key[0]), binaryToHex(parity_corrected_key[1]) };

		// Convert the 64 bits into a byte array of length 8
		byte[] byteKey1 = DatatypeConverter.parseHexBinary(hexKey[0]);
		byte[] byteKey2 = DatatypeConverter.parseHexBinary(hexKey[1]);
		byte[][] byteKey = {byteKey1, byteKey2};

		return byteKey;

	}

	// A function that takes a hex string and returns the corresponding binary string
	public static String hexToBinary(String hex) 
	{
		String hex_char,bin_char,binary;
		binary = "";
		int len = hex.length()/2;
		for(int i=0;i<len;i++)
		{
			hex_char = hex.substring(2*i,2*i+2);
			int conv_int = Integer.parseInt(hex_char,16);
			bin_char = Integer.toBinaryString(conv_int);
			bin_char = zero_pad_bin_char(bin_char);
			if(i==0) binary = bin_char; 
			else binary = binary+bin_char;
		}
		return binary;
	}

	// A function that takes a binary string and returns a hex string
	public static String binaryToHex(String binary) 
	{
		String hex = "";
		String hex_char;
		int len = binary.length()/8;
		
		for(int i=0;i<len;i++)
		{
			String bin_char = binary.substring(8*i,8*i+8);
			int conv_int = Integer.parseInt(bin_char,2);
			hex_char = Integer.toHexString(conv_int);
			hex_char = zero_pad_hex_char(hex_char);
			if(i==0) hex = hex_char;
			else hex = hex+hex_char;
		}
		
		return hex;
	}

	// A function to 0 pad hex strings
	public static String zero_pad_hex_char(String hex_char)
	{
		int len = hex_char.length();
		if(len == 2) return hex_char;
		String zero_pad = "0";
		for(int i = 1; i < 2-len; i++)
			zero_pad = zero_pad + "0";
		return zero_pad + hex_char;
	}

	// A function to 0 pad the binary strings
	public static String zero_pad_bin_char(String bin_char)
	{
		int len = bin_char.length();
		if(len == 8) return bin_char;
		String zero_pad = "0";
		for(int i=1;i<8-len;i++) 
			zero_pad = zero_pad + "0"; 
		return zero_pad + bin_char;
	}
	// a function for the parity bit
	// @param a binary String containing 7 bits
	// @return a binary String containing 8 bits with odd parity rule
	public static String oddParityRule(String str1)
	{
		int count = 0;
		// byte contains 7 bits
		for (int i = 0; i < str1.length(); i++)
		{
			if (str1.charAt(i) == '1')
				count++;
		}

		if (count%2 == 0)
		{
			// the number of 1's is even
			str1 = str1 + "1";
		}
		else
		{
			// the number of 1's is odd
			str1 = str1 + "0";
		}

		return str1;
	}


}
