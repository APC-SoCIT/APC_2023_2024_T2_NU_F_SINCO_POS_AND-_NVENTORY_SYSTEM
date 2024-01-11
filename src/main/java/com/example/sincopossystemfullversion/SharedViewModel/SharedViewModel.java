package com.example.sincopossystemfullversion.SharedViewModel;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sincopossystemfullversion.Java.Product;

import java.util.ArrayList;
import java.util.List;

public class SharedViewModel extends ViewModel {
    private MutableLiveData<List<Product>> productListLiveData = new MutableLiveData<>();
    private MutableLiveData<Product> selectedProductLiveData = new MutableLiveData<>();
    private MutableLiveData<List<Product>> updatedBillLiveData = new MutableLiveData<>();
    private MutableLiveData<String> selectedPaymentMethodLiveData = new MutableLiveData<>(); // New MutableLiveData

    // Method to add a single product to the list
    public void addProduct(Product product) {
        List<Product> currentList = productListLiveData.getValue();
        if (currentList == null) {
            currentList = new ArrayList<>();
        }

        // Check if the product is already in the list
        boolean productExists = false;
        for (Product existingProduct : currentList) {
            if (existingProduct.equals(product)) {
                productExists = true;
                break;
            }
        }

        if (!productExists) {
            // Add the product to the list
            currentList.add(product);
            productListLiveData.setValue(currentList);
        }
    }

    // Updated method to accept new parameters and add a product to the list
    public void addProduct(String productName, Uri imageUri, String productDescription, String productPrice) {
        // Create a new Product instance with the provided parameters
        Product product = new Product(productName, imageUri, productDescription, productPrice);

        // Add the product to the list
        addProduct(product);
    }

    // Setter method for selectedProduct
    public void setSelectedProduct(Product product) {
        selectedProductLiveData.setValue(product);
    }

    // Getter method for selectedProduct
    public LiveData<Product> getSelectedProduct() {
        return selectedProductLiveData;
    }

    // Method to update updatedbill
    public void updateUpdatedBill(List<Product> updatedProducts) {
        updatedBillLiveData.setValue(updatedProducts);
    }

    // Getter method for updatedbill
    public LiveData<List<Product>> getUpdatedBill() {
        return updatedBillLiveData;
    }

    // Setter method for selectedPaymentMethod
    public void setSelectedPaymentMethod(String paymentMethod) {
        selectedPaymentMethodLiveData.setValue(paymentMethod);
    }

    // Getter method for selectedPaymentMethod
    public LiveData<String> getSelectedPaymentMethod() {
        return selectedPaymentMethodLiveData;
    }

    public LiveData<List<Product>> getProductList() {
        return productListLiveData;
    }
}
