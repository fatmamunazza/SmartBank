package com.axess.smartbankapi.service;

import com.axess.smartbankapi.constants.Constants;
import com.axess.smartbankapi.utils.GenerateKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class SecretKeyService {

    @Autowired
    GenerateKeyUtil generateKeyUtil;

    public String generateKey() throws NoSuchAlgorithmException { // keep it private
        // genrerateKey generate uncoded key
        SecretKey key = generateKeyUtil.generateSecretKey();
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public SecretKey decrypt(String secretKey) {
        byte[] byteKey = Base64.getDecoder().decode(secretKey);
        return new SecretKeySpec(byteKey, 0, byteKey.length, Constants.ALGO_TYPE);
    }
}
