package com.codecool.shop.controller;

import com.codecool.shop.dao.UserDao;
import com.codecool.shop.dao.implementation.UserDaoDb;
import com.codecool.shop.model.User;
import org.mindrot.jbcrypt.BCrypt;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/register"})
public class RegistrationController  extends HttpServlet {
    private UserDao userDao = UserDaoDb.getInstance();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        String generatedSecuredPasswordHash = BCrypt.hashpw(password, BCrypt.gensalt(12));

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(generatedSecuredPasswordHash);

        userDao.addNewUser(user);

        response.sendRedirect("/");
    }
}
