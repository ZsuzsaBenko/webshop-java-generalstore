package com.codecool.shop.dao.implementation;

import com.codecool.shop.dao.UserDao;
import com.codecool.shop.model.User;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class UserDaoDb implements UserDao {

    private static final String DATABASE = System.getenv("DATABASE");
    private static final String DBUSER = System.getenv("USER");
    private static final String DBPASSWORD = System.getenv("PASSWORD");
    private static UserDaoDb instance = null;

    private UserDaoDb() {
    }

    public static UserDaoDb getInstance() {
        if (instance == null) {
            instance = new UserDaoDb();
        }
        return instance;
    }

    @Override
    public void addNewUser(User user) {
        String sql = "INSERT INTO users (name, email, password) VALUES (?,?,?);";
        try (Connection connection = DriverManager.getConnection(DATABASE, DBUSER, DBPASSWORD)) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database not found", e);
        }
    }

    @Override
    public User find(String email) {
        String sql = "SELECT * FROM users WHERE email=?;";
        User user = null;
        try (Connection connection = DriverManager.getConnection(DATABASE, DBUSER, DBPASSWORD)) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();
            Map<String, String> billingAddress = new HashMap<>();
            if (rs.next()) {
                user = new User();
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setPhoneNumber(rs.getString("telephone"));
                billingAddress.put("country", rs.getString("country"));
                billingAddress.put("zipcode", rs.getString("zipcode"));
                billingAddress.put("city", rs.getString("city"));
                billingAddress.put("street", rs.getString("street"));
                billingAddress.put("number", rs.getString("number"));
                user.setBillingAddress(billingAddress);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database not found", e);
        }
        return user;
    }

    @Override
    public void updateUserData(String email) {
        User user = find(email);
        String sql = "UPDATE users SET name=?, telephone=?, country=?, zipcode=?, city=?, street=? , number=?" +
                "WHERE email=?";
        try (Connection connection = DriverManager.getConnection(DATABASE, DBUSER, DBPASSWORD)) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, user.getName());
            statement.setString(2, user.getPhoneNumber());
            statement.setString(3, user.getBillingAddress().get("country"));
            statement.setString(4, user.getBillingAddress().get("zipcode"));
            statement.setString(5, user.getBillingAddress().get("city"));
            statement.setString(6, user.getBillingAddress().get("street"));
            statement.setString(7, user.getBillingAddress().get("number"));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database not found", e);
        }

    }
}
