package com.axess.smartbankapi.repository;


import com.axess.smartbankapi.model.CCUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CCUserRepository extends MongoRepository<CCUser, Long> {

     Optional<CCUser> findByUserIdAndPassword(String userId, String password);
}
