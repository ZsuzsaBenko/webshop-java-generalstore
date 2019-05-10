package com.codecool.shop.dao.implementation;


import com.codecool.shop.dao.ProductDao;
import com.codecool.shop.model.Product;
import com.codecool.shop.model.ProductCategory;
import com.codecool.shop.model.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDaoDb implements ProductDao {

    private static final String DATABASE = System.getenv("DATABASE");
    private static final String DBUSER = System.getenv("USER");
    private static final String DBPASSWORD = System.getenv("PASSWORD");
    private static ProductDaoDb instance = null;

    /* A private Constructor prevents any other class from instantiating.
     */
    private ProductDaoDb() {
    }

    public static ProductDaoDb getInstance() {
        if (instance == null) {
            instance = new ProductDaoDb();
        }
        return instance;
    }

    private List<Product> executeQuery(String query) {
        List<Product> products = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DATABASE, DBUSER, DBPASSWORD)) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Product product = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getFloat("default_price"),
                        rs.getString("currency"),
                        rs.getString("description"),
                        ProductCategoryDaoDb.getInstance().find(rs.getInt("product_category")),
                        SupplierDaoDb.getInstance().find(rs.getInt("supplier")));
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database not found", e);
        }
        return products;
    }

    @Override
    public void add(Product product) {
        String sql = "INSERT INTO product (id, name, default_price, currency, description, supplier, product_category) " +
                    "VALUES (?,?,?,?,?,?,?);";
        try (Connection connection = DriverManager.getConnection(DATABASE, DBUSER, DBPASSWORD)) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, product.getId());
            statement.setString(2, product.getName());
            statement.setFloat(3, product.getDefaultPrice());
            statement.setString(4, product.getDefaultCurrency());
            statement.setString(5, product.getDescription());
            statement.setInt(6, SupplierDaoDb.getInstance()
                    .find(product.getSupplier().getName()).getId());
            statement.setInt(7, ProductCategoryDaoDb.getInstance()
                    .find(product.getProductCategory().getName()).getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database not found", e);
        }
    }

    private PreparedStatement createFindStatement(Connection connection, String sql, int id) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, id);
        return preparedStatement;
    }

    @Override
    public Product find(int id) {
        String sql = "SELECT * FROM product WHERE id=?;";
        Product product = null;
        try (Connection connection = DriverManager.getConnection(DATABASE, DBUSER, DBPASSWORD);
             PreparedStatement statement = createFindStatement(connection, sql, id);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                product = new Product(rs.getInt("id"),
                                      rs.getString("name"),
                                      rs.getFloat("default_price"),
                                      rs.getString("currency"),
                                      rs.getString("description"),
                                      ProductCategoryDaoDb.getInstance().find(rs.getInt("product_category")),
                                      SupplierDaoDb.getInstance().find(rs.getInt("supplier")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database not found", e);
        }

        return product;
    }

    @Override
    public void remove(int id) {
        String sql = "DELETE FROM product WHERE id = ?;";
        try (Connection connection = DriverManager.getConnection(DATABASE, DBUSER, DBPASSWORD)) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database not found", e);
        }
    }

    @Override
    public List<Product> getAll() {
        String sql = "SELECT * FROM product;";
        return executeQuery(sql);
    }

    @Override
    public List<Product> getBy(Supplier supplier) {
        int id = supplier.getId();
        String sql = "SELECT * FROM product WHERE supplier = " + id +";";
        return executeQuery(sql);
    }

    @Override
    public List<Product> getBy(ProductCategory productCategory) {
        int id = productCategory.getId();
        String sql = "SELECT * FROM product WHERE product_category = " + id +";";
        return executeQuery(sql);
    }

    @Override
    public List<Product> getProducts(String category, String supplier) throws NullPointerException{
        String sql = "";
        try {
            int supplierId = SupplierDaoDb.getInstance().find(supplier).getId();
            int categoryId = ProductCategoryDaoDb.getInstance().find(category).getId();
            sql = "SELECT * FROM product WHERE supplier = " + supplierId +
                    " AND product_category = " + categoryId +";";
        } catch (NullPointerException e){
            throw new NullPointerException("Invalid category or supplier.");
        }
        return executeQuery(sql);
    }
}

//getProducts methods fixed: they find the filtered products by the foreign keys.
// when new Product is instantiated, id is added 8from database)
