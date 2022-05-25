package com.axess.smartbankapi.repository;

import com.axess.smartbankapi.model.UserRedeemptionHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedeemptionHistoryRepository extends MongoRepository<UserRedeemptionHistory, Long> {

}


