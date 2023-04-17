package com.cs301p.easy_ecomm;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.cs301p.easy_ecomm.daoClasses.CartItemDAO;
import com.cs301p.easy_ecomm.daoClasses.CustomerDAO;
import com.cs301p.easy_ecomm.daoClasses.ProductDAO;
import com.cs301p.easy_ecomm.daoClasses.ReviewDAO;
import com.cs301p.easy_ecomm.daoClasses.TransactionDAO;
import com.cs301p.easy_ecomm.daoClasses.WalletDAO;
import com.cs301p.easy_ecomm.entityClasses.CartItem;
import com.cs301p.easy_ecomm.entityClasses.Customer;
import com.cs301p.easy_ecomm.entityClasses.Product;
import com.cs301p.easy_ecomm.entityClasses.Review;
import com.cs301p.easy_ecomm.entityClasses.Transaction;
import com.cs301p.easy_ecomm.entityClasses.Wallet;
import com.cs301p.easy_ecomm.factoryClasses.DAO_Factory;
import com.cs301p.easy_ecomm.mappers.TransactionMapper;
import com.cs301p.easy_ecomm.responseClasses.CartItemDataResponse;

@SpringBootApplication
public class EasyEcommApplication {
    private PlatformTransactionManager platformTransactionManager;
    private JdbcTemplate jdbcTemplate;

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(EasyEcommApplication.class, args);
        DAO_Factory dao_Factory = (DAO_Factory) applicationContext.getBean(DAO_Factory.class);

        // CustomerDAO customerDAO = dao_Factory.getCustomerDAO();
        // WalletDAO walletDAO = dao_Factory.getWalletDAO();
        // Wallet w1 = new Wallet(0, "1211-3344-2556-1095", (float)440021.14); //
        // Auto-increment, can pass any id while inserting.
        // Customer c1 = new Customer(0, "Customer 1", "em1@tm.com", "pass123",
        // "1234432100", "Chennai, Tamil Nadu", 3);

        // int count = walletDAO.addWallet(w1);
        // System.out.println(count + " records added to wallet table.");
        // count = customerDAO.addCustomer(c1);
        // System.out.println(count + " records added to customer table.");
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Usecase (E)
    public int purchaseCart(Customer customer, DAO_Factory dao_Factory) {
        System.out.println();
        System.out.println("Initiate multiple actions...");
        TransactionDefinition td = new DefaultTransactionDefinition();
        CartItemDAO cartItemDAO = dao_Factory.getCartItemDAO();
        ProductDAO productDAO = dao_Factory.getProductDAO();
        TransactionStatus ts = this.platformTransactionManager.getTransaction(td);

        try {
            // Check cart of customer.
            List<CartItemDataResponse> cartItemDataResponses = cartItemDAO.listCartItems(customer);

            // Check Quantity.
            for (CartItemDataResponse p : cartItemDataResponses) {
                Product product = new Product();
                product.setId(p.getProductId());

                // Handle Insufficient Quantity.
                if (p.getQuantity() > productDAO.getProductById(product).getQuantityAvailable()) {
                    return (product.getId());
                }
            }

            // Generate new transactionId.
            String sql = "SELECT * FROM transaction WHERE id=(SELECT MAX(id) FROM transaction);";
            List<Transaction> transactions = this.jdbcTemplate.query(sql, new TransactionMapper());

            int new_id = transactions.get(0).getId() + 1;

            // Insert Transaction.
            for (CartItemDataResponse p : cartItemDataResponses) {
                int count = 0;
                sql = "INSERT INTO transaction(id, customerId, sellerId, productId, date, returnStatus) VALUES (?, ?, ?, ?, ?, ?);";

                Product product = new Product();
                product.setId(p.getProductId());

                Date date = new Date(System.currentTimeMillis());
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                String formattedDate = formatter.format(date);

                count = this.jdbcTemplate.update(sql, new_id, customer.getId(),
                        productDAO.getProductById(product).getSellerId(),
                        p.getProductId(), formattedDate, false);

                if (count <= 0) {
                    System.out.println("INSERT INTO TRANSACTION -- ERROR:\n productID: " + p.getProductId());
                }

                // return (count);
            }

        } catch (Exception ex) {
            System.out.println("Transaction Failed: " + ex);
            platformTransactionManager.rollback(ts);
        }
        return (0); // Success.
    }

    public int handleInsufficientQuantity(Customer customer, Product product, String choice, DAO_Factory dao_Factory) {
        // If choice = "yes", discard item, else buy reduced quantity.
        if (choice.strip().toLowerCase().equals("yes")) {
            // Delete from cart_item table.
            CartItemDAO cartItemDAO = dao_Factory.getCartItemDAO();
            CartItem c = new CartItem(customer.getId(), product.getId(), 0);
            int count = cartItemDAO.deleteCartItem(c);

            System.out.println("Deleted " + count + " rows from cart_item table.");
        } else {
            // Update quantity in cart.
            CartItemDAO cartItemDAO = dao_Factory.getCartItemDAO();
            CartItem c = new CartItem(customer.getId(), product.getId(), product.getQuantityAvailable());
            int count = cartItemDAO.updateCartItem(c);

            System.out.println("Updated " + count + " rows from cart_item table.");
        }

        return (0);
    }

    // Usecase (F)
    public int reviewProduct(Customer customer, Product product, int stars, String content, DAO_Factory dao_Factory) {
        System.out.println();
        System.out.println("Initiate multiple actions...");
        TransactionDefinition td = new DefaultTransactionDefinition();
        TransactionStatus ts = this.platformTransactionManager.getTransaction(td);

        // Check number of stars.
        if (stars > 5 || stars < 0) {
            System.out.println("Stars must be between 0 and 5");
            return (-2); // Error.
        }

        try {
            // Check if customer has purchased the product.
            TransactionDAO transactionDAO = dao_Factory.getTransactionDAO();
            List<Transaction> transactions = transactionDAO.getTransactionsByCustomer(customer);
            int count = 0;

            for (Transaction transaction : transactions) {
                if (transaction.getProductId() == product.getId()) {
                    // If yes, add review.
                    ReviewDAO reviewDAO = dao_Factory.getReviewDAO();
                    Review review = new Review(0, customer.getId(), product.getId(), stars, content);
                    count = reviewDAO.addReview(review);
                    System.out.println("Added review for " + product.getId() + ", by " + customer.getId());
                    return (count);
                }
            }

        } catch (Exception ex) {
            System.out.println("Transaction Failed: " + ex);
            platformTransactionManager.rollback(ts);
        }

        System.out.println("You can not write a review for a product which you haven't purchased!");
        return (-1); // Error.
    }

    // ! TODO: deactivate dao connections.
}
