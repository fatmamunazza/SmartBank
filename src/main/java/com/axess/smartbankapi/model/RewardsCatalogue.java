package com.axess.smartbankapi.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("RewardsCatalogue")
@Data
public class RewardsCatalogue {

    @Id
    private long id;
    private String item;
    private double redeemptionPoint;
    private double redeemptionAmount;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public double getRedeemptionPoint() {
        return redeemptionPoint;
    }

    public void setRedeemptionPoint(double redeemptionPoint) {
        this.redeemptionPoint = redeemptionPoint;
    }

    public double getRedeemptionAmount() {
        return redeemptionAmount;
    }

    public void setRedeemptionAmount(double redeemptionAmount) {
        this.redeemptionAmount = redeemptionAmount;
    }


}
