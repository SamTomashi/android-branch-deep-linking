package io.branch.referral.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

/**
 * Created by Evan Groth on 12/21/16.
 */

public class Product {
    private static final int DECIMAL_PLACES = 2;
    public String sku;
    public String name;
    public BigDecimal price;
    public int quantity;
    public String brand;
    public String category;
    public String variant;
    public ProductCategory productCategory;

    public Product() {
        this.sku = "";
        this.name = "";
        this.price = new BigDecimal(0).setScale(DECIMAL_PLACES, BigDecimal.ROUND_HALF_UP);
        this.quantity = 0;
        this.brand = "";
        this.category = "";
        this.variant = "";
        this.productCategory = null;
    }

    public Product(String sku, String name, BigDecimal price, int quantity, String brand, String category, String variant, ProductCategory productCategory) {
        this.sku = sku;
        this.name = name;
        this.price = price.setScale(DECIMAL_PLACES, BigDecimal.ROUND_HALF_UP);
        this.quantity = quantity;
        this.brand = brand;
        this.category = category;
        this.variant = variant;
        this.productCategory = productCategory;
    }

    public JSONObject getProductJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sku", this.sku);
            jsonObject.put("name", this.name);
            jsonObject.put("price", this.price);
            jsonObject.put("quantity", this.quantity);
            jsonObject.put("brand", this.brand);
            jsonObject.put("category", this.category);
            jsonObject.put("variant", this.variant);
            jsonObject.put("productCategory", this.productCategory);
        } catch (JSONException e) {

        }
        return jsonObject;
    }
}
