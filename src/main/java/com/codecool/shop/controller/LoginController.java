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
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(urlPatterns = {"/login"})
public class LoginController extends HttpServlet {

    private UserDao userDao = UserDaoDb.getInstance();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(true);

        String email = request.getParameter("login-email");
        String password = request.getParameter("login-password");

        try {
            User user = userDao.find(email);
            String hashedPassword = user.getPassword();
            if (BCrypt.checkpw(password, hashedPassword)) {
                session.setAttribute("email", email);
                session.setAttribute("invalidLogin", "false");
            } else {
                session.setAttribute("invalidLogin", "true");
            }
        } catch (NullPointerException e) {
            session.setAttribute("invalidLogin", "true");
        } finally {
            response.sendRedirect("/");

        }
    }
}

