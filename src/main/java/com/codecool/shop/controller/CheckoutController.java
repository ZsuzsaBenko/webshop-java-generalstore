package com.codecool.shop.controller;

import com.codecool.shop.config.TemplateEngineUtil;
import com.codecool.shop.dao.UserDao;
import com.codecool.shop.dao.implementation.UserDaoDb;
import com.codecool.shop.model.User;
import com.codecool.shop.model.order.Order;
import com.codecool.shop.model.order.PaymentStatus;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/checkout"})
public class CheckoutController extends HttpServlet {
    private UserDao userDao = UserDaoDb.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        TemplateEngine engine = TemplateEngineUtil.getTemplateEngine(request.getServletContext());
        WebContext context = new WebContext(request, response, request.getServletContext());

        HttpSession session = request.getSession();
        String email = (String) session.getAttribute("email");
        User user = userDao.find(email);

        ControllerUtil.setNavbarParameters(request, context);
        Map<String, Object> parameters = getParameters(user);
        context.setVariable("user", parameters);
        response.setCharacterEncoding("UTF-8");
        engine.process("order/checkout", context, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();

        String name = request.getParameter("name");
        String email = (String) session.getAttribute("email");
        String phone = request.getParameter("phone");
        String country = request.getParameter("country");
        String zipcode = request.getParameter("zipcode");
        String city = request.getParameter("city");
        String street = request.getParameter("street");
        String number = request.getParameter("number");
        String sCountry = request.getParameter("sCountry");
        String sZipcode = request.getParameter("sZipcode");
        String sCity = request.getParameter("sCity");
        String sStreet = request.getParameter("sStreet");
        String sNumber = request.getParameter("sNumber");

        User user = userDao.find(email);

        user.setName(name);
        user.setPhoneNumber(phone);
        Map<String, String> newBillingAddress = new HashMap<>();
        newBillingAddress.put("country", country);
        newBillingAddress.put("zipcode", zipcode);
        newBillingAddress.put("city", city);
        newBillingAddress.put("street", street);
        newBillingAddress.put("number", number);
        user.setBillingAddress(newBillingAddress);

        userDao.updateUserData(email, user);

        Order order = (Order) session.getAttribute("order");

        if (order != null) {
            order.setUser(user);

            Map<String, String> shippingAddress = order.getShippingAddress();
            shippingAddress.put("country", sCountry);
            shippingAddress.put("zipcode", sZipcode);
            shippingAddress.put("city", sCity);
            shippingAddress.put("street", sStreet);
            shippingAddress.put("number", sNumber);

            order.setPaymentStatus(PaymentStatus.PROCESSED);

            response.sendRedirect("/payment");
        } else {
            response.sendRedirect("/cart");
        }


    }

    private Map<String, Object> getParameters(User user) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", user.getName());
        parameters.put("email", user.getEmail());
        parameters.put("phone", user.getPhoneNumber());
        parameters.put("country", user.getBillingAddress().get("country"));
        parameters.put("zipcode", user.getBillingAddress().get("zipcode"));
        parameters.put("city", user.getBillingAddress().get("city"));
        parameters.put("street", user.getBillingAddress().get("street"));
        parameters.put("number", user.getBillingAddress().get("number"));
        return parameters;
    }


}
