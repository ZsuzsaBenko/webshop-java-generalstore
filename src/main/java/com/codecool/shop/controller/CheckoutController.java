package com.codecool.shop.controller;

import com.codecool.shop.config.TemplateEngineUtil;
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
import java.util.Map;

@WebServlet(urlPatterns = {"/checkout"})
public class CheckoutController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        TemplateEngine engine = TemplateEngineUtil.getTemplateEngine(request.getServletContext());
        WebContext context = new WebContext(request, response, request.getServletContext());

        ControllerUtil.setUserParameters(request, context);
        response.setCharacterEncoding("UTF-8");
        engine.process("order/checkout", context, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String country = request.getParameter("country");
        String zipcode = request.getParameter("zipcode");
        String city = request.getParameter("city");
        String street = request.getParameter("street");
        String doorNumber = request.getParameter("doorNumber");
        String sCountry = request.getParameter("sCountry");
        String sZipcode = request.getParameter("sZipcode");
        String sCity = request.getParameter("sCity");
        String sStreet = request.getParameter("sStreet");
        String sDoorNumber = request.getParameter("sDoorNumber");

        HttpSession session = request.getSession();

        Order order = (Order) session.getAttribute("order");

        if (order != null) {
            order.setName(name);
            order.setEmailAddress(email);
            order.setPhoneNumber(phone);

            Map<String, String> billingAddress = order.getBillingAddress();
            billingAddress.put("country", country);
            billingAddress.put("zipcode", zipcode);
            billingAddress.put("city", city);
            billingAddress.put("street", street);
            billingAddress.put("doorNumber", doorNumber);

            Map<String, String> shippingAddress = order.getShippingAddress();
            shippingAddress.put("country", sCountry);
            shippingAddress.put("zipcode", sZipcode);
            shippingAddress.put("city", sCity);
            shippingAddress.put("street", sStreet);
            shippingAddress.put("doorNumber", sDoorNumber);

            order.setPaymentStatus(PaymentStatus.PROCESSED);

            response.sendRedirect("/payment");
        } else {
            response.sendRedirect("/cart");
        }


    }


}
