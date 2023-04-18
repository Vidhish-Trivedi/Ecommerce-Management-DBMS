package com.cs301p.easy_ecomm.daoClasses;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import com.cs301p.easy_ecomm.entityClasses.Product;
import com.cs301p.easy_ecomm.entityClasses.Seller;
import com.cs301p.easy_ecomm.mappers.ProductMapper;
import com.cs301p.easy_ecomm.utilClasses.FilterBy;
import com.cs301p.easy_ecomm.utilClasses.OrderBy;

public class ProductDAO {
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private PlatformTransactionManager platformTransactionManager;

    public ProductDAO(DataSource dataSource, PlatformTransactionManager platformTransactionManager,
            JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.platformTransactionManager = platformTransactionManager;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
    }

    public JdbcTemplate getJdbcTemplate() {
        return this.jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public PlatformTransactionManager getPlatformTransactionManager() {
        return this.platformTransactionManager;
    }

    public void getPlatformTransactionManager(PlatformTransactionManager platformTransactionManager) {
        this.platformTransactionManager = platformTransactionManager;
    }

    // Now, write query functions.
    public Product getProductById(Product product) {
        String sql = "SELECT * FROM product WHERE id=" + product.getId();
        List<Product> products = this.jdbcTemplate.query(sql, new ProductMapper());

        return products.get(0);
    }

    public int addProduct(Product product) {
        int count = 0;
        String sql = "INSERT INTO product(type, name, sellerId, price, quantityAvailable) VALUES (?, ?, ?, ?, ?);";

        count = this.jdbcTemplate.update(sql, product.getType(), product.getSellerId(),
                product.getPrice(), product.getQuantityAvailable());

        return (count);
    }

    public int updateProduct(Product product) {
        int count = 0;
        String sql = "UPDATE product SET price=?, quantityAvailabe=? WHERE id=?;";

        count = this.jdbcTemplate.update(sql, product.getPrice(), product.getQuantityAvailable(), product.getId());

        return (count);
    }

    public int deleteProduct(Product product) {
        int count = 0;
        String sql = "DELETE FROM product WHERE id=?;";

        count = this.jdbcTemplate.update(sql, product.getId());

        return (count);
    }

    public Seller getSellerByProductId(Product product) {
        String sql = "SELECT sellerId from product where id=" + product.getId();
        List<Product> products = this.jdbcTemplate.query(sql, new ProductMapper());

        Seller s = new Seller();
        s.setId(products.get(0).getSellerId());

        return s;
    }

    public List<Product> listProducts(FilterBy filter_by, OrderBy sort_by) {
        String sql;
        List<Product> result = null;
        int count = -1;
        // Can do between x AND x.
        try {
            if (filter_by.getIsBetween()) {
                sql = "SELECT * FROM product WHERE " + filter_by.getAttr() + " BETWEEN " + filter_by.getL_val()
                        + " AND " + filter_by.getH_val() + " ORDER BY " + sort_by.getAttr() + " "
                        + sort_by.getDirection();

                result = this.jdbcTemplate.query(sql, new ProductMapper());

            } else {
                sql = "SELECT * FROM product WHERE " + filter_by.getAttr() + "=" + filter_by.getL_val() + " ORDER BY "
                        + sort_by.getAttr() + " "
                        + sort_by.getDirection();
                result = this.jdbcTemplate.query(sql, new ProductMapper());
            }
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return (result);
    }

}
