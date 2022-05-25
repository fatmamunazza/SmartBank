package com.axess.smartbankapi.controller;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.*;
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
import java.security.InvalidParameterException;
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

        String secretName = "arn:aws:secretsmanager:us-east-1:350358714376:secret:test/awssecret-YgoNqs";
        String region = "us-east-1";

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        // Create a Secrets Manager client
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
        } catch (DecryptionFailureException e) {
            // Secrets Manager can't decrypt the protected secret text using the provided KMS key.
            // Deal with the exception here, and/or rethrow at your discretion.
            throw e;
        } catch (InternalServiceErrorException e) {
            // An error occurred on the server side.
            // Deal with the exception here, and/or rethrow at your discretion.
            throw e;
        } catch (InvalidParameterException e) {
            // You provided an invalid value for a parameter.
            // Deal with the exception here, and/or rethrow at your discretion.
            throw e;
        } catch (InvalidRequestException e) {
            // You provided a parameter value that is not valid for the current state of the resource.
            // Deal with the exception here, and/or rethrow at your discretion.
            throw e;
        } catch (ResourceNotFoundException e) {
            // We can't find the resource that you asked for.
            // Deal with the exception here, and/or rethrow at your discretion.
            throw e;
        }

        // Decrypts secret using the associated KMS key.
        // Depending on whether the secret is a string or binary, one of these fields will be populated.
        if (getSecretValueResult.getSecretString() != null) {
            secret = getSecretValueResult.getSecretString();
            secretValue = getString(secret, "mySecret");

        } else {
            secret = new String(Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
            secretValue = getString(secret, "mySecret");
        }
        return secretValue;
        // Your code goes here.
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
