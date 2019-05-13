package com.codecool.shop.controller;

//import ch.qos.logback.classic.Level;
import com.codecool.shop.dao.ProductCategoryDao;
import com.codecool.shop.dao.ProductDao;
import com.codecool.shop.dao.SupplierDao;
import com.codecool.shop.dao.implementation.ProductCategoryDaoDb;
import com.codecool.shop.dao.implementation.ProductDaoDb;
import com.codecool.shop.config.TemplateEngineUtil;
import com.codecool.shop.dao.implementation.SupplierDaoDb;
import com.codecool.shop.model.Product;
import com.codecool.shop.model.ProductCategory;
import com.codecool.shop.model.Supplier;
import com.codecool.shop.model.order.Order;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(urlPatterns = {"/"})
public class ProductController extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);
    private ProductDao productDataStore = ProductDaoDb.getInstance();
    private ProductCategoryDao productCategoryDataStore = ProductCategoryDaoDb.getInstance();
    private final ProductCategory defaultCategory = productCategoryDataStore.find(1);
    private SupplierDao supplierDao = SupplierDaoDb.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        TemplateEngine engine = TemplateEngineUtil.getTemplateEngine(request.getServletContext());
        WebContext context = new WebContext(request, response, request.getServletContext());

        HttpSession session = request.getSession(true);

        String categoryName = request.getParameter("category");
        String supplierName = request.getParameter("supplier");

        List<Product> products = selectProducts(categoryName, supplierName, session);
        Map<String, Object> parameters = getServletParameters(categoryName, supplierName, products);

        String addId = request.getParameter("item_id");

        if (addId != null) {
            addToCart(addId, session);
            String url = (String) request.getSession().getAttribute("url");

            if (url == null) {
                url = "/";
            }
            LOGGER.warn("User will be redirected to {}.", url);
            response.sendRedirect(url);
        } else {
            parameters.forEach(((key, value) -> context.setVariable(String.valueOf(key), value)));
            response.setCharacterEncoding("UTF-8");
            engine.process("product/index", context, response.getWriter());
        }
    }

    private void addToCart(String addId, HttpSession session) {
        Order order = (Order) session.getAttribute("order");

        if (order == null) {
            LOGGER.info("User created new order.");
            order = new Order();
        } else {
            order = (Order) session.getAttribute("order");
        }


        if (addId != null) {
            if (Integer.parseInt(addId) < 1 || Integer.parseInt(addId) > getProductNumber()) {
                LOGGER.error("Invalid id: {}!", addId);
            } else {
                order.getShoppingCart().addProduct(Integer.parseInt(addId));
                LOGGER.info("User added item to shopping cart. Number of items in shopping card is {}.",
                        order.getShoppingCart().getCart().size());
            }
        }

        session.setAttribute("order", order);
    }

    private List<Product> selectProducts(String categoryName, String supplierName, HttpSession session) {
        List<Product> products;

        //setLoggingLevel(Level.DEBUG);

        ProductCategory category = productCategoryDataStore.find(categoryName);
        Supplier supplier = supplierDao.find(supplierName);

        if (category == null && supplier == null) {
            products = productDataStore.getBy(defaultCategory);
            LOGGER.debug("Results filtered by default category: {}", defaultCategory.getName());
        } else if (category == null) {
            products = productDataStore.getBy(supplier);
            LOGGER.debug("Results filtered by supplier: {}", supplier.getName());
        } else if (supplier == null) {
            products = productDataStore.getBy(category);
            LOGGER.debug("Results filtered by category: {}", category.getName());
        } else {
            products = productDataStore.getProducts(categoryName, supplierName);
            LOGGER.debug("Results filtered by category: {} and supplier: {}", category.getName(), supplier.getName());
        }
        if (categoryName != null || supplierName != null) {
            session.setAttribute("url", (String.format("/?category=%s&supplier=%s", categoryName, supplierName)));
        }

        return products;
    }

    private HashMap<String, Object> getServletParameters(String categoryName, String supplierName, List<Product> products) {
        HashMap<String, Object> parameters = new HashMap<>();

        ProductCategory category = productCategoryDataStore.find(categoryName);
        Supplier supplier = supplierDao.find(supplierName);

        if (category == null && supplier == null) {
            parameters.put("filter", String.format("%s Generals", defaultCategory.getName()));
        } else if (category == null) {
            parameters.put("filter", String.format("Generals from %s", supplier.getName()));
        } else if (supplier == null) {
            parameters.put("filter", String.format("%s Generals", category.getName()));
        } else {
            parameters.put("filter", String.format("%s Generals from %s", category.getName(), supplier.getName()));
        }

        parameters.put("products", products);

        if (products.size() == 0) {
            parameters.put("filter", "No results found.");
        }

        parameters.put("selectedCateg", categoryName);
        parameters.put("selectedSupplier", supplierName);
        parameters.put("suppliers", supplierDao.getAll());
        parameters.put("categories", productCategoryDataStore.getAll());

        return parameters;
    }

    private int getProductNumber() {
        return productDataStore.getAll().size();
    }

    /*private static void setLoggingLevel(ch.qos.logback.classic.Level level) {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(level);
    }*/
}
