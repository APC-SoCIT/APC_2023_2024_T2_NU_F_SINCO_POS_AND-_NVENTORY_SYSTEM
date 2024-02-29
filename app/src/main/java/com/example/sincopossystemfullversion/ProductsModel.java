package com.example.sincopossystemfullversion;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Map;


public class ProductsModel implements Parcelable {

    String id; // Product ID
    String product_name, product_type, image_url, ingredient_type;
    long product_cost; // Changed the data type to long
    int quantity; // Add the quantity field
    String selectedSize; // Add selected size field
    boolean isFirstTimeClicked; // Add isFirstTimeClicked flag
    private boolean archived;

    private Map<String, Long> sizes;

    public ProductsModel() {
        // Default constructor
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ProductsModel(String id, String product_name, String product_type, String ingredient_type, long product_cost, String image_url) {
        this.id = id;
        this.product_name = product_name;
        this.product_type = product_type;
        this.product_cost = product_cost;
        this.ingredient_type = ingredient_type;
        this.image_url = image_url;
        this.quantity = 0; // Initialize quantity to 0
        this.selectedSize = ""; // Initialize selected size to empty string
        this.isFirstTimeClicked = false; // Initialize isFirstTimeClicked to false
    }

    public Map<String, Long> getSizes() {
        return sizes;
    }

    public void setSizes(Map<String, Long> sizes) {
        this.sizes = sizes;}

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }


    public long getProduct_cost() { // Changed the return type to long
        return product_cost;
    }

    public void setProduct_cost(long product_cost) { // Changed the parameter type to long
        this.product_cost = product_cost;
    }

    public String getProduct_type() {
        return product_type;
    }

    public void setProduct_type(String product_type) {
        this.product_type = product_type;
    }

    public String getIngredient_type() {
        return ingredient_type;
    }

    public void setIngredient_type(String ingredient_type) {
        this.ingredient_type = ingredient_type;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    // Getter and setter for quantity
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Getter and setter for selected size
    public String getSelectedSize() {
        return selectedSize;
    }

    public void setSelectedSize(String selectedSize) {
        this.selectedSize = selectedSize;
    }

    // Getter and setter for isFirstTimeClicked flag
    public boolean isFirstTimeClicked() {
        return isFirstTimeClicked;
    }

    public void setFirstTimeClicked(boolean firstTimeClicked) {
        isFirstTimeClicked = firstTimeClicked;
    }

    // Parcelable implementation
    protected ProductsModel(Parcel in) {
        id = in.readString();
        product_name = in.readString();
        product_type = in.readString();
        image_url = in.readString();
        product_cost = in.readLong(); // Changed to readLong() for long data type
        quantity = in.readInt();
        selectedSize = in.readString(); // Read selected size from Parcel
        isFirstTimeClicked = in.readByte() != 0; // Read isFirstTimeClicked from Parcel
    }

    public static final Creator<ProductsModel> CREATOR = new Creator<ProductsModel>() {
        @Override
        public ProductsModel createFromParcel(Parcel in) {
            return new ProductsModel(in);
        }

        @Override
        public ProductsModel[] newArray(int size) {
            return new ProductsModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(product_name);
        dest.writeString(product_type);
        dest.writeString(image_url);
        dest.writeLong(product_cost); // Changed to writeLong() for long data type
        dest.writeInt(quantity);
        dest.writeString(selectedSize); // Write selected size to Parcel
        dest.writeByte((byte) (isFirstTimeClicked ? 1 : 0)); // Write isFirstTimeClicked to Parcel
    }
}