package com.example.sincopossystemfullversion.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincopossystemfullversion.ProductsModel;
import com.example.sincopossystemfullversion.R;

import java.text.DecimalFormat;
import java.util.List;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder> {

    private List<ProductsModel> checkoutProducts;

    // Constructor to initialize the adapter with a list of products
    public CheckoutAdapter(List<ProductsModel> checkoutProducts) {
        this.checkoutProducts = checkoutProducts;
    }

    // ViewHolder class to represent the item view
    public static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        // Declare your TextViews or other views here
        TextView quantityTextView, productNameTextView, priceTextView, productsizeTextView;

        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize your TextViews or other views here
            quantityTextView = itemView.findViewById(R.id.CheckoutproductQtyTextView);
            productNameTextView = itemView.findViewById(R.id.CheckoutproductNameTextView);
            priceTextView = itemView.findViewById(R.id.CheckoutproductPriceTextView);
            productsizeTextView = itemView.findViewById(R.id.CheckoutproductSizeTextView);

        }
    }

    // Method to update the data in the adapter
    public void updateData(List<ProductsModel> newCheckoutProducts) {
        this.checkoutProducts = newCheckoutProducts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CheckoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout and create the ViewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.checkout_item, parent, false);
        return new CheckoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutViewHolder holder, int position) {
        // Bind the data to the ViewHolder
        ProductsModel checkoutProduct = checkoutProducts.get(position);

        holder.quantityTextView.setText(String.valueOf(checkoutProduct.getQuantity()));
        holder.productNameTextView.setText(String.valueOf(checkoutProduct.getProduct_name()));
        holder.productsizeTextView.setText(checkoutProduct.getSelectedSize());

        float totalPrice = (float) (checkoutProduct.getProduct_cost() * checkoutProduct.getQuantity());

        // Format the price with two decimal places
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        String formattedPrice = "₱" + decimalFormat.format(totalPrice);
        holder.priceTextView.setText(formattedPrice);
    }

    @Override
    public int getItemCount() {
        // Return the size of the data set
        return checkoutProducts.size();
    }
}
