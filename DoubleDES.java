/* CMPSC 443 Lab 1
 * Written by : Rishabh Sawhney
 * DoubleDES.java
 * @discussion This class implements a double DES encyption with a keysize of 112 .
 *
 */
import java.math.BigInteger;
import java.lang.Object;
import java.util.Arrays;
import java.io.*;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.xml.bind.DatatypeConverter;

public class DoubleDES 
{
	public static void main(String[] args) 
	{
		try 
		{
			// recieve command line input in string form and convert to hex bytes
			String key = args[0];			// Hex String key is taken from the first command line argument
			byte[] plainTextBytes = DatatypeConverter.parseHexBinary(args[1]); 	// Plain text is stored in a byte array

			// Generate the Key
			byte[][] bKey = generatekey(key);
			
			// encrypt using  doubleDES
			byte[] cipherText = doubleDESencrypt( bKey, plainTextBytes);

			// print the final encrytipn 
			System.out.println(/*"Ciphertext : " + */DatatypeConverter.printHexBinary(cipherText));

			// decrypt using doubleDES
		//	byte[] cleartext = doubleDESdecrypt( bKey, cipherText);

			// print the decryption
		//	System.out.println("Cleartext : " + DatatypeConverter.printHexBinary(cleartext));
			
		} 
		catch (Throwable e) 
		{ 
			e.printStackTrace();
		}
	}
	public static byte[] doubleDESencrypt( byte[][] bKey, byte[] plainTextBytes) throws Throwable
	{
		// call the encrypt or decrypt function using the first 56 bit key and encrypt mode
		byte[] middleTextBytes = encryptordecrypt( bKey[0], Cipher.ENCRYPT_MODE, plainTextBytes, 0 );

		// pass the result to the next encyption with the next 56 bits as key
		byte[] cipherTextBytes = encryptordecrypt (bKey[1], Cipher.ENCRYPT_MODE, middleTextBytes, 1);

		return cipherTextBytes;
	}

	public static byte[] doubleDESdecrypt ( byte[][] bKey, byte[] cipherTextBytes) throws Throwable
	{
		byte[] middletext = encryptordecrypt( bKey[1], Cipher.DECRYPT_MODE, cipherTextBytes, 1);
		byte[] plaintext = encryptordecrypt( bKey[0], Cipher.DECRYPT_MODE, middletext, 0);
		return plaintext;
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
	public static byte[][] generatekey(String key)
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
