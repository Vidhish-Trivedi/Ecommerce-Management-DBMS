package com.cs301p.easy_ecomm.responseClasses;

public class ShippingDetailsDataResponse {
    private int transactionId;
    private int productId;
    private String customerName;
    private String customerAddress;


    public ShippingDetailsDataResponse() {
    }

    public ShippingDetailsDataResponse(int transactionId, int productId, String customerName, String customerAddress) {
        this.transactionId = transactionId;
        this.productId = productId;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
    }

    public int getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getProductId() {
        return this.productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getCustomerName() {
        return this.customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerAddress() {
        return this.customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    @Override
    public String toString() {
        return "{" +
            "\n transactionId='" + getTransactionId() + "'" +
            ",\n productId='" + getProductId() + "'" +
            ",\n customerName='" + getCustomerName() + "'" +
            ",\n customerAddress='" + getCustomerAddress() + "'" +
            "\n}";
    }
}
