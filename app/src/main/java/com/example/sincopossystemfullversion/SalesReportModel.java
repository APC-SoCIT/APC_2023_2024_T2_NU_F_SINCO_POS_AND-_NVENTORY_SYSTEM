package com.example.sincopossystemfullversion;

import java.util.UUID;

public class SalesReportModel {

    private String sr_transactionid;

    String sr_date, sr_payment, sr_product;
    Long sr_price, sr_qty, sr_total;

    public SalesReportModel() {
    }

    public SalesReportModel(String sr_date, String sr_payment, String sr_product, Long sr_price, Long sr_qty, Long sr_total, String sr_transactionid) {
        this.sr_date = sr_date;
        this.sr_payment = sr_payment;
        this.sr_price = sr_price;
        this.sr_product = sr_product;
        this.sr_qty = sr_qty;
        this.sr_total = sr_total;
        this.sr_transactionid = sr_transactionid;
    }

    public Long getSr_price() {
        return sr_price;
    }

    public void setSr_price(Long sr_price) {
        this.sr_price = sr_price;
    }

    public Long getSr_qty() {
        return sr_qty;
    }

    public void setSr_qty(int sr_qty) {
        this.sr_qty = (long) sr_qty;
    }


    public Long getSr_total() {
        return sr_total;
    }

    public void setSr_total(Long sr_total) {
        this.sr_total = sr_total;
    }

    public String getSr_transactionid() {
        return sr_transactionid;
    }

    public void setSr_transactionid(String sr_transactionid) {
        this.sr_transactionid = sr_transactionid;
    }

    public String getSr_date() {
        return sr_date;
    }

    public void setSr_date(String sr_date) {
        this.sr_date = sr_date;
    }

    public String getSr_payment() {
        return sr_payment;
    }

    public void setSr_payment(String sr_payment) {
        this.sr_payment = sr_payment;
    }

    public String getSr_product() {
        return sr_product;
    }

    public void setSr_product(String sr_product) {
        this.sr_product = sr_product;
    }

    // Static method to generate a transaction ID
    // Static method to generate a shorter transaction ID by truncating UUID
    public static String generateTransactionId() {
        // Use UUID to generate a unique ID and truncate to the first 8 characters
        String uuid = UUID.randomUUID().toString();
        return uuid.substring(0, 8);
    }
}