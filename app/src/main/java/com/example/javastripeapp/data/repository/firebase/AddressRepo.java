package com.example.javastripeapp.data.repository.firebase;

import com.example.javastripeapp.data.Address;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;

public class AddressRepo {
    private final GenericReference<Address> addressRef;

    public AddressRepo() {
        this.addressRef = new GenericReference<>("addresses", Address.class);
    }

    private Task<Void> createAddress(Address address) {
        return addressRef.createNewObject(address);
    }

    private Task<Void> createAddresses(List<Address> addresses) {
        Task<Void> chain = Tasks.forResult(null);

        for (Address address : addresses) {
            chain = chain.continueWithTask(task -> createAddress(address));
        }
        return chain;
    }

    public Task<Address> fetchAddress(String addressId) {
        return addressRef.getObject(addressId);
    }

    public Task<List<Address>> fetchUserAddresses(String userId) {
        return addressRef.findByField("userId", userId);
    }


    public Task<Void> createCustomerAddresses(String userId) {

        List<Address> addressList = new ArrayList<>();

        String a1Line1 = "20 Vanessa Pl";
        String a1City = "Whitby";
        String a1Province = "ON";
        String a1PostalCode = "L1N6T3";

        Address address1 = new Address(a1Line1, a1City, a1Province, a1PostalCode, userId);

        String a2Line1 = "200 Sun Valley Drive";
        String a2City = "Winnipeg";
        String a2Province = "MB";
        String a2PostalCode = "R2G2W7";

        Address address2 = new Address(a2Line1, a2City, a2Province, a2PostalCode, userId);

        String a3Line1 = "1025 Boundary Road";
        String a3City = "Vancouver";
        String a3Province = "BC";
        String a3PostalCode = "V5K4T2";

        Address address3 = new Address(a3Line1, a3City, a3Province, a3PostalCode, userId);

        addressList.add(address1);
        addressList.add(address2);
        addressList.add(address3);

        return createAddresses(addressList);
    }

    public Task<Void> createProviderAddresses(String userId) {
        List<Address> addressList = new ArrayList<>();

        String a1Line1 = "100 High Point Drive";
        String a1City = "Winnipeg";
        String a1Province = "MB";
        String a1PostalCode = "R2G3R4";

        Address address1 = new Address(a1Line1, a1City, a1Province, a1PostalCode, userId);

        String a2Line1 = "101 Boundary Rd";
        String a2City = "Vancouver";
        String a2Province = "BC";
        String a2PostalCode = "V5K4R6";

        Address address2 = new Address(a2Line1, a2City, a2Province, a2PostalCode, userId);

        String a3Line1 = "24 Dundas St W";
        String a3City = "Whitby";
        String a3Province = "ON";
        String a3PostalCode = "L1N2L9";

        Address address3 = new Address(a3Line1, a3City, a3Province, a3PostalCode, userId);

        addressList.add(address1);
        addressList.add(address2);
        addressList.add(address3);

        return createAddresses(addressList);
    }
}
