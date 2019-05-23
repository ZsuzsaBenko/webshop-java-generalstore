package com.codecool.shop.dao;

import com.codecool.shop.model.User;

public interface UserDao {

    void addNewUser(User user);
    void updateUserData(String email, User user);
    User find (String email);
    void removeUser(String email);

}
