package com.axess.smartbankapi.utils;


import com.axess.smartbankapi.constants.Constants;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class GenerateKeyUtil {

  public SecretKey generateSecretKey() throws NoSuchAlgorithmException {
    KeyGenerator keyGenerator = KeyGenerator.getInstance(Constants.ALGO_TYPE);
    keyGenerator.init(Constants.KEY_SIZE_BIT);
    return keyGenerator.generateKey();
  }

  public IvParameterSpec generateIv() {
    byte[] iv = new byte[Constants.IV_SIZE_BYTE];
   // new SecureRandom().nextBytes(iv);
    return new IvParameterSpec(iv);
  }

  public String encrypt(
      String algorithm, String input, SecretKey key, IvParameterSpec ivParameterSpec)
      throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException,
          BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
    Cipher cipher = Cipher.getInstance(algorithm);
    cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);
    byte[] cipherText = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(cipherText);
  }

  public String decrypt(
      String algorithm, String cipherText, SecretKey key, IvParameterSpec ivParameterSpec)
      throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException,
          BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {

    Cipher cipher = Cipher.getInstance(algorithm);
    cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);
    byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
    return new String(plainText, StandardCharsets.UTF_8);
  }
}
