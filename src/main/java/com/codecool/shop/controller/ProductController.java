package com.codecool.shop.controller;

import com.codecool.shop.dao.ProductCategoryDao;
import com.codecool.shop.dao.ProductDao;
import com.codecool.shop.dao.SupplierDao;
import com.codecool.shop.dao.UserDao;
import com.codecool.shop.dao.implementation.ProductCategoryDaoDb;
import com.codecool.shop.dao.implementation.ProductDaoDb;
import com.codecool.shop.config.TemplateEngineUtil;
import com.codecool.shop.dao.implementation.SupplierDaoDb;
import com.codecool.shop.dao.implementation.UserDaoDb;
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

@WebServlet(urlPatterns = {"/"})
public class ProductController extends HttpServlet {
    private ProductDao productDataStore = ProductDaoDb.getInstance();
    private ProductCategoryDao productCategoryDataStore = ProductCategoryDaoDb.getInstance();
    private final ProductCategory defaultCategory = productCategoryDataStore.find(1);
    private SupplierDao supplierDao = SupplierDaoDb.getInstance();
    private UserDao userDao = UserDaoDb.getInstance();

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

        handleLogin(session, parameters);

        String addId = request.getParameter("item_id");

        if (addId != null) {
            addToCart(addId, session);
            String url = (String) request.getSession().getAttribute("url");

            if (url == null) {
                url = "/";
            }
            response.sendRedirect(url);
        } else {
            parameters.forEach(((key, value) -> context.setVariable(String.valueOf(key), value)));
            response.setCharacterEncoding("UTF-8");
            engine.process("product/index", context, response.getWriter());
        }
    }

    private void handleLogin(HttpSession session, Map<String, Object> parameters) {
        String invalidLogin = (String) session.getAttribute("invalidLogin");
        String email = (String) session.getAttribute("email");
        if (email != null) {
            parameters.put("status", "logged-in");
            String name = userDao.find(email).getName();
            parameters.put("name", name);
            if (invalidLogin.equals("true")) {
                session.removeAttribute(invalidLogin);
                parameters.put("invalidLogin", "false");
            }
        }
        parameters.put("invalidLogin", invalidLogin);
    }

    private void addToCart(String addId, HttpSession session) {
        Order order = (Order) session.getAttribute("order");

        if (order == null) {
            order = new Order();
        } else {
            order = (Order) session.getAttribute("order");
        }

        if (addId != null) {
            order.getShoppingCart().addProduct(Integer.parseInt(addId));
        }

        session.setAttribute("order", order);
    }

    private List<Product> selectProducts(String categoryName, String supplierName, HttpSession session) {
        List<Product> products;

        ProductCategory category = productCategoryDataStore.find(categoryName);
        Supplier supplier = supplierDao.find(supplierName);

        if (category == null && supplier == null) {
            products = productDataStore.getBy(defaultCategory);
        } else if (category == null) {
            products = productDataStore.getBy(supplier);
        } else if (supplier == null) {
            products = productDataStore.getBy(category);
        } else {
            products = productDataStore.getProducts(categoryName, supplierName);
        }
        if (categoryName != null || supplierName != null) {
            session.setAttribute("url", (String.format("/?category=%s&supplier=%s", categoryName, supplierName)));
        }

        return products;
    }

    private Map<String, Object> getServletParameters(String categoryName, String supplierName, List<Product> products) {
        Map<String, Object> parameters = new HashMap<>();

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
}
