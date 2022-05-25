//package com.axess.smartbankapi.utils;
//
//import com.google.api.gax.rpc.NotFoundException;
//import com.google.cloud.secretmanager.v1.*;
//import com.google.cloud.spring.core.GcpProjectIdProvider;
//import com.google.cloud.spring.secretmanager.SecretManagerTemplate;
//import com.google.protobuf.ByteString;
//import com.lblw.vphx.objectstorage.constants.Constants;
//import com.lblw.vphx.objectstorage.service.SecretKeyService;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.exception.ExceptionUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import reactor.core.publisher.Mono;
//
//import java.security.NoSuchAlgorithmException;
//import java.util.HashMap;
//
//import static com.google.cloud.spring.secretmanager.SecretManagerTemplate.LATEST_VERSION;
//
//@Slf4j
//@Component
//public class SecretManagerUtil {
//
//  @Autowired private SecretManagerTemplate secretManagerTemplate;
//  @Autowired private SecretManagerServiceClient secretManagerServiceClient;
//  @Autowired private GcpProjectIdProvider projectIdProvider;
//  @Autowired private SecretKeyService secretKeyService;
//
//  public Mono<String> getSecretKey(String secretId, String version) {
//    return Mono.just(secretManagerTemplate.getSecretString("sm://" + secretId + "/" + version));
//  }
//
//  public Mono<HashMap<String, String>> getData(String secretId) throws NoSuchAlgorithmException {
//    String versionValue;
//    String payload;
//    try {
//      SecretVersionName secretVersionName =
//          SecretVersionName.newBuilder()
//              .setProject(this.projectIdProvider.getProjectId())
//              .setSecret(secretId)
//              .setSecretVersion(LATEST_VERSION)
//              .build();
//      AccessSecretVersionResponse response =
//          secretManagerServiceClient.accessSecretVersion(secretVersionName);
//
//      payload = response.getPayload().getData().toStringUtf8();
//      versionValue = response.getName().substring(response.getName().lastIndexOf('/') + 1);
//      log.info(Constants.RETRIEVE_SECRET_MSG);
//    } catch (NotFoundException ex) {
//      versionValue = Constants.FIRST_VERSION;
//      payload = addSecretVersion(secretId).block();
//    } catch (Exception ex) {
//      log.error(
//          Constants.ERROR_WHILE_RETRIEVING_SECRET + ExceptionUtils.getStackTrace(ex));
//      throw ex;
//    }
//    HashMap<String, String> map = new HashMap<>();
//    map.put(Constants.SECRET_KEY_VERSION, versionValue);
//    map.put(Constants.SECRET_KEY, payload);
//    return Mono.just(map);
//  }
//
//  public Mono<String> addSecretVersion(String secretId) throws NoSuchAlgorithmException {
//    String secretKey = secretKeyService.generateKey();
//    SecretName name =
//        SecretName.newBuilder()
//            .setProject(this.projectIdProvider.getProjectId())
//            .setSecret(secretId)
//            .build();
//    AddSecretVersionRequest payloadRequest =
//        AddSecretVersionRequest.newBuilder()
//            .setParent(name.toString())
//            .setPayload(SecretPayload.newBuilder().setData(ByteString.copyFromUtf8(secretKey)))
//            .build();
//    secretManagerServiceClient.addSecretVersion(payloadRequest);
//    log.info(Constants.ADD_NEW_SECRET_VERSION_MSG);
//    return Mono.just(secretKey);
//  }
//}
