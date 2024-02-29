package com.example.sincopossystemfullversion.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sincopossystemfullversion.ProductsModel;
import com.example.sincopossystemfullversion.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProductNew extends FirebaseRecyclerAdapter<ProductsModel, ProductNew.myViewHolder> {
    private List<ProductsModel> selectedProducts;
    private BillingAdapter billingAdapter; // Add billingAdapter as a member variable
    private static final String TAG = "ProductNewAdapter"; // Define TAG variable
    private Toast toast;
    private String[] sizes = {"uno", "dos", "tres", "quatro", "sinco"}; // Define sizes as a member variable
    DecimalFormat currencyFormatter = new DecimalFormat("₱ #,###.00");


    public ProductNew(@NonNull FirebaseRecyclerOptions<ProductsModel> options, BillingAdapter billingAdapter) {
        super(options);
        this.selectedProducts = new ArrayList<>();
        this.billingAdapter = billingAdapter; // Initialize billingAdapter
    }

    public List<ProductsModel> getSelectedProducts() {
        return selectedProducts;
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull ProductsModel model) {

        if (model.isArchived()) {
            // If archived, hide the item in the RecyclerView by setting its height to 0
            holder.itemView.getLayoutParams().height = 0;
            holder.itemView.setVisibility(View.INVISIBLE); // Optional: Hide the item to avoid interaction
        } else {
            // If not archived, show the item in the RecyclerView
            holder.itemView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

            final int itemPosition = position; // Make position effectively final
            // Check if the quantity is equal to 0 before adding the click listener to the "Add" button
            holder.btnAdd.setEnabled(model.getQuantity() > 0);

            holder.btnAdd.setOnClickListener(v -> {
                // Check if a size is selected
                if (model.getSelectedSize() != null) {
                    // Add the clicked product to the selectedProducts list
                    selectedProducts.add(model);
                    // Update the UI or perform other actions as needed
                    notifyDataSetChanged(); // Notify the adapter to update the view


                    // Add the product to the selectedProducts list in billingAdapter
                    billingAdapter.getSelectedProducts().add(model);
                    // Notify the billingAdapter to update the view
                    billingAdapter.notifyDataSetChanged();
                } else {
                    // Show a toast message informing the user to select a size first
                    Toast.makeText(holder.itemView.getContext(), "Please select a size first", Toast.LENGTH_SHORT).show();
                }
            });

            // Bind data to views
            holder.prodname.setText(model.getProduct_name());
            holder.prodcost.setText(currencyFormatter.format(model.getProduct_cost()));
            holder.productQuantity.setText(String.valueOf(model.getQuantity()));

            // Set onClickListeners for quantity-related buttons
            holder.btnPositive.setOnClickListener(v -> increaseQuantity(holder.itemView.getContext(), position));
            holder.btnNegative.setOnClickListener(v -> decreaseQuantity(holder.itemView.getContext(), position));

            Glide.with(holder.img.getContext())
                    .load(model.getImage_url())
                    .placeholder(com.firebase.ui.database.R.drawable.common_google_signin_btn_icon_dark)
                    .circleCrop()
                    .error(com.firebase.ui.database.R.drawable.common_google_signin_btn_icon_dark_normal)
                    .into(holder.img);

            // Set click listeners for cardViews
            holder.cardUNO.setOnClickListener(v -> handleCardViewClick(holder, "uno", itemPosition));
            holder.cardDOS.setOnClickListener(v -> handleCardViewClick(holder, "dos", itemPosition));
            holder.cardTRES.setOnClickListener(v -> handleCardViewClick(holder, "tres", itemPosition));
            holder.cardQUATRO.setOnClickListener(v -> handleCardViewClick(holder, "quatro", itemPosition));
            holder.cardSINCO.setOnClickListener(v -> handleCardViewClick(holder, "sinco", itemPosition));

            // Set the default size selection to the previously selected size
            String selectedSize = model.getSelectedSize();
            if (selectedSize != null) {
                handleCardViewClick(holder, selectedSize, position);
            }
        }
    }


    private void handleCardViewClick(myViewHolder holder, String size, int position) {
        // Reset the selected state for all cardViews
        holder.resetCardViewSelectedState();

        // Update the selectedSize field in the ProductsModel object
        ProductsModel productModel = getItem(position);
        if (productModel != null) {
            productModel.setSelectedSize(size);
        }

        // Check if the selected size exists in the sizes array
        if (Arrays.asList(sizes).contains(size.toLowerCase())) {
            // Fetch price from sizes in the database based on selected size
            DatabaseReference productRef = getRef(position); // Get reference to the product node in the database
            productRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Retrieve sizes object from dataSnapshot
                        DataSnapshot sizesSnapshot = dataSnapshot.child("sizes");
                        if (sizesSnapshot.hasChild(size.toLowerCase())) {
                            // Update the prodcost TextView with the new price
                            Long price = sizesSnapshot.child(size.toLowerCase()).getValue(Long.class);
                            if (price != null) {
                                // Set the new price to the ProductsModel object
                                productModel.setProduct_cost(price);
                                // Update the prodcost TextView with the new price
                                holder.prodcost.setText(currencyFormatter.format(price));
                                // Notify the adapter that the item at this position has changed
                                notifyItemChanged(position);
                            }

                        } else {
                            // Handle if selected size is not found in the database
                            Toast.makeText(holder.itemView.getContext(), "Price for size " + size + " not found in database", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle if product data is not found in the database
                        Toast.makeText(holder.itemView.getContext(), "Product data not found in database", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle errors
                    Log.e(TAG, "Database error: " + databaseError.getMessage());
                    Toast.makeText(holder.itemView.getContext(), "Database error", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Handle if selected size is not in the sizes array
            Toast.makeText(holder.itemView.getContext(), "Selected size not supported", Toast.LENGTH_SHORT).show();
        }

        // Set the selected state for the clicked cardView
        switch (size.toLowerCase()) {
            case "uno":
                holder.setSelectedCardView(holder.cardUNO, R.color.selectednav);
                break;
            case "dos":
                holder.setSelectedCardView(holder.cardDOS, R.color.selectednav);
                break;
            case "tres":
                holder.setSelectedCardView(holder.cardTRES, R.color.selectednav);
                break;
            case "quatro":
                holder.setSelectedCardView(holder.cardQUATRO, R.color.selectednav);
                break;
            case "sinco":
                holder.setSelectedCardView(holder.cardSINCO, R.color.selectednav);
                break;
        }

    }



    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        return new myViewHolder(view);
    }


    private void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    public void increaseQuantity(Context context, int position) {
        ProductsModel productModel = getItem(position);
        if (productModel != null && productModel.getSelectedSize() != null) {
            int newQuantity = productModel.getQuantity() + 1;
            productModel.setQuantity(newQuantity);
            notifyItemChanged(position);
        } else {
            // Show a toast message informing the user to select a size first
            showToast(context, "Please select a size first");
        }
    }

    public void decreaseQuantity(Context context, int position) {
        ProductsModel productModel = getItem(position);
        if (productModel != null && productModel.getSelectedSize() != null && productModel.getQuantity() > 0) {
            int newQuantity = productModel.getQuantity() - 1;
            productModel.setQuantity(newQuantity);
            notifyItemChanged(position);
        } else {
            // Show a toast message informing the user to select a size first or if the quantity is already at 0
            if (productModel != null && productModel.getQuantity() == 0) {
                showToast(context, "Quantity is already 0");
            } else {
                showToast(context, "Please select a size first");
            }
        }
    }


    public static class myViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView prodname, prodcost, productQuantity;
        ImageButton btnPositive, btnNegative;
        CardView cardUNO, cardDOS, cardTRES, cardQUATRO, cardSINCO;
        public Button btnAdd;


        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            prodname = itemView.findViewById(R.id.ProductTitle);
            img = itemView.findViewById(R.id.cardImage);
            prodcost = itemView.findViewById(R.id.PriceTitle);
            productQuantity = itemView.findViewById(R.id.quantityTextView);
            btnAdd = itemView.findViewById(R.id.addbutton);
            btnPositive = itemView.findViewById(R.id.positiveIcon);
            btnNegative = itemView.findViewById(R.id.negativeIcon);
            cardUNO = itemView.findViewById(R.id.cardUNO);
            cardDOS = itemView.findViewById(R.id.cardDOS);
            cardTRES = itemView.findViewById(R.id.cardTRES);
            cardQUATRO = itemView.findViewById(R.id.cardQUATRO);
            cardSINCO = itemView.findViewById(R.id.cardSINCO);


            // Select the "uno" card as default
            setSelectedCardView(cardUNO, R.color.sizecolor);
            setSelectedCardView(cardDOS, R.color.sizecolor);
            setSelectedCardView(cardTRES, R.color.sizecolor);
            setSelectedCardView(cardQUATRO, R.color.sizecolor);
            setSelectedCardView(cardSINCO, R.color.sizecolor);
        }



        // Method to reset the selected state for all cardViews
        private void resetCardViewSelectedState() {
            cardUNO.setSelected(false);
            cardDOS.setSelected(false);
            cardTRES.setSelected(false);
            cardQUATRO.setSelected(false);
            cardSINCO.setSelected(false);
            setCardViewBackground(cardUNO, R.color.sizecolor);
            setCardViewBackground(cardDOS, R.color.sizecolor);
            setCardViewBackground(cardTRES, R.color.sizecolor);
            setCardViewBackground(cardQUATRO, R.color.sizecolor);
            setCardViewBackground(cardSINCO, R.color.sizecolor);
        }

        // Method to set the selected state for the clicked cardView
        private void setSelectedCardView(CardView cardView, int colorResId) {
            cardView.setSelected(true);
            setCardViewBackground(cardView, colorResId);
        }

        // Method to set the background color of a cardView
        private void setCardViewBackground(CardView cardView, int colorResId) {
            cardView.setCardBackgroundColor(itemView.getContext().getResources().getColor(colorResId));
        }
    }
}