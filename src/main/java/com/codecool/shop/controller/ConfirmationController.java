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

        ControllerUtil.setNavbarParameters(request, context);
        response.setCharacterEncoding("UTF-8");
        engine.process("order/confirmation", context, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Order order = (Order) request.getSession().getAttribute("order");
        PaymentStatus currentStatus = order.getPaymentStatus();

        if (currentStatus.getNext() == PaymentStatus.PAID) {
            ShoppingCart cart = order.getShoppingCart();
            Map<String, String> billingAddress = order.getBillingAddress();
            Map<String, String> shippingAddress = order.getShippingAddress();
            String name = order.getName();
            String to = order.getEmailAddress();
            String subject = "General Store - Order confirmation";
            String body = createEmailBody(cart, name, billingAddress, shippingAddress);

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
                message.setContent(body, "text/html; charset=UTF-8");

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

    private String createEmailBody(ShoppingCart cart, String name, Map<String, String> billingAddress, Map<String, String> shippingAddress) {
        String address = getAddressString(billingAddress, shippingAddress);
        String generals = getGeneralsString(cart);
        String total = String.valueOf(cart.getTotalPrice());

        return "<!doctype html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Confirmation</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<h2>Dear " + name + ", </h2>" +
                "<h2>Your order was successful.</h2>\n" +
                "<div class=\"card-text\">\n" +
                "   <table class=\"table\" border=\"solid\" border-size=1 text-align=\"left\" padding=5 border-collapse=\"collapse\">\n" +
                "       <thead text-align=\"left\">\n" +
                "           <tr>\n" +
                "               <th>Billing address:</th>\n" +
                "               <th>Shipping address:</th>\n" +
                "           </tr>\n" +
                "       </thead>\n" +
                "       <tbody>" + address +
                "           <tr></tr>" +
                "       </tbody>" +
                "   </table>" +
                "</div>" +
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

    private String getAddressString(Map<String, String> bAddress, Map<String, String> sAddress) {
        StringBuilder addressString = new StringBuilder();
        addressString.append(
                "<tr>\n" +
                "   <td>" + bAddress.get("city") + "</td>\n" +
                "   <td>" + sAddress.get("city") + "</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "   <td>" + bAddress.get("street") + " " + bAddress.get("number") + "</td>\n" +
                "   <td>" + sAddress.get("street") + " " + sAddress.get("number") + "</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "   <td>" + bAddress.get("country") + "</td>\n" +
                "   <td>" + sAddress.get("country") + "</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "   <td>" + bAddress.get("zipcode") + "</td>\n" +
                "   <td>" + sAddress.get("zipcode") + "</td>\n" +
                "</tr>\n");
        return addressString.toString();
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
