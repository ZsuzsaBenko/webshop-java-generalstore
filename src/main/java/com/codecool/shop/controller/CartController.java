package com.codecool.shop.controller;

import com.codecool.shop.config.TemplateEngineUtil;
import com.codecool.shop.dao.ProductDao;
import com.codecool.shop.dao.implementation.ProductDaoDb;
import com.codecool.shop.model.Product;
import com.codecool.shop.model.order.Order;
import com.codecool.shop.model.order.ShoppingCart;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/cart"})
public class CartController extends HttpServlet {
    private ProductDao productDataStore = ProductDaoDb.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        TemplateEngine engine = TemplateEngineUtil.getTemplateEngine(request.getServletContext());
        WebContext context = new WebContext(request, response, request.getServletContext());

        ShoppingCart cart = modifyShoppingCartContent(request);

        Map<Product, Integer> cartContent = getShoppingCartWithObjectKeys(cart);

        String url = (String) request.getSession().getAttribute("url");

        ControllerUtil.setUserParameters(request, context);

        context.setVariable("cartMap", cart != null ? cartContent : new LinkedHashMap<>());
        context.setVariable("cartValue", cart != null ? String.format("%s Talentum", cart.getTotalPrice()) : "");
        context.setVariable("previousURL", url == null ? "/" : url);
        engine.process("order/cart", context, response.getWriter());
    }

    private ShoppingCart modifyShoppingCartContent(HttpServletRequest request) {
        String addId = request.getParameter("add");
        String removeId = request.getParameter("remove");

        Order order = (Order) request.getSession().getAttribute("order");
        ShoppingCart cart = null;

        if (order != null) {
            cart = order.getShoppingCart();

            if (addId != null) {
                cart.addProduct(Integer.parseInt(addId));
            }
            if (removeId != null) {
                cart.removeProduct(Integer.parseInt(removeId));
            }
        }
        return cart;
    }

    private Map<Product, Integer> getShoppingCartWithObjectKeys(ShoppingCart cart) {
        Map<Product, Integer> cartContent = new LinkedHashMap<>();
        if (cart != null) {
            for (Integer prodId : cart.getCart().keySet()) {
                Product product = productDataStore.find(prodId);
                int amount = cart.getCart().get(prodId);
                cartContent.put(product, amount);
            }
        }
        return cartContent;
    }
}