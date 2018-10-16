package allurosi.housebuddy.expensetracker;

import com.google.firebase.firestore.Exclude;

public class Product {

    private String name;
    private double price;
    private String productId;


    Product(String name, double price) {
        this.name = name;
        this.price = price;
    }

    Product(){}

    Product(Product original) {
        this.productId = original.productId;
        this.name = original.name;
        this.price = original.price;
    }

    @Exclude
    String getProductId() {
        return productId;
    }

    @Exclude
    void setProductId(String product_id) {
        this.productId = product_id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Product product = (Product) obj;
        return productId.equals(product.productId) && name.equals(product.name) && price == product.price;

    }
}
