package ru.mauveferret;

import javax.crypto.Cipher;
import java.io.InputStream;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

//FIXME try blocks

class RSA {
    KeyPair generateKeyPair()  {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048, new SecureRandom());
            KeyPair pair = generator.generateKeyPair();
            return pair;
        }
        catch (NoSuchAlgorithmException ex)
        {
            System.out.println("so so strange");
            return  null;
        }
    }

    PublicKey bytesToPublicKey(byte[] bytes)
    {
        try {
            X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(ks);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

   /* PublicKey bytesToPrivateKey(byte[] bytes)
    {
        try {
            PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(ks);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    */

    public static KeyPair getKeyPairFromKeyStore() throws Exception {
        //Generated with:
        //  keytool -genkeypair -alias mykey -storepass s3cr3t -keypass s3cr3t -keyalg RSA -keystore keystore.jks

        InputStream ins = RSA.class.getResourceAsStream("/keystore.jks");

        KeyStore keyStore = KeyStore.getInstance("JCEKS");
        keyStore.load(ins, "s3cr3t".toCharArray());   //Keystore password
        KeyStore.PasswordProtection keyPassword =       //Key password
                new KeyStore.PasswordProtection("s3cr3t".toCharArray());

        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry("mykey", keyPassword);

        java.security.cert.Certificate cert = keyStore.getCertificate("mykey");
        PublicKey publicKey = cert.getPublicKey();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        return new KeyPair(publicKey, privateKey);
    }

    String encrypt(String plainText, PublicKey publicKey)  {
        byte[] cipherText = new byte[5];
        try {
            Cipher encryptCipher = Cipher.getInstance("RSA");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

             cipherText = encryptCipher.doFinal(plainText.getBytes(UTF_8));

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return Base64.getEncoder().encodeToString(cipherText);
    }

     String decrypt(String cipherText, PrivateKey privateKey)  {
        byte[] bytes = Base64.getDecoder().decode(cipherText);

       try {
           Cipher decriptCipher = Cipher.getInstance("RSA");
           decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);

           return new String(decriptCipher.doFinal(bytes), UTF_8);
       }
       catch (Exception e)
       {
           e.printStackTrace();
           return "";
       }
    }

    public static String sign(String plainText, PrivateKey privateKey) throws Exception {
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(plainText.getBytes(UTF_8));

        byte[] signature = privateSignature.sign();

        return Base64.getEncoder().encodeToString(signature);
    }

    public static boolean verify(String plainText, String signature, PublicKey publicKey) throws Exception {
        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(publicKey);
        publicSignature.update(plainText.getBytes(UTF_8));

        byte[] signatureBytes = Base64.getDecoder().decode(signature);

        return publicSignature.verify(signatureBytes);
    }
}
