package com.example.javastripeapp.data.models.address;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Address {
    private String addressId;
    private String line1;
    private String city;
    private String province;
    private String postalCode;
    private String userId;

    public Address() {

    }

    public Address(String line1, String city, String province, String postalCode, String userId) {
        this.line1 = line1;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
        this.userId = userId;
    }

    public Map<String, Object> toStripeAddressMap() {
        Map<String, Object> address = new HashMap<>();
        address.put("line1", this.line1);
        address.put("city", this.city);
        address.put("state", this.province);
        address.put("postal_code", this.postalCode);
        address.put("country", "CA");
        return address;
    }

    @Exclude
    public String getFormattedAddress() {
        return line1 + ", " + city + ", " + province + " " + postalCode;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getLine1() {
        return line1;
    }

    public void setLine1(String line1) {
        this.line1 = line1;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @NonNull
    @Override
    public String toString() {
        return getFormattedAddress();
    }
}
