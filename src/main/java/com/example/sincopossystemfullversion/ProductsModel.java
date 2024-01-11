package com.example.sincopossystemfullversion;

public class ProductsModel {

    String product_name,product_desc,product_type,image_url;
            Long product_cost;


    ProductsModel(){


    }

    public ProductsModel(String product_name, String product_desc, String product_type, Long product_cost, String image_url) {
        this.product_name = product_name;
        this.product_desc = product_desc;
        this.product_type = product_type;
        this.product_cost = product_cost;
        this.image_url = image_url;
    }


    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getProduct_desc() {
        return product_desc;
    }

    public void setProduct_desc(String product_desc) {
        this.product_desc = product_desc;
    }

    public Long getProduct_cost() {
        return product_cost;
    }

    public void setProduct_cost(Long product_cost) {
        this.product_cost = product_cost;
    }

    public String getProduct_type() {
        return product_type;
    }

    public void setProduct_type(String product_type) {
        this.product_type = product_type;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }
}
