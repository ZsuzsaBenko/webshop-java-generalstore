import com.codecool.shop.dao.ProductCategoryDao;
import com.codecool.shop.dao.ProductDao;
import com.codecool.shop.dao.SupplierDao;
import com.codecool.shop.dao.UserDao;
import com.codecool.shop.dao.implementation.*;
import com.codecool.shop.model.Product;
import com.codecool.shop.model.ProductCategory;
import com.codecool.shop.model.Supplier;
import com.codecool.shop.model.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class reDaoTest {

    //These will be initialized before the tests run.
    private ProductCategory testCategory;
    private Supplier testSupplier;
    private Product testProduct;

    private ProductCategory unusedCategory;
    private Supplier unusedSupplier;

    private Supplier falseSupplier;
    private ProductCategory falseCategory;

    private User testUser;
    private User testUser2;

    //Create a streams that hold an instance of every class that implements ProductCategoryDao / SupplierDao and ProductDao.
    private static Stream<ProductCategoryDao> getCategoryClasses() {
        return Stream.of(
                ProductCategoryDaoMem.getInstance(),
                ProductCategoryDaoDb.getInstance());
    }

    private static Stream<SupplierDao> getSupplierClasses() {
        return Stream.of(
                SupplierDaoMem.getInstance(),
                SupplierDaoDb.getInstance());
    }

    private static Stream<ProductDao> getProductClasses() {
        return Stream.of(
                ProductDaoMem.getInstance(),
                ProductDaoDb.getInstance());
    }

    private static Stream<UserDao> getUserClasses(){
        return Stream.of(UserDaoDb.getInstance());
    }

    /*
    Initialize a ProductCategory, a Supplier and a Product with these attributes:
    We also needed a category and a supplier that are not added to the db and memory.
    And also one of each that are added to the db and memory, but are not used by any Product.
    */
    @BeforeAll
    public void setUp(){
        testCategory = new ProductCategory(400, "RomanCategory", "Lieutenant", "Let's go");
        testSupplier = new Supplier(500, "RomanSupplier", "Colonel");
        testProduct = new Product(100, "RomanMan", 1700, "TAL", "A man", testCategory, testSupplier);
        testUser= new User();
        testUser.setName("Yolo Yoco");
        testUser.setEmail("Yolo@yolomail.com");
        testUser.setPassword("Yolo");

        testUser2 = new User();
        testUser2.setEmail("1234@1234.com");
        testUser2.setPassword("1234");
        testUser2.setName("1234");

        falseCategory = new ProductCategory(401, "FalseCategory", "FalseLieutenant", "Let's go");
        falseSupplier = new Supplier(501, "FalseSupplier", "FalseColonel");

        unusedCategory = new ProductCategory(402, "Unused", "Unkown", "Nothing");
        unusedSupplier = new Supplier(502, "Unused", "Nothing");
    }

    /*
     The following tests will run multiple times each.
     They will be called using every instance that we have created in the getCategoryClasses, getSupplierClasses  and in the getProductClasses methods.
     If we have 2 instances in getCategoryClasses(), the first test will run twice, if we had 3, it would run 3 times etc.
    */

    @Order(1)
    @ParameterizedTest
    @MethodSource("getCategoryClasses")
    public void testCategoryAdd(ProductCategoryDao category){
        category.add(testCategory);
        category.add(unusedCategory);
        assertNotNull(category.find(400));
        assertNotNull(category.find(402));
    }

    @Order(2)
    @ParameterizedTest
    @MethodSource("getSupplierClasses")
    public void testSupplierAdd(SupplierDao supplier){
        supplier.add(testSupplier);
        supplier.add(unusedSupplier);
        assertNotNull(supplier.find(500));
        assertNotNull(supplier.find(502));
    }

    @Order(3)
    @ParameterizedTest
    @MethodSource("getProductClasses")
    public void testProductAdd(ProductDao product){
        product.add(testProduct);
        assertNotNull(product.find(100));
    }

    @Order(4)
    @ParameterizedTest
    @MethodSource("getUserClasses")
    public void testUserAddAndFind(UserDao user) {
        user.addNewUser(testUser);
        User foundUser = user.find("Yolo@yolomail.com");
        assertEquals(testUser.getPassword(),foundUser.getPassword());
    }

    @Order(5)
    @ParameterizedTest
    @MethodSource("getUserClasses")
    public void testUserAddNotNull(UserDao user){
        assertNotNull(user.find("Yolo@yolomail.com"));
    }

    @Order(6)
    @ParameterizedTest
    @MethodSource("getCategoryClasses")
    public void testCategoryFind(ProductCategoryDao category){
        assertEquals("RomanCategory", category.find(400).getName());
    }

    @Order(7)
    @ParameterizedTest
    @MethodSource("getCategoryClasses")
    public void testCategoryFindByString(ProductCategoryDao category){
        assertEquals(400, category.find("RomanCategory").getId());
    }

    @Order(8)
    @ParameterizedTest
    @MethodSource("getSupplierClasses")
    public void testSupplierFind(SupplierDao supplier){
        assertEquals("RomanSupplier", supplier.find(500).getName());
    }

    @Order(9)
    @ParameterizedTest
    @MethodSource("getSupplierClasses")
    public void testSupplierFindByString(SupplierDao supplier){
        assertEquals(500, supplier.find("RomanSupplier").getId());
    }

    @Order(10)
    @ParameterizedTest
    @MethodSource("getProductClasses")
    public void testProductFindByID(ProductDao product){
        assertEquals("RomanMan", product.find(100).getName());
    }

    @Order(11)
    @ParameterizedTest
    @MethodSource("getProductClasses")
    public void testProductFindBySupplier(ProductDao product){
        assertEquals(1, product.getBy(testSupplier).size());
        assertEquals(0, product.getBy(falseSupplier).size());
    }

    @Order(12)
    @ParameterizedTest
    @MethodSource("getProductClasses")
    public void testProductFindByProductCategory(ProductDao product){
        assertEquals(1, product.getBy(testCategory).size());
        assertEquals(0, product.getBy(falseCategory).size());
    }

    @Order(13)
    @ParameterizedTest
    @MethodSource("getUserClasses")
    public void testUserNotFind(UserDao user){
        User falseUser = user.find("Fake.Feri@fakemail.com");
        assertThrows(NullPointerException.class,()->user.find(falseUser.getEmail()));
    }

    @Order(14)
    @ParameterizedTest
    @MethodSource("getProductClasses")
    public void testGetProductsByString(ProductDao product){
        assertEquals(1, product.getProducts(testCategory.getName(),testSupplier.getName()).size());
        assertEquals(0, product.getProducts("Unused","Unused").size());
        assertThrows(NullPointerException.class, () -> product.getProducts("", ""));
        assertThrows(NullPointerException.class, () -> product.getProducts(testCategory.getName(), "jhijn"));
        assertThrows(NullPointerException.class, () -> product.getProducts("jhijn", testSupplier.getName()));
    }

    @Order(15)
    @ParameterizedTest
    @MethodSource("getCategoryClasses")
    public void testGetAllCategories(ProductCategoryDao category){
        assertEquals(2, category.getAll().size());
    }

    @Order(16)
    @ParameterizedTest
    @MethodSource("getSupplierClasses")
    public void testGetAllSuppliers(SupplierDao supplier){
        assertEquals(2, supplier.getAll().size());
    }

    @Order(17)
    @ParameterizedTest
    @MethodSource("getProductClasses")
    public void testProductGetAllProducts(ProductDao product){
        assertEquals(1, product.getAll().size());
    }

    @Order(18)
    @ParameterizedTest
    @MethodSource("getUserClasses")
    public void testUserUpdate(UserDao user){
        user.updateUserData(testUser.getEmail(),testUser2);
        assertEquals(user.find(testUser.getEmail()).getName(), testUser2.getName());
    }

    /*
    Remove is left for last as it will remove the data from the database as well.
    We start with product as category and supplier are both connected to this.
    */

    @Order(19)
    @ParameterizedTest
    @MethodSource("getProductClasses")
    public void testProductRemove(ProductDao product){
        product.remove(100);
        assertNull(product.find(100));
        assertEquals(0, product.getAll().size());
    }

    @Order(20)
    @ParameterizedTest
    @MethodSource("getCategoryClasses")
    public void testCategoryRemove(ProductCategoryDao category){
        category.remove(400);
        category.remove(402);
        assertNull(category.find(400));
        assertEquals(0, category.getAll().size());
    }


    @Order(21)
    @ParameterizedTest
    @MethodSource("getSupplierClasses")
    public void testSupplierRemove(SupplierDao supplier){
        supplier.remove(500);
        supplier.remove(502);
        assertNull(supplier.find(500));
        assertEquals(0, supplier.getAll().size());
    }

    @Order(22)
    @ParameterizedTest
    @MethodSource("getUserClasses")
    public void testUserRemove(UserDao user){
        user.removeUser(testUser.getEmail());
        assertNull(user.find(testUser.getEmail()));
    }












}