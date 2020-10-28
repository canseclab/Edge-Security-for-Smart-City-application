
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import java.util.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES
{
	public static void main(String[] args)
		throws InvalidKeyException,NoSuchAlgorithmException, NoSuchPaddingException, 
		UnsupportedEncodingException, InvalidAlgorithmParameterException, 
		IllegalBlockSizeException, BadPaddingException
	{
		Scanner in = new Scanner(System.in);
		Scanner on = new Scanner(System.in);
		AES_Object use = new AES_Object();
		while(true)
		{
			System.out.println("Choose the service you want to use: (1.En/Decrypt 0.Exit)");
			int input = in.nextInt();
			if(input == 0)
				break;
			System.out.println("Choose the key bits: (1.128 bits 2.256 bits)");
			int key_size = in.nextInt();
			int keylength = 0;
			if(key_size == 1)
				keylength = 128;
			else
				keylength = 256;
			String key = use.GetKey(keylength);
			System.out.println("Choose the mode you want: (1.CBC 2.CFB 3.CFB8)");
			int mode = in.nextInt();
			String mod = "";
			if(mode == 1)
				mod = "CBC";
			else if(mode == 2)
				mod = "CFB";
			else if(mode == 3)
				mod = "CFB8";
			String IV = use.GetIV();
			System.out.println("Enter the plaintext:");
			String plaintext = on.nextLine();
			String ciphertext = use.encryptAES(plaintext, key, mod, IV);			//加密
			String Decrypt_text = use.decryptAES(ciphertext, key, mod, IV);			//解密
			System.out.println("----------------Encrypting----------------\n");
			System.out.println("The Ciphertext is \"" + ciphertext + "\"\n");
			System.out.println("----------------Decrypting----------------\n");
			System.out.println("The Plaintext is \"" + Decrypt_text + "\"\n");
			System.out.println("------------------------------------------");
		}
	}
}

class AES_Object
{
	public String GetKey(int keylength)												//亂數取得Key
	{
		Random ran = new Random();
		int length = keylength / 8;
		String key = "";
		for(int i = 0; i < length; i++)
		{
			int value = ran.nextInt(62);
			if(value >= 0 && value <= 9)
				key += (char) (value + (int) '0');
			else if(value >= 10 && value <=35)
				key += (char) (value - 10 + (int) 'a');
			else
				key += (char) (value - 36 + (int) 'A');
		}
		return key;
	}
	
	public String GetIV()															//取得IV
	{
		Calendar now = Calendar.getInstance();		//建立Calendar物件 已取得時間
		String IV = "";
		IV += now.get(Calendar.YEAR);
		IV += now.get(Calendar.MONTH);
		IV += now.get(Calendar.DAY_OF_MONTH);
		IV += now.get(Calendar.HOUR);
		IV += now.get(Calendar.MINUTE);
		IV += now.get(Calendar.SECOND);
		IV += now.get(Calendar.MILLISECOND);		//取得年份、月份、日期、小時
		IV += "0000";								//分鐘、秒、毫秒，為避免不滿16位數
		IV = IV.substring(0, 16);					//補零再取0-16位數作為IV
		return IV;
	}
	
	public String default_IV = "0123456789123456";
	public String default_mode = "CBC";
	
	public String encryptAES(String content, String key, String mode, String IV) 	//加密Function		
		throws InvalidKeyException, NoSuchAlgorithmException, 
		NoSuchPaddingException, UnsupportedEncodingException, 
		InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

		byte[] byteContent = content.getBytes("UTF-8");

		byte[] enCodeFormat = key.getBytes();
		//System.out.println(enCodeFormat.length);
	    SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, "AES");
			
	    byte[] initParam = IV.getBytes();
	    IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
			
	    Cipher cipher = Cipher.getInstance("AES/" + mode + "/PKCS5Padding");				//決定mode
	    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
		
	    byte[] encryptedBytes = cipher.doFinal(byteContent);
		
	    Encoder encoder = Base64.getEncoder();
	    return encoder.encodeToString(encryptedBytes);
	}

	public String decryptAES(String content, String key, String mode, String IV) 				//解密Function
			throws InvalidKeyException, NoSuchAlgorithmException, 
			NoSuchPaddingException, InvalidAlgorithmParameterException, 
			IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
			
	    Decoder decoder = Base64.getDecoder();
	    byte[] encryptedBytes = decoder.decode(content);
		
	    byte[] enCodeFormat = key.getBytes();
	    SecretKeySpec secretKey = new SecretKeySpec(enCodeFormat, "AES");
		
	    byte[] initParam = IV.getBytes();
	    IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);

	    Cipher cipher = Cipher.getInstance("AES/" + mode + "/PKCS5Padding");				//決定mode
	    cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

	    byte[] result = cipher.doFinal(encryptedBytes);
		
	    return new String(result, "UTF-8");
	}
	
	public byte[] File_encrypt(byte[] byteContent, byte[] enCodeFormat) 	//Encrypt file		
			throws InvalidKeyException, NoSuchAlgorithmException, 
			NoSuchPaddingException, UnsupportedEncodingException, 
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException 
	{

		SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, "AES");
				
		byte[] initParam = default_IV.getBytes();
		IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
				
		Cipher cipher = Cipher.getInstance("AES/" + default_mode + "/PKCS5Padding");				//決定mode
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
			
		byte[] encryptedBytes = cipher.doFinal(byteContent);
			
		return encryptedBytes;
	}

	public byte[] File_decrypt(byte[] encryptedBytes, byte[] enCodeFormat) 		//decrypt file
				throws InvalidKeyException, NoSuchAlgorithmException, 
				NoSuchPaddingException, InvalidAlgorithmParameterException, 
				IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException 
	{
		
		SecretKeySpec secretKey = new SecretKeySpec(enCodeFormat, "AES");
			
		byte[] initParam = default_IV.getBytes();
		IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);

		Cipher cipher = Cipher.getInstance("AES/" + default_mode + "/PKCS5Padding");				//決定mode
		cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

		byte[] result = cipher.doFinal(encryptedBytes);
			
		return result;
	}
}
