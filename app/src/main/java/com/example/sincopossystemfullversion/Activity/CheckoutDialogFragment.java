package com.example.sincopossystemfullversion.Activity;

import static com.example.sincopossystemfullversion.SalesReportModel.generateTransactionId;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincopossystemfullversion.Adapter.CheckoutAdapter;
import com.example.sincopossystemfullversion.Fragments.ProductsFragment;
import com.example.sincopossystemfullversion.ProductsModel;
import com.example.sincopossystemfullversion.R;
import com.example.sincopossystemfullversion.SalesReportModel;
import com.example.sincopossystemfullversion.print.BluetoothwithWifiprint;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CheckoutDialogFragment extends DialogFragment {


    // Define a key for SharedPreferences
    private static final String PREF_BUTTON_PRESS_COUNT = "button_press_count";

    // Define a variable to keep track of the count
    private int buttonPressCount = 0;
    private int groundCoffee = 0;
    private int tea = 0;
    private int milkAndCream = 0;
    private int sweeteners = 0;
    private int syrupsAndFlavorings = 0;
    private int ice = 0;


    private SharedPreferences sharedPreferences;


    private List<ProductsModel> selectedProducts;
    private RecyclerView checkoutRecyclerView;
    private CheckoutAdapter checkoutAdapter;

    private TextView subtotalTextView, totalTextView, changeTextView;
    private EditText cashInputEditText;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        sharedPreferences = requireContext().getSharedPreferences("IngredientCounts", Context.MODE_PRIVATE);
        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);


        // Retrieve selected products from arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            selectedProducts = arguments.getParcelableArrayList("selectedProductsFromProductNewAdapter");
        } else {
            // Initialize with an empty list if selectedProducts is null
            selectedProducts = new ArrayList<>();
        }


        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.checkout_popout);


        // Retrieve the stored buttonPressCount value
        buttonPressCount = sharedPreferences.getInt(PREF_BUTTON_PRESS_COUNT, 0);

        groundCoffee = sharedPreferences.getInt("groundCoffeeCount", 0);
        tea = sharedPreferences.getInt("teaCount", 0);
        milkAndCream = sharedPreferences.getInt("milkAndCreamCount", 0);
        sweeteners = sharedPreferences.getInt("sweetenersCount", 0);
        syrupsAndFlavorings = sharedPreferences.getInt("syrupsAndFlavoringsCount", 0);
        ice = sharedPreferences.getInt("iceCount", 0);


        checkoutRecyclerView = dialog.findViewById(R.id.CheckoutRv);
        checkoutAdapter = new CheckoutAdapter(selectedProducts);
        checkoutRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        checkoutRecyclerView.setAdapter(checkoutAdapter);

        // Initialize TextViews and EditText
        subtotalTextView = dialog.findViewById(R.id.CheckoutSubTotal);
        totalTextView = dialog.findViewById(R.id.CheckoutTotal);
        changeTextView = dialog.findViewById(R.id.CheckoutChange);
        cashInputEditText = dialog.findViewById(R.id.CashInput);


        // Set up a text change listener for the cash input
        cashInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Not needed in this case
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Update the billing details when the user types the amount
                updateBillingDetails();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Not needed in this case
            }
        });

        // Find PrintCardView and set OnClickListener
        CardView printCardView = dialog.findViewById(R.id.PrintCardView);
        printCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Log the intent information before starting the Bluetooth printing activity
                Log.d("PrintCardView", "Intent data:");

                // Log product details
                for (ProductsModel product : selectedProducts) {
                    if (product != null) {
                        Log.d("PrintCardView", "Product: " + product.getProduct_name() +
                                ", Qty: " + product.getQuantity() +
                                ", Cost: ₱" + product.getProduct_cost());
                    }
                }

                // Create an intent to start the Bluetooth printing activity
                Intent intent = new Intent(requireContext(), BluetoothwithWifiprint.class);

                // Pass the required data as extras to the intent
                intent.putParcelableArrayListExtra("selectedProducts", (ArrayList<? extends Parcelable>) selectedProducts);
                intent.putExtra("subtotal", calculateSubtotal());
                intent.putExtra("cashAmount", getCashAmount());
                intent.putExtra("change", changeTextView.getText().toString());

                // Log the extras passed to the intent
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    for (String key : extras.keySet()) {
                        Object value = extras.get(key);
                        Log.d("PrintCardView", String.format("%s: %s", key, value.toString()));
                    }
                }

                // Start the Bluetooth printing activity
                startActivity(intent);

                // Show a toast indicating that printing is initiated
                Toast.makeText(requireContext(), "Print Receipt Clicked!", Toast.LENGTH_SHORT).show();

                // Dismiss the dialog
                dismiss();

            }
        });

        Button btnBack = dialog.findViewById(R.id.backbutton);
        Button btnConfirm = dialog.findViewById(R.id.Confirmbutton);


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(requireContext(), "Confirmed!", Toast.LENGTH_SHORT).show();

                // Iterate over selected products and log details
                for (ProductsModel product : selectedProducts) {
                    Log.d("ProductDetails", "Product Name: " + product.getProduct_name() +
                            ", Quantity: " + product.getQuantity() +
                            ", Cost: " + product.getProduct_cost());
                }

                accessFirebaseProducts();

                // Increment the button press count
                buttonPressCount++;

                // Log the button press count to Logcat
                Log.d("ButtonPressCount", "Button Press Count: " + buttonPressCount);

                // If count reaches 10, reset it to 0
                if (buttonPressCount == 10) {
                    buttonPressCount = 0;
                    Log.d("ButtonPressCount", "Button Press Count: " + buttonPressCount + " (Reset)");
                }

                // Save the updated count to SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(PREF_BUTTON_PRESS_COUNT, buttonPressCount);
                editor.apply();

                List<SalesReportModel> salesReportData = new ArrayList<>();
                for (ProductsModel product : selectedProducts) {
                    SalesReportModel salesReportModel = new SalesReportModel();
                    salesReportModel.setSr_date(getCurrentDate());
                    salesReportModel.setSr_transactionid(generateTransactionId());
                    salesReportModel.setSr_product(product.getProduct_name());
                    salesReportModel.setSr_qty(product.getQuantity());
                    salesReportModel.setSr_price(product.getProduct_cost());
                    salesReportModel.setSr_total(product.getProduct_cost() * product.getQuantity());
                    salesReportModel.setSr_payment("Cash");

                    salesReportData.add(salesReportModel);
                }

                // Update the checkout RecyclerView with selected products
                checkoutAdapter.updateData(selectedProducts);


                addSalesReportDataToDatabase(salesReportData);

                Intent intent = new Intent(requireContext(), BluetoothwithWifiprint.class);

                // Pass the required data as extras to the intent
                intent.putParcelableArrayListExtra("selectedProducts", (ArrayList<? extends Parcelable>) selectedProducts);
                intent.putExtra("subtotal", calculateSubtotal());
                intent.putExtra("total", totalTextView.getText().toString());
                intent.putExtra("cashAmount", getCashAmount());
                intent.putExtra("change", changeTextView.getText().toString());

                // Start the Bluetooth printing activity
                startActivity(intent);

                dismiss();


            }
        });

        return dialog;
    }


    private void accessFirebaseProducts() {
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child("products");
        DatabaseReference ingredientsRef = FirebaseDatabase.getInstance().getReference().child("ingredients");

        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    String productName = productSnapshot.child("product_name").getValue(String.class);
                    String ingredientType = productSnapshot.child("ingredient_type").getValue(String.class);

                    // Logging for debugging
                    Log.d("FirebaseDebug", "Product Name: " + productName + ", Ingredient Type: " + ingredientType);

                    // Check if the product is in the selected products
                    for (ProductsModel product : selectedProducts) {
                        if (product.getProduct_name().equals(productName)) {
                            // Increment the respective counter based on ingredient type and quantity
                            switch (ingredientType) {
                                case "Ground coffee":
                                    Log.d("FirebaseDebug", "Entering Ground Coffee switch case");
                                    groundCoffee += product.getQuantity(); // Increment by the quantity of the item
                                    Log.d("IngredientCount", "Ground Coffee Count: " + groundCoffee); // Log the ground coffee count
                                    if (groundCoffee >= 100) { // Check if threshold is met
                                        updateIngredientQuantity(ingredientsRef, ingredientType, groundCoffee / 100); // Update ingredient quantity
                                        groundCoffee %= 100; // Reset counter
                                        Log.d("FirebaseDebug", "Ground Coffee threshold reached. Updated count: " + groundCoffee);
                                    }
                                    break;
                                case "Tea":
                                    Log.d("FirebaseDebug", "Entering Tea switch case");
                                    tea += product.getQuantity();
                                    Log.d("IngredientCount", "Tea Count: " + tea);
                                    if (tea >= 100) {
                                        updateIngredientQuantity(ingredientsRef, ingredientType, tea / 10);
                                        tea %= 100;
                                        Log.d("FirebaseDebug", "Tea threshold reached. Updated count: " + tea);
                                    }
                                    break;
                                case "Milk and Cream":
                                    Log.d("FirebaseDebug", "Entering Milk and Cream switch case");
                                    milkAndCream += product.getQuantity();
                                    Log.d("IngredientCount", "Milk and Cream Count: " + milkAndCream);
                                    if (milkAndCream >= 100) {
                                        updateIngredientQuantity(ingredientsRef, ingredientType, milkAndCream / 10);
                                        milkAndCream %= 100;
                                        Log.d("FirebaseDebug", "Milk and Cream threshold reached. Updated count: " + milkAndCream);
                                    }
                                    break;
                                case "Sweeteners":
                                    Log.d("FirebaseDebug", "Entering Sweeteners switch case");
                                    sweeteners += product.getQuantity();
                                    Log.d("IngredientCount", "Sweeteners Count: " + sweeteners);
                                    if (sweeteners >= 40) {
                                        updateIngredientQuantity(ingredientsRef, ingredientType, sweeteners / 10);
                                        sweeteners %= 40;
                                        Log.d("FirebaseDebug", "Sweeteners threshold reached. Updated count: " + sweeteners);
                                    }
                                    break;
                                case "Syrups and flavorings":
                                    Log.d("FirebaseDebug", "Entering Syrups and Flavorings switch case");
                                    syrupsAndFlavorings += product.getQuantity();
                                    Log.d("IngredientCount", "Syrups and Flavorings Count: " + syrupsAndFlavorings);
                                    if (syrupsAndFlavorings >= 40) {
                                        updateIngredientQuantity(ingredientsRef, ingredientType, syrupsAndFlavorings / 10);
                                        syrupsAndFlavorings %= 40;
                                        Log.d("FirebaseDebug", "Syrups and Flavorings threshold reached. Updated count: " + syrupsAndFlavorings);
                                    }
                                    break;
                                case "Ice":
                                    Log.d("FirebaseDebug", "Entering Ice switch case");
                                    ice += product.getQuantity();
                                    Log.d("IngredientCount", "Ice Count: " + ice);
                                    if (ice >= 10) {
                                        updateIngredientQuantity(ingredientsRef, ingredientType, ice / 10);
                                        ice %= 10;
                                        Log.d("FirebaseDebug", "Ice threshold reached. Updated count: " + ice);
                                    }
                                    break;

                            }
                            break;
                        }
                    }
                }


                saveIngredientCounts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Error accessing Firebase products: " + databaseError.getMessage());
            }
        });
    }

    private void updateIngredientQuantity(DatabaseReference ingredientsRef, String ingredientType, int quantity) {
        ingredientsRef.orderByChild("ingredient_type").equalTo(ingredientType).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ingredientSnapshot : dataSnapshot.getChildren()) {
                    int currentQuantity = ingredientSnapshot.child("ingredient_qty").getValue(Integer.class);
                    if (currentQuantity >= quantity) {
                        // Subtract the ingredient quantity by the specified quantity
                        ingredientsRef.child(ingredientSnapshot.getKey()).child("ingredient_qty").setValue(currentQuantity - quantity);
                    } else {
                        Log.e("IngredientError", "Insufficient quantity of ingredient: " + ingredientType);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Error updating ingredient quantity: " + databaseError.getMessage());
            }
        });
    }

    private void saveIngredientCounts() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("groundCoffeeCount", groundCoffee);
        editor.putInt("teaCount", tea);
        editor.putInt("milkAndCreamCount", milkAndCream);
        editor.putInt("sweetenersCount", sweeteners);
        editor.putInt("syrupsAndFlavoringsCount", syrupsAndFlavorings);
        editor.putInt("iceCount", ice);
        editor.apply();
    }


    private String getCurrentDate() {
        // Get the current date
        // Set the time zone to Philippine time
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Manila");
        Calendar calendar = Calendar.getInstance(timeZone);

        // Get the current date and time in Philippine time
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateTimeFormat.setTimeZone(timeZone);

        return dateTimeFormat.format(calendar.getTime());
    }


    private void addSalesReportDataToDatabase(List<SalesReportModel> salesReportData) {
        // Get a reference to the Firebase Database
        DatabaseReference salesReportRef = FirebaseDatabase.getInstance().getReference().child("sales_report");

        // Iterate through the salesReportData and add each SalesReportModel to the database
        for (SalesReportModel salesReportModel : salesReportData) {
            String key = salesReportRef.push().getKey();
            if (key != null) {
                salesReportRef.child(key).setValue(salesReportModel);
            }
        }
    }


    private void updateBillingDetails() {

        double subtotal = calculateSubtotal();



        double total = subtotal;


        double cashAmount = 0.00;
        if (!cashInputEditText.getText().toString().isEmpty()) {
            cashAmount = Double.parseDouble(cashInputEditText.getText().toString());
        }

        // Calculate change
        double change = cashAmount - total;



        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        String formattedSubtotal = "₱" + decimalFormat.format(subtotal);
        String formattedTotal = "₱" + decimalFormat.format(total);
        String formattedChange = "₱" + decimalFormat.format(change);


        subtotalTextView.setText("Subtotal: " + formattedSubtotal);
        totalTextView.setText("Total: " + formattedTotal);
        changeTextView.setText("Change: " + formattedChange);
    }


    private double calculateSubtotal() {
        double subtotal = 0.00;

        // Iterate through selected products and sum up their costs
        for (ProductsModel product : selectedProducts) {
            subtotal += product.getProduct_cost() * product.getQuantity();
        }

        return subtotal;
    }


    public double getTotalPrice() {
        return calculateSubtotal();
    }

    public double getCashAmount() {
        // Retrieve the text from the EditText field
        String cashInputText = cashInputEditText.getText().toString();

        // Check if the input is not empty
        if (!cashInputText.isEmpty()) {
            try {
                return Double.parseDouble(cashInputText);
            } catch (NumberFormatException e) {
                Log.e("Error", "Invalid cash input: " + cashInputText);
                return 0.0; // Return 0.0 as default value or handle error as appropriate
            }
        } else {
            Log.e("Error", "Empty cash input");
            return 0.0; // Return 0.0 as default value or handle error as appropriate
        }
    }

    public double calculateChange(double cashAmount, double totalPrice) {
        return cashAmount - totalPrice;
    }

}
