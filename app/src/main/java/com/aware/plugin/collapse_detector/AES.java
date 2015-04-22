package com.aware.plugin.collapse_detector;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import android.util.Base64;
import android.util.Log;


public class AES {
    private static final String transformation = "AES/ECB/NoPadding"; //should be changed to CBC
    private static final String algorithm = "AES";
    private static final int blockSize = 16;
    private static final byte[] keyValue ={ '1', '2', '3', '4', '5', '6', '7',
            '8', '9', '0', 'a','b', 'c', 'd', 'e', 'f' };

    public static String encrypt(String data) {
        try{
            SecretKeySpec key = new SecretKeySpec(keyValue, algorithm);
            Cipher c = Cipher.getInstance(transformation);
            c.init(Cipher.ENCRYPT_MODE, key);
            //padding string with '{' to be divisible by 16
            int padAmount=(blockSize - data.length() % blockSize);
            String padding = new String(new char[padAmount]).replace('\0', '!');
            data += padding;
            Log.d("AES ","DATA "+data );
            //encoding and encrypting
            byte[] encryptedValue = c.doFinal(data.getBytes());
            byte[] encodedValue = Base64.encode(encryptedValue, Base64.DEFAULT);
            return new String(encodedValue);

        }catch(Exception e){
            Log.e("AES", "Error in encryption", e);
            e.printStackTrace();
            return null;
        }
    }

    public static String decrypt(String encryptedData) {
        try {
            //decoding and decrypting
            SecretKeySpec key = new SecretKeySpec(keyValue, algorithm);
            Cipher c = Cipher.getInstance(transformation);
            c.init(Cipher.DECRYPT_MODE, key);
            byte[] decodedValue = Base64.decode(encryptedData.getBytes(), Base64.DEFAULT);
            byte[] decryptedValue = c.doFinal(decodedValue);
            return new String(decryptedValue);

        }catch(Exception e){
            Log.e("AES", "Error in decryption", e);
            e.printStackTrace();
            return null;
        }
    }
}
