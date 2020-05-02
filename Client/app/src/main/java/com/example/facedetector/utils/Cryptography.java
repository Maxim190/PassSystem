package com.example.facedetector.utils;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Cryptography {

    private BigInteger privateKey;
    private byte[] sharedAESKey;

    private String ivParameter = "0000000000000000";

    public Cryptography() {
        privateKey = new BigInteger(1024, new SecureRandom());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public byte[] encrypt(byte[] src) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec skeySpec = new SecretKeySpec(sharedAESKey, "AES");
        IvParameterSpec iv = new IvParameterSpec (ivParameter.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(src);
        return Base64.getEncoder().encode(encrypted);
    }

    private byte[] deleteEndSpaces (byte[] src) {
        if (src[src.length - 1] == ' ') {
            int counter = 0;
            for (int i = src.length - 1; i > 0; i--) {
                if (src[i] != ' ') {
                    break;
                }
                counter++;
            }
            src = Arrays.copyOfRange(src, 0, src.length - counter);
        }
        return src;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public byte[] decrypt(byte[] src) {
        try {
            src = deleteEndSpaces(src);
            SecretKeySpec skeySpec = new SecretKeySpec(sharedAESKey, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte [] encrypted = Base64.getDecoder().decode(src);
            return cipher.doFinal(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new byte[0];
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public byte[] calcPublicKeyFromServerParams(Map<String, byte[]> data) {
        if (data != null && !data.isEmpty()) {
            BigInteger p = new BigInteger(JSONManager.parseToStr(data.get("p")));
            BigInteger g = new BigInteger(JSONManager.parseToStr(data.get("g")));
            BigInteger serverPublicKey = new BigInteger(JSONManager.parseToStr(data.get("public_key")));

//            try {
                BigInteger sharedNum = serverPublicKey.modPow(privateKey, p);
//                MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
//                sha256.update(serverPublicKey.modPow(privateKey, p).toString().getBytes());
//                sharedAESKey = sha256.digest(serverPublicKey.modPow(privateKey, p).toString().getBytes());
//                Log.i("PassSystem", "SHARED -- " + Base64.getEncoder().encodeToString(sharedAESKey));
                sharedAESKey = Arrays.copyOf(sharedNum.toString().getBytes(), 32);
//            } catch (NoSuchAlgorithmException e) {
//                e.printStackTrace();
//            }

            BigInteger publicKey = g.modPow(privateKey, p);
            return publicKey.toString().getBytes();
        }
        else {
            return null;
        }
    }
}
