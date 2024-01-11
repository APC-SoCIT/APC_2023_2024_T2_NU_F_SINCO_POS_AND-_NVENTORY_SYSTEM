package com.example.sincopossystemfullversion.Activity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.sincopossystemfullversion.Java.Product;
import com.example.sincopossystemfullversion.R;

import java.util.List;

public class CheckoutDialogFragment extends DialogFragment {

    private TextView productsTextView;
    private TextView subtotalTextView;
    private TextView discountTextView;
    private TextView paymentMethodTextView;

    private TextView totalTextView;
    private TextView cashTextView;
    private TextView changeTextView;
    private EditText cashInput;
    private Button confirmButton;

    private List<Product> chosenProducts;
    private String selectedPaymentMethod = "";
    private int confirmButtonClickCount = 0;

    private CheckoutDialogListener checkoutDialogListener; // Declare the listener

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.checkout_popout);

        // Retrieve selected products and payment method from arguments
        Bundle args = getArguments();
        if (args != null) {
            chosenProducts = args.getParcelableArrayList("chosenProducts");
            selectedPaymentMethod = args.getString("selectedPaymentMethod", "");
            setChosenProducts(chosenProducts);
        }

        productsTextView = dialog.findViewById(R.id.ProductsCheckout);
        subtotalTextView = dialog.findViewById(R.id.SubtotalCheckout);
        discountTextView = dialog.findViewById(R.id.DiscountCheckout);
        totalTextView = dialog.findViewById(R.id.TotalCheckout);
        cashTextView = dialog.findViewById(R.id.CashCheckout);
        changeTextView = dialog.findViewById(R.id.ChangeCheckout);
        confirmButton = dialog.findViewById(R.id.Confirmbutton);
        paymentMethodTextView = dialog.findViewById(R.id.PaymentMethodCheckout);
        cashInput = dialog.findViewById(R.id.cashInput);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmButtonClickCount++;
                updateValues();

                // Show toast on the second click
                if (confirmButtonClickCount == 2) {
                    // Pass the order information to the listener
                    if (checkoutDialogListener != null) {
                        checkoutDialogListener.onOrderConfirmed(
                                "2024-01-08",
                                "123456789",
                                "Product A",
                                2,
                                10.00,
                                20.00,
                                "Credit Card"
                        );
                        showToast("Order successful");
                        dismiss(); // Dismiss the dialog
                    }
                }
            }
        });

        Button btnBack = dialog.findViewById(R.id.backbutton);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setChosenProducts(chosenProducts);

        return dialog;
    }

    private void setChosenProducts(List<Product> chosenProducts) {
        this.chosenProducts = chosenProducts;

        if (productsTextView != null && chosenProducts != null) {
            StringBuilder productsText = new StringBuilder();
            for (Product product : chosenProducts) {
                productsText.append(product.getTitle())
                        .append(" - Qty: ")
                        .append(product.getQuantity())
                        .append("\n");
            }
            productsTextView.setText("" + productsText.toString());
        }
    }

    private void updateValues() {
        double subtotal = calculateSubtotal();

        // Get the user's input from the EditText
        String cashStr = cashInput.getText().toString();

        // Check if the input is not empty
        if (!cashStr.isEmpty()) {
            double cash = Double.parseDouble(cashStr);

            if (cash >= subtotal) {
                // Calculate the change only if the cash is sufficient
                double change = calculateChange(subtotal, cash);

                subtotalTextView.setText("Subtotal: ₱" + String.format("%.2f", subtotal));
                totalTextView.setText("Total: ₱" + String.format("%.2f", subtotal));
                cashTextView.setText("Cash: ₱" + String.format("%.2f", cash));
                changeTextView.setText("Change: ₱" + String.format("%.2f", change));

                // Set the payment method in the TextView
                paymentMethodTextView.setText("Payment Method: " + selectedPaymentMethod);
            } else {
                // Handle the case where the cash is insufficient
                cashTextView.setText("Cash: ₱" + String.format("%.2f", cash));
                changeTextView.setText("Insufficient Cash");
                paymentMethodTextView.setText("Payment Method: N/A");
            }
        } else {
            // Show a toast message if the input is empty
            showToast("Please enter the cash amount");
            cashTextView.setText("Cash: ₱0.00");
            changeTextView.setText("Change: ₱0.00");
            paymentMethodTextView.setText("Payment Method: N/A");
        }
    }

    private double calculateSubtotal() {
        double subtotal = 0;
        for (Product product : chosenProducts) {
            subtotal += product.getDiscountedPrice() * product.getQuantity();
        }
        return subtotal;
    }

    private double calculateChange(double total, double cash) {
        return cash - total;
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Define the interface for the listener
    public interface CheckoutDialogListener {
        void onOrderConfirmed(String date, String transactionId, String product, int quantity, double price, double total, String paymentMethod);
    }

    public void setCheckoutDialogListener(CheckoutDialogListener listener) {
        this.checkoutDialogListener = listener;
    }

}