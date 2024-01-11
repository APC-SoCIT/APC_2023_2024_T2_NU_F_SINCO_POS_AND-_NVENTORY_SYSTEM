package com.example.sincopossystemfullversion.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sincopossystemfullversion.Activity.CheckoutDialogFragment;
import com.example.sincopossystemfullversion.R;

public class SalesReportFragment extends Fragment implements CheckoutDialogFragment.CheckoutDialogListener {

    private TableLayout dataTable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sales_report, container, false);

        dataTable = view.findViewById(R.id.dataTable);


        return view;
    }

    // Implement the listener method to update the UI when an order is confirmed
    @Override
    public void onOrderConfirmed(String date, String transactionId, String product, int quantity, double price, double total, String paymentMethod) {
        // Create a new TableRow and add it to the DataTable
        TableRow row = new TableRow(requireContext());

        TextView dateTextView = createTextView(date);
        TextView transactionIdTextView = createTextView(transactionId);
        TextView productTextView = createTextView(product);
        TextView quantityTextView = createTextView(String.valueOf(quantity));
        TextView priceTextView = createTextView("₱" + String.format("%.2f", price));
        TextView totalTextView = createTextView("₱" + String.format("%.2f", total));
        TextView paymentMethodTextView = createTextView(paymentMethod);

        // Add TextViews to the TableRow
        row.addView(dateTextView);
        row.addView(transactionIdTextView);
        row.addView(productTextView);
        row.addView(quantityTextView);
        row.addView(priceTextView);
        row.addView(totalTextView);
        row.addView(paymentMethodTextView);

        // Add the TableRow to the DataTable
        dataTable.addView(row);
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(requireContext());
        textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        textView.setText(text);
        textView.setGravity(View.TEXT_ALIGNMENT_CENTER);
        textView.setPadding(8, 8, 8, 8);
        return textView;
    }
}

