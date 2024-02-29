package com.example.sincopossystemfullversion.Fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.sincopossystemfullversion.Adapter.BillingAdapter;
import com.example.sincopossystemfullversion.Adapter.ProductAdapter;
import com.example.sincopossystemfullversion.Adapter.ProductNew;
import com.example.sincopossystemfullversion.RecycleViewFix.GridSpacingItemDecoration;
import com.example.sincopossystemfullversion.Java.Product;
import com.example.sincopossystemfullversion.ProductsModel;
import com.example.sincopossystemfullversion.R;
import com.example.sincopossystemfullversion.Activity.CheckoutDialogFragment;
import com.example.sincopossystemfullversion.print.BluetoothwithWifiprint;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProductsFragment extends Fragment implements ProductAdapter.OnAddButtonClickListener {

    private List<Product> addedProducts;
    private View view; // Store the fragment view

    private HorizontalScrollView horizontalScrollViewProducts;


    private ProductAdapter productAdapter;


    private ProductNew newAdapter;

    private SwipeRefreshLayout swipeRefreshLayout; // Added SwipeRefreshLayout

    private List<ProductsModel> selectedProducts = new ArrayList<>(); // Initialize the list

    private BillingAdapter billingAdapter;

    private List<CardView> cardViews;
    private ProgressBar loadingProgressBar;
    private double subtotal = 0.00;
    private double total = 0.00;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_product, container, false);


        // Initialize the list of CardViews
        cardViews = new ArrayList<>();
        cardViews.add(view.findViewById(R.id.AllCardView));
        cardViews.add(view.findViewById(R.id.CoffeeCardView));
        cardViews.add(view.findViewById(R.id.PastriesCardView));
        cardViews.add(view.findViewById(R.id.NoncoffeeCardView));
        cardViews.add(view.findViewById(R.id.CookiesCardView));
        cardViews.add(view.findViewById(R.id.TraditionalCoffeeCardView));
        cardViews.add(view.findViewById(R.id.SincoFloatersCardView));
        cardViews.add(view.findViewById(R.id.MockTailsCardView));
        cardViews.add(view.findViewById(R.id.SincoSignatureCardView));


        // Initialize the loading progress bar
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);

        // Show loading progress bar when data loading starts
        loadingProgressBar.setVisibility(View.VISIBLE);


        // Set OnClickListener for each card view
        for (int i = 0; i < cardViews.size(); i++) {
            final int index = i;
            cardViews.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Reset the background color and text color of all card views
                    for (CardView cardView : cardViews) {
                        cardView.setCardBackgroundColor(getResources().getColor(R.color.lightcolor)); // Change to your normal color
                    }
                    // Reset the text color of all navigation labels
                    resetNavigationTextColors();

                    // Highlight the clicked card view
                    cardViews.get(index).setCardBackgroundColor(getResources().getColor(R.color.selectednav)); // Change to your selected color

                    // Highlight the corresponding navigation label
                    highlightNavigationLabel(index);

                    String productType;
                    switch (index) {
                        case 0: // AllCardView
                            // Reset the query to retrieve all products
                            productType = null;
                            break;
                        case 1: // CoffeeCardView
                            productType = "Coffee";
                            break;
                        case 2: // PastriesCardView
                            productType = "Pastries";
                            break;
                        case 3: // NoncoffeeCardView
                            productType = "Non-Coffee";
                            break;
                        case 4: // CookiesCardView
                            productType = "Cookies";
                            break;
                        case 5: // TraditionalCoffeeCardView
                            productType = "Traditional Coffee";
                            break;
                        case 6: // SincoFloatersCardView
                            productType = "Sinco Floaters";
                            break;
                        case 7: // MockTailsCardView
                            productType = "MockTails";
                            break;
                        case 8: // SincoSignatureCardView
                            productType = "Sinco Signature";
                            break;
                        // Add cases for other card views as needed
                        default:
                            productType = null;
                            break;
                    }
                    // Update the query based on the selected product type
                    updateProductQuery(productType);
                }
            });
        }

        // Reset the background color and text color of all card views
        for (CardView cardView : cardViews) {
            cardView.setCardBackgroundColor(getResources().getColor(R.color.lightcolor)); // Change to your normal color
        }
        // Reset the text color of all navigation labels
        resetNavigationTextColors();

        // Highlight the "All" card view
        cardViews.get(0).setCardBackgroundColor(getResources().getColor(R.color.selectednav)); // Change to your selected color
        // Highlight the corresponding navigation label
        highlightNavigationLabel(0);


        horizontalScrollViewProducts = view.findViewById(R.id.HorizontalScrollViewProducts);
        horizontalScrollViewProducts.setHorizontalScrollBarEnabled(false);

        // Find the SwipeRefreshLayout by its ID
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayoutProducts);

        // Set the listener for the refresh action
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Your refresh logic goes here

            // After completing the refresh, don't forget to stop the refreshing animation
            swipeRefreshLayout.setRefreshing(false);
        });

        // Initialize addedProducts list
        addedProducts = new ArrayList<>();

        // Instantiate the BillingAdapter and RecyclerView
        billingAdapter = new BillingAdapter(requireContext(), new ArrayList<>());
        RecyclerView billingRecyclerView = view.findViewById(R.id.RecyclerViewProductsInfo);
        billingRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        billingRecyclerView.setAdapter(billingAdapter);

        // Set up RecyclerView for added products
        // Find your RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.addedProductsRecyclerView);

        // Define the number of columns in the grid
        int spanCount = 3; // Assuming you have 3 columns in your grid
        // Define the spacing in pixels
        int spacing = 16; // You can adjust this value as needed
        // Create a new instance of the item decoration
        GridSpacingItemDecoration itemDecoration = new GridSpacingItemDecoration(spanCount, spacing, true);
        // Set the item decoration to your RecyclerView
        recyclerView.addItemDecoration(itemDecoration);

        productAdapter = new ProductAdapter(addedProducts, requireContext(), this);
        recyclerView.setAdapter(productAdapter);

        // Initial query for products
        updateProductQuery(null);

        // Set up a click listener for the Checkout button
        Button checkoutButton = view.findViewById(R.id.checkoutButton);
        checkoutButton.setOnClickListener(v -> showCustomDialog());

        // Set up a click listener for the Clear button
        TextView clearButton = view.findViewById(R.id.Clearbutton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear the selected products in the BillingAdapter
                billingAdapter.getSelectedProducts().clear();

                // Clear the selected products list in the fragment
                selectedProducts.clear();

                // Notify the BillingAdapter to update the view
                billingAdapter.notifyDataSetChanged();

                // Clear subtotal and total
                clearBillingDetails();
            }
        });

        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                txtSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                txtSearch(query);
                return false;
            }
        });


        return view;
    }


    private void txtSearch(String str) {
        String capitalizedStr = capitalizeSearch(str);

        FirebaseRecyclerOptions<ProductsModel> options =
                new FirebaseRecyclerOptions.Builder<ProductsModel>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("products")
                                        .orderByChild("product_name")
                                        .startAt(capitalizedStr)
                                        .endAt(capitalizedStr + "~"),
                                ProductsModel.class)
                        .build();

        newAdapter = new ProductNew(options, billingAdapter);
        newAdapter.startListening();
        RecyclerView recyclerView = view.findViewById(R.id.addedProductsRecyclerView);
        recyclerView.setAdapter(newAdapter);
    }

    private String capitalizeSearch(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    // Method to reset the text color of all navigation labels
    private void resetNavigationTextColors() {
        TextView[] navigationLabels = new TextView[]{
                view.findViewById(R.id.TextAll),
                view.findViewById(R.id.TextCoffee),
                view.findViewById(R.id.TextPastries),
                view.findViewById(R.id.TextNoncoffee),
                view.findViewById(R.id.TextCookies),
                view.findViewById(R.id.TextTraditionalCoffee),
                view.findViewById(R.id.TextSincoFloaters),
                view.findViewById(R.id.TextMockTails),
                view.findViewById(R.id.TextSincoSignature)
        };
        for (TextView textView : navigationLabels) {
            textView.setTextColor(getResources().getColor(R.color.selectednav));
        }
    }

    // Method to highlight the navigation label corresponding to the selected card view
    private void highlightNavigationLabel(int index) {
        int[] navigationLabelsIds = new int[]{
                R.id.TextAll,
                R.id.TextCoffee,
                R.id.TextPastries,
                R.id.TextNoncoffee,
                R.id.TextCookies,
                R.id.TextTraditionalCoffee,
                R.id.TextSincoFloaters,
                R.id.TextMockTails,
                R.id.TextSincoSignature
                // Add more TextView IDs here if needed
        };
        for (int i = 0; i < navigationLabelsIds.length; i++) {
            TextView textView = view.findViewById(navigationLabelsIds[i]);
            if (i == index) {
                // Highlight the selected navigation label
                textView.setTextColor(getResources().getColor(R.color.white));
            } else {
                // Reset the color of unselected navigation labels
                textView.setTextColor(getResources().getColor(R.color.selectednav));
            }
        }
    }


    private void updateProductQuery(String productType) {
        // Show loading progress bar
        loadingProgressBar.setVisibility(View.VISIBLE);

        Query query;
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child("products");

        if (productType != null) {
            // Filter products by product type
            query = productsRef.orderByChild("product_type").equalTo(productType);
        } else {
            // No filtering, retrieve all non-archived products
            query = productsRef.orderByChild("archived").equalTo(false);
        }

        FirebaseRecyclerOptions<ProductsModel> options =
                new FirebaseRecyclerOptions.Builder<ProductsModel>()
                        .setQuery(query, ProductsModel.class)
                        .build();

        newAdapter = new ProductNew(options, billingAdapter) {
            @Override
            protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull ProductsModel model) {
                super.onBindViewHolder(holder, position, model);

                holder.btnAdd.setOnClickListener(v -> {
                    // Add the clicked product to the selectedProducts list in billingAdapter
                    billingAdapter.getSelectedProducts().add(model);
                    // Notify the billingAdapter to update the view
                    billingAdapter.notifyDataSetChanged();

                    // Update subtotal, total, and TextViews
                    updateBillingDetails();

                    // Add the selected product to the ProductsFragment's selectedProducts list
                    selectedProducts.add(model);
                });
            }
        };

        RecyclerView recyclerView = view.findViewById(R.id.addedProductsRecyclerView);
        recyclerView.setAdapter(newAdapter);
        newAdapter.startListening();

        // Hide loading progress bar when data loading completes
        newAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                // Hide loading progress bar
                loadingProgressBar.setVisibility(View.GONE);
            }
        });
    }


    private void showCustomDialog() {
        // Pass the selected products to the CheckoutDialogFragment
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("selectedProductsFromProductNewAdapter", new ArrayList<>(selectedProducts));

        CheckoutDialogFragment checkoutDialog = new CheckoutDialogFragment();
        checkoutDialog.setArguments(bundle);

        checkoutDialog.show(requireActivity().getSupportFragmentManager(), "checkout_dialog");
    }

    @Override
    public void onAddButtonClick(int position) {
        // Implement your logic for the add button click
        Toast.makeText(requireContext(), "Add button clicked at position: " + position, Toast.LENGTH_SHORT).show();
    }

    // Updated method to handle Checkout button click
    public void onCheckoutButtonClick() {
        // Implement your logic for the Checkout button click
        Toast.makeText(requireContext(), "Checkout button clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPositiveClick(int position) {
        // Implement your logic for the positive button click
        newAdapter.increaseQuantity(getContext(), position);
    }

    @Override
    public void onNegativeClick(int position) {
        // Implement your logic for the negative button click
        newAdapter.decreaseQuantity(getContext(), position);
    }


    // Add a method to update subtotal, total, and TextViews
    private void updateBillingDetails() {
        subtotal = calculateSubtotal(); // Implement a method to calculate subtotal based on selected products
        total = subtotal;    // Subtract the discount

        // Update TextViews with new values
        TextView subtotalTextView = view.findViewById(R.id.subtotalTextView);
        TextView totalTextView = view.findViewById(R.id.totalTextView);

        // Format subtotal and total with comma for thousands separator and two decimal places
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        String formattedSubtotal = "₱" + decimalFormat.format(subtotal);
        String formattedTotal = "₱" + decimalFormat.format(total);

        subtotalTextView.setText("Subtotal: " + formattedSubtotal);
        totalTextView.setText("Total: " + formattedTotal);
    }

    // Add a method to calculate subtotal
    private double calculateSubtotal() {
        double subtotal = 0.00;

        // Iterate through selected products and sum up their costs
        List<ProductsModel> selectedProducts = billingAdapter.getSelectedProducts();
        for (ProductsModel product : selectedProducts) {
            subtotal += product.getProduct_cost() * product.getQuantity();
        }

        return subtotal;
    }

    // Add a method to clear subtotal and total when the "Clear" button is clicked
    private void clearBillingDetails() {
        subtotal = 0.00;
        total = 0.00;

        // Update TextViews with new values
        TextView subtotalTextView = view.findViewById(R.id.subtotalTextView);
        TextView totalTextView = view.findViewById(R.id.totalTextView);

        subtotalTextView.setText("Subtotal: ₱0.00");
        totalTextView.setText("Total: ₱0.00");
    }

}