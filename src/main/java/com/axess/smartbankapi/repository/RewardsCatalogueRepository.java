package com.axess.smartbankapi.repository;

import com.axess.smartbankapi.model.RewardsCatalogue;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RewardsCatalogueRepository extends MongoRepository<RewardsCatalogue, Long> {

}
