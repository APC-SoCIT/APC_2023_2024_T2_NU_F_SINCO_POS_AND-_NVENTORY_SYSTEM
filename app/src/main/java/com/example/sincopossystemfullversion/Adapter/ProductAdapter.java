package com.example.sincopossystemfullversion.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincopossystemfullversion.Java.Product;
import com.example.sincopossystemfullversion.R;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private List<Product> productList;
    private Context context;
    private OnAddButtonClickListener listener;
    private List<Product> selectedProducts;

    public interface OnAddButtonClickListener {
        void onAddButtonClick(int position);
        void onPositiveClick(int position);
        void onNegativeClick(int position);
    }

    public ProductAdapter(List<Product> productList, Context context, OnAddButtonClickListener listener) {
        this.productList = productList;
        this.context = context;
        this.listener = listener;
        this.selectedProducts = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.productTitle.setText(product.getTitle());
        holder.productImage.setImageURI(product.getImageUri()); // Set the image using Uri
        holder.productDescription.setText(product.getDescription());
        holder.productPrice.setText(product.getPrice());
        holder.productQuantity.setText(String.valueOf(product.getQuantity()));

        holder.btnAdd.setOnClickListener(v -> listener.onAddButtonClick(position));
        holder.btnPositive.setOnClickListener(v -> listener.onPositiveClick(position));
        holder.btnNegative.setOnClickListener(v -> listener.onNegativeClick(position));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    // Add this method to get the list of selected products
    public List<Product> getSelectedProducts() {
        return selectedProducts;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView productTitle;
        ImageView productImage;
        TextView productDescription;
        TextView productPrice;
        TextView productQuantity;
        Button btnAdd;
        ImageView btnPositive;
        ImageView btnNegative;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            productTitle = itemView.findViewById(R.id.ProductTitle);
            productImage = itemView.findViewById(R.id.cardImage);
            productDescription = itemView.findViewById(R.id.descriptionTitle);
            productPrice = itemView.findViewById(R.id.PriceTitle);
            productQuantity = itemView.findViewById(R.id.quantityTextView);
            btnAdd = itemView.findViewById(R.id.addbutton);
            btnPositive = itemView.findViewById(R.id.positiveIcon);
            btnNegative = itemView.findViewById(R.id.negativeIcon);
        }
    }

    public Product getSelectedProduct(int position) {
        if (position >= 0 && position < productList.size()) {
            return productList.get(position);
        }
        return null;
    }

    // Inside ProductAdapter class
    public void increaseQuantity(int position) {
        if (position >= 0 && position < productList.size()) {
            Product product = productList.get(position);
            int newQuantity = product.getQuantity() + 1;
            product.setQuantity(newQuantity);
            notifyItemChanged(position);
            selectedProducts.add(product); // Add selected product to the list
        }
    }

    public void decreaseQuantity(int position) {
        if (position >= 0 && position < productList.size()) {
            Product product = productList.get(position);
            int newQuantity = product.getQuantity() - 1;
            if (newQuantity >= 0) {
                product.setQuantity(newQuantity);
                notifyItemChanged(position);
                selectedProducts.add(product); // Add selected product to the list
            }
        }
    }
}
