package com.example.sincopossystemfullversion.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincopossystemfullversion.Activity.CheckoutDialogFragment;
import com.example.sincopossystemfullversion.Adapter.ProductAdapter;
import com.example.sincopossystemfullversion.Adapter.ProductNew;
import com.example.sincopossystemfullversion.Java.Product;
import com.example.sincopossystemfullversion.ProductsModel;
import com.example.sincopossystemfullversion.R;
import com.example.sincopossystemfullversion.SharedViewModel.SharedViewModel;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ProductsFragment extends Fragment implements ProductAdapter.OnAddButtonClickListener {

    private List<Product> addedProducts;
    private View view; // Store the fragment view
    private ProductAdapter productAdapter;
    private SharedViewModel sharedViewModel; // Declare the shared ViewModel
    private TextView billTextView, quantityTextView, editProducts; // Reference to the TextView for the bill
    private boolean isAddButtonClicked = false; // Track whether the add button has been clicked
    private androidx.cardview.widget.CardView CashCardView;
    private androidx.cardview.widget.CardView GcashCardView;
    private android.widget.ImageView imageView1;
    private android.widget.ImageView imageView2;

    ProductNew newAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_product, container, false);

        // Initialize the shared ViewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Initialize addedProducts list
        addedProducts = new ArrayList<>();

        // Set up RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 3); // Set the span count to 3
        recyclerView.setLayoutManager(gridLayoutManager);

        productAdapter = new ProductAdapter(addedProducts, requireContext(), this);
        recyclerView.setAdapter(productAdapter);

        //New
        FirebaseRecyclerOptions<ProductsModel> options =
                new FirebaseRecyclerOptions.Builder<ProductsModel>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("products"), ProductsModel.class)
                        .build();

        ProductNew adapter = new ProductNew(options, requireContext());
        recyclerView.setAdapter(adapter);
        adapter.startListening();

        // Set up a click listener for the Clear button
        TextView clearButton = view.findViewById(R.id.Clearbutton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the "Clear" button click
                clearTextOnBill();
            }
        });


        // Set up a click listener for the Checkout button
        Button checkoutButton = view.findViewById(R.id.checkoutButton);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Display the checkout confirmation dialog
                showCustomDialog();
            }
        });

        // Initialize the TextView for the bill
        billTextView = view.findViewById(R.id.addedProductsTextView);

        // Initialize PaymentMethod CardViews and ImageViews
        CashCardView = view.findViewById(R.id.CashCardView);
        GcashCardView = view.findViewById(R.id.GcashCardView);
        imageView1 = view.findViewById(R.id.imageView1);
        imageView2 = view.findViewById(R.id.imageView2);

        // Set up click listeners for the CardViews
        CashCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePaymentMethodClick(CashCardView, GcashCardView, "Cash");
            }
        });

        GcashCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePaymentMethodClick(GcashCardView, CashCardView, "GCash");
            }
        });

        // Observe changes in the shared ViewModel
        sharedViewModel.getProductList().observe(getViewLifecycleOwner(), new Observer<List<Product>>() {
            @Override
            public void onChanged(List<Product> products) {
                if (products != null) {
                    addedProducts.clear();
                    addedProducts.addAll(products);
                    // Update the new adapter
                    newAdapter.notifyDataSetChanged();

                    // Update the billing text view only when the user clicks the add button
                    if (isAddButtonClicked) {
                        updateBillingTextView();
                    }
                }
            }
        });

        return view;
    }


    @Override
    public void onAddButtonClick(int position) {
        Product selectedProduct = productAdapter.getSelectedProduct(position);

        if (selectedProduct != null) {
            if (selectedProduct.getQuantity() > 0) {
                if (!addedProducts.contains(selectedProduct)) {
                    // Product is not in the list, add it
                    addedProducts.add(selectedProduct);
                } else {
                    // Product is already in the list, increase the quantity
                    int index = addedProducts.indexOf(selectedProduct);
                    Product existingProduct = addedProducts.get(index);
                    existingProduct.setQuantity(existingProduct.getQuantity() + 1);
                }

                // Update the billing text view
                updateBillingTextView();

                // Update the ViewModel
                sharedViewModel.updateUpdatedBill(addedProducts);

                // Update isAddButtonClicked to true when the add button is clicked
                isAddButtonClicked = true;
            } else {
                Toast.makeText(requireContext(), "Quantity should be above 0", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Inside ProductsFragment class
    public void onPositiveClick(int position) {
        // Increase the quantity when the positive button is clicked
        productAdapter.increaseQuantity(position);

        // Update the billing text view
        updateBillingTextView();

        // Inflate the layout containing the quantityTextView
        View otherLayout = getLayoutInflater().inflate(R.layout.card_layout, null);

        // Find the TextView in the inflated layout
        TextView quantityTextView = otherLayout.findViewById(R.id.quantityTextView);

        // Get the current value from the TextView
        int currentValue = Integer.parseInt(quantityTextView.getText().toString());

        // Increment the value by 1
        int incrementedValue = currentValue + 1;

        // Update the TextView with the incremented value
        quantityTextView.setText(String.valueOf(incrementedValue));
    }



    @Override
    public void onNegativeClick(int position) {
        // Decrease the quantity when the negative button is clicked
        productAdapter.decreaseQuantity(position);
        // Update the billing text view
        updateBillingTextView();
    }

    // Clear the text on the bill without removing products
    private void clearTextOnBill() {
        // Clear text on the bill
        billTextView.setText("");

        // Reset isAddButtonClicked to false when the text view is cleared
        isAddButtonClicked = false;
    }

    private void updateBillingTextView() {
        double subtotal = 0;
        double totalDiscount = 0;

        StringBuilder productsStringBuilder = new StringBuilder();
        for (Product product : addedProducts) {
            if (product.getQuantity() > 0) {
                productsStringBuilder.append(product.getTitle())
                        .append(" - ")
                        .append(product.getPrice())
                        .append(" - Qty: ")
                        .append(product.getQuantity())
                        .append("\n");

                double productSubtotal = parsePriceNumeric(product.getPrice()) * product.getQuantity();
                subtotal += productSubtotal;

                totalDiscount += (productSubtotal * product.getDiscount() / 100);
            }
        }

        // Display the products in a separate TextView
        billTextView.setText(productsStringBuilder.toString());

        // Display subtotal in a separate TextView
        TextView subtotalTextView = view.findViewById(R.id.subtotalTextView);
        subtotalTextView.setText("Subtotal: ₱" + subtotal);

        // Display total discount in a separate TextView
        TextView discountTextView = view.findViewById(R.id.discountTextView);
        discountTextView.setText("Total Discount: ₱" + totalDiscount);

        // Calculate total and display it in a separate TextView
        double total = subtotal - totalDiscount;
        TextView totalTextView = view.findViewById(R.id.totalTextView);
        totalTextView.setText("Total: ₱" + total);
    }

    // Helper method to parse the numeric part of the price string
    private double parsePriceNumeric(String price) {
        String numericPart = price.replaceAll("[^0-9.]", "");
        return Double.parseDouble(numericPart);
    }

    // Display the custom dialog for checkout
    private void showCustomDialog() {
        // Pass the selected products and payment method to the CheckoutDialogFragment
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("chosenProducts", new ArrayList<>(addedProducts));
        bundle.putString("selectedPaymentMethod", sharedViewModel.getSelectedPaymentMethod().getValue());

        CheckoutDialogFragment checkoutDialog = new CheckoutDialogFragment();
        checkoutDialog.setArguments(bundle);

        checkoutDialog.show(requireActivity().getSupportFragmentManager(), "checkout_dialog");
    }

    // Helper method to handle payment method clicks
    private void handlePaymentMethodClick(
            androidx.cardview.widget.CardView clickedCardView,
            androidx.cardview.widget.CardView otherCardView,
            String paymentMethod) {
        // Reset the background color for all CardViews
        CashCardView.setCardBackgroundColor(getResources().getColor(R.color.lightcolor));
        GcashCardView.setCardBackgroundColor(getResources().getColor(R.color.lightcolor));

        // Set the clicked CardView background color
        clickedCardView.setCardBackgroundColor(getResources().getColor(R.color.selectednav));

        // Store the selected payment method in the shared ViewModel
        sharedViewModel.setSelectedPaymentMethod(paymentMethod);

        // Display a toast indicating the selected payment method
        Toast.makeText(requireContext(), "Selected Payment Method: " + paymentMethod, Toast.LENGTH_SHORT).show();

        // Observe changes in the selectedPaymentMethod in the shared ViewModel
        sharedViewModel.getSelectedPaymentMethod().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String selectedPaymentMethod) {
                if (selectedPaymentMethod != null && !selectedPaymentMethod.equals("N/A")) {
                    // Do something with the selected payment method
                    // For example, update UI or perform other actions based on the selected payment method
                }
            }
        });
    }
}