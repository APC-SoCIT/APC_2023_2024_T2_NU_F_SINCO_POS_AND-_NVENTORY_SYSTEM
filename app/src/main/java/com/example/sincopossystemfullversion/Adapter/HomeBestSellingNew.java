package com.example.sincopossystemfullversion.Adapter;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
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
import com.example.sincopossystemfullversion.SalesReportModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class HomeBestSellingNew extends FirebaseRecyclerAdapter <SalesReportModel, HomeBestSellingNew.myViewHolder>{

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     * @param context
     */

    private Map<String, Long> productQuantityMap = new HashMap<>();
    private Map<String, ProductsModel> productMap = new HashMap<>();
    private Set<String> encounteredProducts = new HashSet<>();
    private Set<Pair<String, String>> encounteredMonthProductPairs = new HashSet<>();
    private List<Map.Entry<String, Long>> sortedProducts = new ArrayList<>();

    public HomeBestSellingNew(@NonNull FirebaseRecyclerOptions<SalesReportModel> options, Context context) {
        super(options);
        fetchProductData();
    }

    @Override
    public int getItemCount() {
        return Math.min(sortedProducts.size(), 5); // Return either the size of the sortedProducts list or 5, whichever is smaller
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull SalesReportModel model) {
        Log.d("Firebase", "Product Quantity Map" + productQuantityMap);

        if (position < sortedProducts.size()) {
            // Retrieve the product details from the sorted list
            Map.Entry<String, Long> entry = sortedProducts.get(position);
            String productName = entry.getKey();
            Long totalQty = entry.getValue();

            // Update the UI with the product name and total quantity
            holder.flavorTV.setText(productName);

            // If the product exists in the product map, load its image
            if (productMap.containsKey(productName)) {
                ProductsModel product = productMap.get(productName);
                if (product != null) {
                    Glide.with(holder.img.getContext())
                            .load(product.getImage_url())
                            .placeholder(com.firebase.ui.database.R.drawable.common_google_signin_btn_icon_dark)
                            .centerCrop()
                            .error(com.firebase.ui.database.R.drawable.common_google_signin_btn_icon_dark_normal)
                            .into(holder.img);
                    holder.img.setVisibility(View.VISIBLE);
                }
            } else {
                // Hide the image if the product does not exist in the product map
                holder.img.setVisibility(View.GONE);
            }

            // Ensure that the TextView is visible
            holder.flavorTV.setVisibility(View.VISIBLE);
        } else {
            // Hide the views for positions beyond the top 5 products
            holder.flavorTV.setVisibility(View.GONE);
            holder.img.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_bestselling, parent, false);
        return new myViewHolder(view);
    }

    static class myViewHolder extends RecyclerView.ViewHolder {
        TextView flavorTV; ImageView img;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            flavorTV = itemView.findViewById(R.id.flavorTV);
            img = itemView.findViewById(R.id.cardImage);
        }
    }

    private void fetchProductData() {
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child("products");
        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ProductsModel product = snapshot.getValue(ProductsModel.class);
                    if (product != null) {
                        String productName = product.getProduct_name();
                        if (productName != null) {
                            productName = productName.trim(); // Trim the product name
                            productMap.put(productName, product);
                        }
                    }
                }
                Log.d("Firebase", "Product Map: " + productMap.toString());
                fetchDataAndCalculateTotalQty();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error reading product data: " + databaseError.getMessage());
            }
        });
    }


    private void fetchDataAndCalculateTotalQty() {
        DatabaseReference salesRef = FirebaseDatabase.getInstance().getReference().child("sales_report");

        salesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear existing data to avoid duplication
                productQuantityMap.clear();

                // Get the current month
                Calendar calendar = Calendar.getInstance();
                int currentMonth = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH is zero-based

                // Iterate over the dataSnapshot to process each SalesReportModel object
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    SalesReportModel model = snapshot.getValue(SalesReportModel.class);
                    String date = model.getSr_date(); // Assuming you have a 'date' field in SalesReportModel
                    String month = extractMonthFromDate(date); // Extract the month from the date

                    // Check if the month matches the current month
                    if (month != null && month.equals(getMonthName(currentMonth))) {
                        String productName = model.getSr_product().trim(); // Trim the product name

                        // Update combined total quantity for the corresponding product
                        Long totalQty = productQuantityMap.getOrDefault(productName, 0L);
                        totalQty += model.getSr_qty();
                        productQuantityMap.put(productName, totalQty);
                    }
                }

                // Sort the products by quantity in descending order
                sortedProducts = new ArrayList<>(productQuantityMap.entrySet());
                Collections.sort(sortedProducts, (entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

                // Notify the adapter that the data set has changed
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors here
                Log.e("Firebase", "Error reading data: " + databaseError.getMessage());
            }
        });
    }


    private String getMonthName(int month) {
        return new DateFormatSymbols().getMonths()[month - 1]; // Adjust for zero-based month index
    }

    private String extractMonthFromDate(String date) {
        if (date != null && !date.isEmpty()) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        try {
            Date dateObj = dateFormat.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateObj);
            int month = calendar.get(Calendar.MONTH); // Month is zero-based
            return new DateFormatSymbols().getMonths()[month];
        } catch (ParseException e) {
            e.printStackTrace();
            return "Invalid Date";
        }
        } else {
            return "Invalid Date";
        }
    }


}
