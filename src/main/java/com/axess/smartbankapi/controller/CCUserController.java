package com.axess.smartbankapi.controller;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.axess.smartbankapi.config.restapi.ApiSuccessResponse;
import com.axess.smartbankapi.constants.Constants;
import com.axess.smartbankapi.dto.LoginDto;
import com.axess.smartbankapi.exception.RecordExistException;
import com.axess.smartbankapi.exception.RecordNotCreatedException;
import com.axess.smartbankapi.exception.RecordNotFoundException;
import com.axess.smartbankapi.model.CCUser;
import com.axess.smartbankapi.service.CCUserService;
import com.axess.smartbankapi.service.SecretKeyService;
import com.axess.smartbankapi.utils.GenerateKeyUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/ccuser")
@CrossOrigin
@PropertySource("classpath:application.yml")
@ConfigurationProperties
@Configuration
public class CCUserController {

    @Autowired
    private CCUserService ccUserService;

    @Autowired
    private SecretKeyService secretKeyService;
    @Autowired
    GenerateKeyUtil generateKeyUtil;

    private static ObjectMapper mapper = new ObjectMapper();

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.credentials.secret-name}")
    private String secretName;

    @Value("${cloud.aws.region.static}")
    private String region;

    @PostMapping("/login")
    public ResponseEntity<?> verifyLogin(@RequestBody LoginDto loginDto) throws RecordNotFoundException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

        ApiSuccessResponse response = new ApiSuccessResponse();

        String secretKey = getSecret();
        SecretKey key = secretKeyService.decrypt(secretKey);

        IvParameterSpec ivParameterSp = generateKeyUtil.generateIv();

        String algorithm = Constants.ALGORITHM_USED;
        String cipherText = generateKeyUtil.encrypt(algorithm, loginDto.getPassword(), key, ivParameterSp);

        CCUser loggedInUser = this.ccUserService.getLoginDetails(loginDto.getUserId(), cipherText);

        response.setMessage("Login Verified successfully. ");
        response.setHttpStatus(String.valueOf(HttpStatus.FOUND));
        response.setHttpStatusCode(200);
        response.setBody(loggedInUser);
        response.setError(false);
        response.setSuccess(true);

        return ResponseEntity.status(HttpStatus.OK).header("status", String.valueOf(HttpStatus.OK))
                .body(response);


    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody CCUser ccUser) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, RecordNotCreatedException, RecordExistException {

        ApiSuccessResponse response = new ApiSuccessResponse();
        String secretKey = getSecret();

        SecretKey key = secretKeyService.decrypt(secretKey);
        IvParameterSpec ivParameterSp = generateKeyUtil.generateIv();

        String algorithm = Constants.ALGORITHM_USED;

        String cipherText = generateKeyUtil.encrypt(algorithm, ccUser.getPassword(), key, ivParameterSp);

        ccUser.setPassword(cipherText);
        String register = this.ccUserService.saveUser(ccUser);
        response.setMessage("Registered successfully. ");
        response.setHttpStatus(String.valueOf(HttpStatus.CREATED));
        response.setHttpStatusCode(200);
        response.setBody(register);
        response.setError(false);
        response.setSuccess(true);

        return ResponseEntity.status(HttpStatus.OK).header("status", String.valueOf(HttpStatus.OK))
                .body(response);


    }

    @GetMapping("/")
    public ResponseEntity<?> getUsers() throws RecordNotFoundException {

        ApiSuccessResponse response = new ApiSuccessResponse();

        List<CCUser> users = this.ccUserService.getAllUsers();

        response.setMessage("No. Of users -  " + users.size());
        response.setHttpStatus(String.valueOf(HttpStatus.FOUND));
        response.setHttpStatusCode(302);
        response.setBody(users);
        response.setError(false);
        response.setSuccess(true);

        return ResponseEntity.status(HttpStatus.OK).header("status", String.valueOf(HttpStatus.OK))
                .body(response);


    }

    public String getSecret() {

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);

        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(region)
                .build();

        String secret, secretValue;
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(secretName);
        GetSecretValueResult getSecretValueResult = null;

        try {
            getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        } catch (Exception e) {
            throw e;
        }

        if (getSecretValueResult.getSecretString() != null) {
            secret = getSecretValueResult.getSecretString();
            secretValue = getString(secret, "mySecret");

        } else {
            secret = new String(Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
            secretValue = getString(secret, "mySecret");
        }
        return secretValue;
    }

    private static String getString(String json, String path) {
        try {
            JsonNode root = mapper.readTree(json);
            return root.path(path).asText();
        } catch (IOException e) {
            System.out.println("Can't get {} from json {}" + path + json + e);
            return null;
        }
    }

}
