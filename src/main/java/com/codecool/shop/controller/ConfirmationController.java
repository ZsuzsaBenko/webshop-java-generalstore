package com.codecool.shop.controller;

import com.codecool.shop.config.TemplateEngineUtil;
import com.codecool.shop.dao.implementation.ProductDaoDb;
import com.codecool.shop.model.Product;
import com.codecool.shop.model.order.Order;
import com.codecool.shop.model.order.PaymentStatus;
import com.codecool.shop.model.order.ShoppingCart;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

@WebServlet(urlPatterns = {"/confirm"})
public class ConfirmationController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        TemplateEngine engine = TemplateEngineUtil.getTemplateEngine(request.getServletContext());
        WebContext context = new WebContext(request, response, request.getServletContext());

        request.getSession().removeAttribute("order");

        ControllerUtil.setUserParameters(request, context);
        engine.process("order/confirmation", context, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Order order = (Order) request.getSession().getAttribute("order");
        PaymentStatus currentStatus = order.getPaymentStatus();

        if (currentStatus.getNext() == PaymentStatus.PAID) {
            ShoppingCart cart = order.getShoppingCart();
            String to = order.getEmailAddress();
            String subject = "General Shop - Order confirmation";
            String body = createEmailBody(cart);

            String host = "smtp.gmail.com";
            String from = System.getenv("GMUS");
            String pass = System.getenv("GMPW");

            Properties props = getProperties(host, from, pass);

            Session session = Session.getDefaultInstance(props);
            MimeMessage message = new MimeMessage(session);

            try {
                message.setFrom(new InternetAddress(from));
                InternetAddress toAddress = new InternetAddress(to);

                message.addRecipient(Message.RecipientType.TO, toAddress);

                message.setSubject(subject);
                message.setContent(body, "text/html");

                Transport transport = session.getTransport("smtp");
                transport.connect(host, from, pass);
                transport.sendMessage(message, message.getAllRecipients());
                transport.close();
            } catch (MessagingException ae) {
                ae.printStackTrace();
            }

            order.setPaymentStatus(PaymentStatus.PAID);

            response.sendRedirect("/confirm");
        }
    }

    private Properties getProperties(String host, String from, String pass) {
        Properties props = System.getProperties();

        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", from);
        props.put("mail.smtp.password", pass);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");

        return props;
    }

    private String createEmailBody(ShoppingCart cart) {
        String generals = getGeneralsString(cart);
        String total = String.valueOf(cart.getTotalPrice());

        return "<!doctype html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Confirmation</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<h1>Your order was successful.</h1>\n" +
                "<p>We will deliver you the following generals:</p>\n" +
                "    <div class=\"card-text\" id=\"cart-container\">\n" +
                "        <table class=\"table\" id=\"cart-content-table\">\n" +
                "            <thead>\n" +
                "                <tr>\n" +
                "                    <th>Name</th>\n" +
                "                    <th>Amount</th>\n" +
                "                    <th>Price</th>\n" +
                "                </tr>\n" +
                "            </thead>\n" +
                "            <tbody>" + generals +
                "               <tr></tr>" +
                "               <tr>\n" +
                "                    <td><span style=\"font-weight: bold\">Total Price:</span></td>\n" +
                "                    <td></td>\n" +
                "                    <td><span style=\"font-weight: bold\">" + total + " Talentum</span></td>\n" +
                "                </tr>\n" +
                "            </tbody>\n" +
                "        </table>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    private String getGeneralsString(ShoppingCart cart) {
        Map<Product, Integer> finalCart = getShoppingCartWithObjectKeys(cart);

        StringBuilder generals = new StringBuilder();
        for (Product general : finalCart.keySet()) {
            generals.append("<tr>\n" +
                    "<td>" + general.getName() + "</td>\n" +
                    "<td><span>" + finalCart.get(general) + "</span></td>\n" +
                    "<td>" + (general.getDefaultPrice() * finalCart.get(general)) +
                    " Talentum</td>\n" +
                    "</tr>\n");
        }

        return generals.toString();
    }

    private Map<Product, Integer> getShoppingCartWithObjectKeys(ShoppingCart cart) {
        Map<Product, Integer> cartContent = new LinkedHashMap<>();
        if (cart != null) {
            for (Integer prodId : cart.getCart().keySet()) {
                Product product = ProductDaoDb.getInstance().find(prodId);
                int amount = cart.getCart().get(prodId);
                cartContent.put(product, amount);
            }
        }
        return cartContent;
    }
}
