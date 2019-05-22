package com.codecool.shop.dao;

import com.codecool.shop.model.User;

public interface UserDao {

    void addNewUser(User user);
    void updateOrderData(int id);
    User find (int id);
    void updateNameAndEmail(int id);

}
