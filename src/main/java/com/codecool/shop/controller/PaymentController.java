package com.codecool.shop.controller;

import com.codecool.shop.config.TemplateEngineUtil;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@WebServlet(urlPatterns = {"/payment"})
public class PaymentController extends HttpServlet {
    private static final List<String> PAYMENT_OPTIONS = new ArrayList<>(Arrays.asList("Slave", "Life", "Talent"));

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        TemplateEngine engine = TemplateEngineUtil.getTemplateEngine(request.getServletContext());
        WebContext context = new WebContext(request, response, request.getServletContext());

        String email = (String) request.getSession().getAttribute("email");
        if (email != null) {
            context.setVariable("status", "logged-in");
        }
        context.setVariable("paymentOptions", PAYMENT_OPTIONS);
        engine.process("order/payment", context, response.getWriter());
    }
}
