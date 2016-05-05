// Author: Rishabh Sawhney
// Date: 19 March 2016
// DictAttack.java - Lab 3
// Takes a password file and a dictionary file and attempts to crack the passwords

import java.io.*;
import javax.crypto.*;
import java.security.*;
import java.lang.*;
import java.util.*;
import javax.xml.bind.DatatypeConverter;

public class DictAttack
{
	public static void main (String[] args)
	{	// check if correct number of arguments
		if (args.length != 3)
		{
			System.err.println("Wrong number of arguments to DictAttack. Correct usage: DictAttack passfile dictfile crackfile");
			System.exit(1);
		}
		// convert command line input in string form
		String passwordFile = args[0];	// first argument is the password file
		String dictFile = args[1]; 	// second argument is the dictionary filei
		String crackFile = args[2];	// third argument is the cracked file that stoes the cracked passwords

		Scanner sPass = null;
		Scanner sDict = null;
		PrintWriter writer = null;
		
		String word, line;

		try
		{
			// Open the files
			sDict = new Scanner (new BufferedReader(new FileReader(dictFile)));
			writer = new PrintWriter(crackFile, "UTF-8");

			while (sDict.hasNext())
			{
				// get the next word
				word = sDict.next();

				sPass = new Scanner (new BufferedReader(new FileReader(passwordFile)));
				sPass.useDelimiter("\n");

				// get the corresponding hashes
				MessageDigest md = MessageDigest.getInstance("SHA1");
				md.update(word.getBytes());
				byte[] wordHash = md.digest();
				String wordHashS = DatatypeConverter.printHexBinary(wordHash);

				String reverse = new StringBuilder(word).reverse().toString();
				md.update(reverse.getBytes());
				byte[] reverseHash = md.digest();
				String reverseHashS = DatatypeConverter.printHexBinary(reverseHash);

				String noVowel = deVowel(word);
				md.update(noVowel.getBytes());
				byte[] noVowelHash = md.digest();
				String noVowelHashS = DatatypeConverter.printHexBinary(noVowelHash);

				while (sPass.hasNext())
				{
					// get the next password file line
					line = sPass.next();

					// parse line
					String[] token = line.split(" ");
					/*
					for (int i = 0;i < token.length; i++)
					{
						System.out.println(token[i]);
					}
					*/
					// if no salt
					if (token[1].equals("0"))
					{
						//System.out.println(token[1]);
						String hash = DatatypeConverter.printHexBinary(DatatypeConverter.parseHexBinary(token[2]));
						if (hash.equals(wordHashS))
						{
							// This means that the password is word
							writer.println(token[0] + ":" + word);
						}
						else if (hash.equals(reverseHashS))
						{
							writer.println(token[0] + ":" + reverse);
						}
						else if (hash.equals(noVowelHashS))
						{
							writer.println(token[0] + ":" + noVowel);
						}
						/*
						System.out.println(hash);		
						System.out.println(wordHashS);
						System.out.println(reverseHashS);
						System.out.println(noVowelHashS);
						*/
					}
					else // if there is salt
					{
						// then token[2] is the salt in string form
						byte[] salt = DatatypeConverter.parseHexBinary(token[2]);
						byte[] wordBytes = word.getBytes();
						byte[] reverseBytes = reverse.getBytes();
						byte[] noVowelBytes = noVowel.getBytes();

						// concatenate salt||wordBytes
						byte[] saltedWord = new byte[salt.length + wordBytes.length];
						System.arraycopy(salt, 0, saltedWord, 0, salt.length);
						System.arraycopy(wordBytes, 0, saltedWord, salt.length, wordBytes.length);
						
						// concatenate salt||reverseBytes
						byte[] saltedReverse = new byte[salt.length + reverseBytes.length];
						System.arraycopy(salt, 0, saltedReverse, 0, salt.length);
						System.arraycopy(reverseBytes, 0, saltedReverse, salt.length, reverseBytes.length);
							
						// concatenate salt||noVowelBytes
						byte[] saltedNoVowel = new byte[salt.length + noVowelBytes.length];
						System.arraycopy(salt, 0, saltedNoVowel, 0, salt.length);
						System.arraycopy(wordBytes, 0, saltedNoVowel, salt.length, noVowelBytes.length);
						

						// Hash the salted word/reverse/noVowel
						md.update(saltedWord);
						byte[] saltedWordHash = md.digest();
						String saltedWordHashS = DatatypeConverter.printHexBinary(saltedWordHash);

						md.update(saltedReverse);
						byte[] saltedReverseHash = md.digest();
						String saltedReverseHashS = DatatypeConverter.printHexBinary(saltedReverseHash);

						md.update(saltedNoVowel);
						byte[] saltedNoVowelHash = md.digest();
						String saltedNoVowelHashS = DatatypeConverter.printHexBinary(saltedNoVowelHash);

						String hash = DatatypeConverter.printHexBinary(DatatypeConverter.parseHexBinary(token[3]));
						// if hash matches one of the calculates hashes
						if (hash.equals(saltedWordHashS))
						{
							// This means that the password is word
							writer.println(token[0] + ":" + word);
						//	System.out.println(DatatypeConverter.printHexBinary(salt));
						//	System.out.println(new String(saltedWord));
						}
						else if (hash.equals(saltedReverseHashS))
						{
							writer.println(token[0] + ":" + reverse);
						}
						else if (hash.equals(saltedNoVowelHashS))
						{
							writer.println(token[0] + ":" + noVowel);
						}


					}
					//break;
				}

				// Close the Scanner
				sPass.close();
				//break;

			}

			// Close the scanner and writer
			sDict.close();
			writer.close();

		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
		finally
		{
			// close files
			if (sPass != null)
			{
				sPass.close();
			}
			if (sDict != null)
			{
				sDict.close();
			}
			if (writer != null);
			{
				writer.close();
			}
		}
	}

	// A static function that atkes a string and returns the same string removing the vowels 
	public static String deVowel (String word)
	{
		String retVal = "";

		for (int i = 0; i < word.length(); i++)
		{
			// if character is a consonant
			if ((word.toLowerCase().charAt(i) != 'a') && 
					(word.toLowerCase().charAt(i) != 'e') &&
					(word.toLowerCase().charAt(i) != 'i') && 
					(word.toLowerCase().charAt(i) != 'o') && (word.toLowerCase().charAt(i) != 'u'))
			{	
				// add the letter to the return value
				retVal += word.charAt(i);
			}
		}

		return retVal;
	}
}
