package com.example.sincopossystemfullversion.Java;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Product implements Parcelable {
    private String title;
    private Uri imageUri;
    private String description;
    private String price;
    private int quantity;
    private double discount;

    // Existing constructors without discount
    public Product(String title, Uri imageUri, String description, String price) {
        this.title = title;
        this.imageUri = imageUri;
        this.description = description;
        this.price = price;
        this.quantity = 1; // Default quantity is set to 1
        this.discount = 0.0; // Default discount is set to 0%
    }

    // Constructor with discount
    public Product(String title, Uri imageUri, String description, String price, double discount) {
        this.title = title;
        this.imageUri = imageUri;
        this.description = description;
        this.price = price;
        this.quantity = 1; // Default quantity is set to 1
        this.discount = discount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPriceWithCurrency() {
        return "₱" + price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    // Calculate the discounted price
    public double getDiscountedPrice() {
        double numericPrice = parsePriceNumeric(price);
        return numericPrice - (numericPrice * discount / 100);
    }

    // Helper method to parse the numeric value from the price string
    private double parsePriceNumeric(String price) {
        String numericValue = price.replaceAll("[^\\d.]", "");
        return Double.parseDouble(numericValue);
    }

    // Parcelable implementation
    protected Product(Parcel in) {
        title = in.readString();
        imageUri = in.readParcelable(Uri.class.getClassLoader());
        description = in.readString();
        price = in.readString();
        quantity = in.readInt();
        discount = in.readDouble();
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeParcelable(imageUri, flags);
        dest.writeString(description);
        dest.writeString(price);
        dest.writeInt(quantity);
        dest.writeDouble(discount);
    }
}