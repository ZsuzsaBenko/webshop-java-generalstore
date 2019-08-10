package com.codecool.shop.controller;

import com.codecool.shop.dao.UserDao;
import com.codecool.shop.dao.implementation.UserDaoDb;
import org.thymeleaf.context.WebContext;

import javax.servlet.http.HttpServletRequest;

public class ControllerUtil {
    private static UserDao userDao = UserDaoDb.getInstance();

    public static void setNavbarParameters(HttpServletRequest request, WebContext context) {
        String email = (String) request.getSession().getAttribute("email");
        if (email != null) {
            context.setVariable("status", "logged-in");
            String name = userDao.find(email).getName();
            context.setVariable("name", name);
        }
    }
}
