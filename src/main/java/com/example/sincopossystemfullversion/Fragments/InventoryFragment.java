package com.example.sincopossystemfullversion.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.sincopossystemfullversion.R;

public class InventoryFragment extends Fragment {

    private TableLayout inventoryTable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_inventory, container, false);

        inventoryTable = root.findViewById(R.id.inventoryTable);

        ImageView addInventoryIcon = root.findViewById(R.id.addinventory);
        addInventoryIcon.setOnClickListener(view -> showAddInventoryDialog());

        return root;
    }

    private void showAddInventoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_inventory, null);

        // Reference the EditTexts in the dialog layout
        EditText codeEditText = dialogView.findViewById(R.id.editTextCode);
        EditText itemNameEditText = dialogView.findViewById(R.id.editTextItemName);
        EditText itemTypeEditText = dialogView.findViewById(R.id.editTextItemType);
        EditText qtyEditText = dialogView.findViewById(R.id.editTextQty);
        EditText itemCostEditText = dialogView.findViewById(R.id.editTextItemCost);
        EditText reorderEditText = dialogView.findViewById(R.id.editTextReorder);
        EditText notesEditText = dialogView.findViewById(R.id.editTextNotes);

        builder.setView(dialogView)
                .setTitle("Add Inventory")
                .setPositiveButton("Add", (dialogInterface, i) -> handleAddButtonClick(
                        codeEditText.getText().toString(),
                        itemNameEditText.getText().toString(),
                        itemTypeEditText.getText().toString(),
                        qtyEditText.getText().toString(),
                        itemCostEditText.getText().toString(),
                        reorderEditText.getText().toString(),
                        notesEditText.getText().toString()))
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

        builder.create().show();
    }

    private void handleAddButtonClick(String code, String itemName, String itemType, String qty,
                                      String itemCost, String reorder, String notes) {
        // Convert itemCost to a double and format it with the peso sign
        try {
            double costValue = Double.parseDouble(itemCost);
            itemCost = String.format("₱%.2f", costValue); // ₱ is the peso sign
        } catch (NumberFormatException e) {
            // Handle the case where itemCost is not a valid number
            // You may want to show an error message or take appropriate action
            e.printStackTrace();
        }

        // Create a new TableRow
        TableRow newRow = createRow(code, itemName, itemType, qty, itemCost, reorder, notes);

        // Add the new TableRow to the TableLayout
        inventoryTable.addView(newRow);

        // Display a Toast message
        Toast.makeText(requireContext(), "Inventory Added ",  Toast.LENGTH_SHORT).show();

        // Perform other actions as needed
    }

    private TableRow createRow(String code, String itemName, String itemType, String qty,
                               String itemCost, String reorder, String notes) {
        // Create a new TableRow
        TableRow newRow = new TableRow(requireContext());

        // Set layout parameters for the new row
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        newRow.setLayoutParams(rowParams);

        // Add TextViews with the entered data to the new TableRow
        TextView codeTextView = createCellTextView(code, 34);
        TextView itemNameTextView = createCellTextView(itemName, 80);
        TextView itemTypeTextView = createCellTextView(itemType, 70);
        TextView qtyTextView = createCellTextView(qty, 40);
        TextView itemCostTextView = createCellTextView(itemCost, 34);
        TextView reorderTextView = createCellTextView(reorder, 65);
        TextView notesTextView = createCellTextView(notes, 45);

        // Add TextViews to the TableRow
        newRow.addView(codeTextView);
        newRow.addView(itemNameTextView);
        newRow.addView(itemTypeTextView);
        newRow.addView(qtyTextView);
        newRow.addView(itemCostTextView);
        newRow.addView(reorderTextView);
        newRow.addView(notesTextView);

        // Long press listener for the TableRow
        newRow.setOnLongClickListener(v -> {
            showDeleteConfirmationDialog(newRow);
            return true;
        });

        return newRow;
    }

    private void showDeleteConfirmationDialog(TableRow rowToDelete) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        builder.setTitle("Delete Inventory")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Yes", (dialogInterface, i) -> handleDeleteButtonClick(rowToDelete))
                .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel());

        builder.create().show();
    }

    private void handleDeleteButtonClick(TableRow rowToDelete) {
        // Remove the TableRow from the TableLayout
        inventoryTable.removeView(rowToDelete);

        // Display a Toast message
        Toast.makeText(requireContext(), "Deleted item", Toast.LENGTH_SHORT).show();

        // Perform other actions as needed
    }

    private TextView createCellTextView(String text, int width) {
        TextView textView = new TextView(requireContext());

        // Set layout parameters to match the existing rows
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                width,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        params.setMargins(2, 2, 2, 2);
        textView.setLayoutParams(params);

        textView.setGravity(Gravity.CENTER);
        textView.setText(text);

        return textView;
    }
}
