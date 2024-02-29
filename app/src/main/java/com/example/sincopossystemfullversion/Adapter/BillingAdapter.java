package com.example.sincopossystemfullversion.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sincopossystemfullversion.ProductsModel;
import com.example.sincopossystemfullversion.R;

import java.text.DecimalFormat;
import java.util.List;

public class BillingAdapter extends RecyclerView.Adapter<BillingAdapter.BillingViewHolder> {
    private List<ProductsModel> selectedProducts;
    private Context context;

    private DecimalFormat decimalFormat;

    public BillingAdapter(Context context, List<ProductsModel> selectedProducts) {
        this.context = context;
        this.selectedProducts = selectedProducts;
        // Initialize DecimalFormat
        decimalFormat = new DecimalFormat("#,##0.00");
    }

    @NonNull
    @Override
    public BillingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.productsbilling, parent, false);
        return new BillingViewHolder(view);
    }
    

    @Override
    public void onBindViewHolder(@NonNull BillingViewHolder holder, int position) {
        ProductsModel selectedProduct = selectedProducts.get(position);

        // Set data to the billing layout elements
        holder.quantityTextView.setText("Qty: " + selectedProduct.getQuantity());
        holder.productNameTextView.setText(selectedProduct.getProduct_name());
        holder.productsizeTextView.setText(selectedProduct.getSelectedSize());
        // Explicitly cast the result to a float or double to resolve the format specifier issue
        float totalPrice = (float) (selectedProduct.getProduct_cost() * selectedProduct.getQuantity());

        // Format the total price using DecimalFormat
        String formattedPrice = "Price: ₱" + decimalFormat.format(totalPrice);
        holder.priceTextView.setText(formattedPrice);

        // Load the product image using Glide
        Glide.with(context)
                .load(selectedProduct.getImage_url())
                .placeholder(R.drawable.uploadimage) // Placeholder image while loading
                .error(R.drawable.uploadimage) // Error image if loading fails
                .into(holder.productImageView);
    }

    @Override
    public int getItemCount() {
        return selectedProducts.size();
    }

    public List<ProductsModel> getSelectedProducts() {
        return selectedProducts;
    }

    public static class BillingViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageView;
        TextView quantityTextView, productNameTextView, priceTextView, productsizeTextView;

        public BillingViewHolder(@NonNull View itemView) {
            super(itemView);

            productImageView = itemView.findViewById(R.id.BillingproductImageView);
            quantityTextView = itemView.findViewById(R.id.BillingquantityTextView);
            productNameTextView = itemView.findViewById(R.id.BillingproductNameTextView);
            priceTextView = itemView.findViewById(R.id.BillingpriceTextView);
            productsizeTextView = itemView.findViewById(R.id.BillingsizeTextView);
        }
    }
}