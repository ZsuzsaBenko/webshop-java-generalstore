package com.codecool.shop.model.order;

import com.codecool.shop.model.User;

import java.util.HashMap;
import java.util.Map;

public class Order {
    private User user;
    private Map<String, String> shippingAddress = new HashMap<>();
    private ShoppingCart shoppingCart = new ShoppingCart();
    private PaymentStatus paymentStatus = PaymentStatus.NEW;

    public String getName() {
        return user.getName();
    }

    public String getEmailAddress() {
        return user.getEmail();
    }

    public String getPhoneNumber() {
        return user.getPhoneNumber();
    }

    public Map<String, String> getBillingAddress() {
        return user.getBillingAddress();
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Map<String, String> getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(Map<String, String> shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public ShoppingCart getShoppingCart() {
        return shoppingCart;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

}
