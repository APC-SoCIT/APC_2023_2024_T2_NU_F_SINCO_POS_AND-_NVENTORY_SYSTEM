package com.example.sincopossystemfullversion.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincopossystemfullversion.Adapter.ArchivedItemsAdapter;
import com.example.sincopossystemfullversion.Adapter.InventoryNew;
import com.example.sincopossystemfullversion.InventoryModel;
import com.example.sincopossystemfullversion.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class InventoryFragment extends Fragment {

    InventoryNew newAdapter;
    ArchivedItemsAdapter archivedAdapter;
    private View root;
    private String selectedUserType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_inventory, container, false);

        updateInventoryFromSalesReport();

        selectedUserType = requireActivity().getIntent().getStringExtra("USER_TYPE");

        ImageView addInventoryIcon = root.findViewById(R.id.addinventory);
        addInventoryIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedUserType != null && selectedUserType.equals("admin")) {
                    insertData();
                } else {
                    showAdminPinDialog();
                }
                }
        });

        Button archivedButton = root.findViewById(R.id.archivedButton);
        archivedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_archived_inventory, null);
                
                builder.setView(dialogView);

                FirebaseRecyclerOptions<InventoryModel> options =
                        new FirebaseRecyclerOptions.Builder<InventoryModel>()
                                .setQuery(FirebaseDatabase.getInstance().getReference().child("ingredients").orderByChild("archived").equalTo(true), InventoryModel.class)
                                .build();

                RecyclerView recyclerView1 = dialogView.findViewById(R.id.ingredientItemsRV);
                LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(requireContext());
                recyclerView1.setLayoutManager(linearLayoutManager1);

                archivedAdapter = new ArchivedItemsAdapter(options, selectedUserType, requireContext());
                recyclerView1.setAdapter(archivedAdapter);
                archivedAdapter.startListening();

                builder.create().show();
            }
        });

        TextView totalprodCount = root.findViewById(R.id.totalprodCount);
        TextView lowstockCount = root.findViewById(R.id.lowstockCount);
        TextView outofstockCount = root.findViewById(R.id.outofstockCount);

        DatabaseReference inventoryRef = FirebaseDatabase.getInstance().getReference().child("ingredients");

        FirebaseRecyclerOptions<InventoryModel> options =
                new FirebaseRecyclerOptions.Builder<InventoryModel>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("ingredients").orderByChild("archived").equalTo(false), InventoryModel.class)
                        .build();
        inventoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get the count of items in the database
                long itemCount = dataSnapshot.getChildrenCount();
                totalprodCount.setText(String.valueOf(itemCount));

                // Initialize counts for low stock and out of stock
                int lowStockItemCount = 0;
                int outOfStockItemCount = 0;

                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    // Assuming your quantity is stored as a Long in the "ingredient_qty" field
                    Long quantity = productSnapshot.child("ingredient_qty").getValue(Long.class);

                    if (quantity != null && quantity > 0) {
                        // If quantity is greater than 0, it's not out of stock
                        // Check if it's low stock based on your criteria
                        if (quantity < 5) {
                            lowStockItemCount++;
                        }
                    } else {
                        // If quantity is null or 0, it's out of stock
                        outOfStockItemCount++;
                    }
                }

                // Update the TextViews with the counts
                lowstockCount.setText(String.valueOf(lowStockItemCount));
                outofstockCount.setText(String.valueOf(outOfStockItemCount));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle potential errors
            }
        });


        RecyclerView recyclerView = root.findViewById(R.id.inventoryRV);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        newAdapter = new InventoryNew(options, selectedUserType, requireContext());
        recyclerView.setAdapter(newAdapter);
        newAdapter.startListening();

        SearchView searchView = root.findViewById(R.id.searchView);
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

        return root;
    }

    private void txtSearch(String str) {
        String capitalizedStr = capitalizeSearch(str);

        FirebaseRecyclerOptions<InventoryModel> options =
                new FirebaseRecyclerOptions.Builder<InventoryModel>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("ingredients")
                                        .orderByChild("ingredient_name")
                                        .startAt(capitalizedStr)
                                        .endAt(capitalizedStr + "~"),
                                InventoryModel.class)
                        .build();

        newAdapter = new InventoryNew(options, selectedUserType, requireContext());
        newAdapter.startListening();
        RecyclerView recyclerView = root.findViewById(R.id.inventoryRV);
        recyclerView.setAdapter(newAdapter);
    }

    private String capitalizeSearch(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }


    private void insertData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_inventory, null);

        EditText itemNameEditText = dialogView.findViewById(R.id.edtItemName);
        Spinner itemTypeEditText = dialogView.findViewById(R.id.spinnerItemType);
        EditText qtyEditText = dialogView.findViewById(R.id.edtQuantity);
        EditText itemCostEditText = dialogView.findViewById(R.id.edtCost);
        EditText reorderEditText = dialogView.findViewById(R.id.editTextReorder);
        EditText notesEditText = dialogView.findViewById(R.id.edtNotes);

        builder.setView(dialogView)
                .setPositiveButton("Add", (dialogInterface, i) -> {
                    // Get values from the dialog
                    String itemName = capitalizeFirstLetter(itemNameEditText.getText().toString());
                    String itemType = capitalizeFirstLetter(itemTypeEditText.getSelectedItem().toString());
                    String quantity = qtyEditText.getText().toString();
                    String itemCost = itemCostEditText.getText().toString();
                    String reorder = reorderEditText.getText().toString();
                    String notes = notesEditText.getText().toString();
                    boolean archived = false;

                    // Check if any field is empty
                    if (itemName.isEmpty() || itemType.isEmpty()
                            || quantity.isEmpty() || itemCost.isEmpty() || reorder.isEmpty() || notes.isEmpty()) {
                        Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                    } else {
                        // Proceed only if all fields are filled
                        Long quantityLong = Long.parseLong(quantity);
                        Long costLong = Long.parseLong(itemCost);

                        // Check if the item name already exists in the database
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ingredients");
                        databaseReference.orderByChild("ingredient_name").equalTo(itemName).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // Item name already exists, show error message
                                    Toast.makeText(requireContext(), "Item with the same name already exists", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Item name does not exist, proceed with insertion
                                    // Generate a unique 5-character code
                                    String code = generateUniqueCode();

                                    // Insert data into Firebase Realtime Database
                                    insertProductData(code, itemName, itemType, quantityLong, costLong, reorder, notes, archived);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d("Firebase","onCancelled", databaseError.toException());
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

        builder.create().show();
    }


    // Method to capitalize the first letter of each word
    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        // Split the input string by space
        String[] words = input.split("\\s+");
        StringBuilder capitalizedString = new StringBuilder();
        // Capitalize the first letter of each word and append it to the StringBuilder
        for (String word : words) {
            capitalizedString.append(word.substring(0, 1).toUpperCase()).append(word.substring(1)).append(" ");
        }
        // Trim any extra space at the end and return the capitalized string
        return capitalizedString.toString().trim();
    }

    private void showAdminPinDialog() {
        AlertDialog.Builder adminPinBuilder = new AlertDialog.Builder(requireContext());
        View adminPinView = getLayoutInflater().inflate(R.layout.admin_permission_dialog, null);
        Button submitButton = adminPinView.findViewById(R.id.submitButton);

        adminPinBuilder.setView(adminPinView);

        AlertDialog adminPinDialog = adminPinBuilder.create();
        adminPinDialog.show();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adminPinDialog.dismiss();
            }
        });
    }

    // Method to generate a unique 5-character code
    private String generateUniqueCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"; // Possible characters for the code
        StringBuilder codeBuilder = new StringBuilder();
        Random random = new Random();

        // Generate 5 random characters
        for (int i = 0; i < 5; i++) {
            int index = random.nextInt(characters.length());
            char randomChar = characters.charAt(index);
            codeBuilder.append(randomChar);
        }

        return codeBuilder.toString();
    }

    private void insertProductData(String codeLong, String itemName, String itemType,
                                   Long quantityLong, Long costLong, String reorder, String notes, boolean archived) {
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child("ingredients").push();

        // Create a map to store product data
        Map<String, Object> productMap = new HashMap<>();
        productMap.put("code", codeLong);
        productMap.put("ingredient_name", itemName);
        productMap.put("ingredient_type", itemType);
        productMap.put("ingredient_qty", quantityLong);
        productMap.put("ingredient_cost", costLong);
        productMap.put("re_order", reorder);
        productMap.put("notes", notes);
        productMap.put("archived", false);

        // Insert data into the database
        productsRef.setValue(productMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "New Product Added Successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error: Cannot add new product. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }


    public void updateInventoryFromSalesReport() {
        DatabaseReference salesReference = FirebaseDatabase.getInstance().getReference().child("sales_report");

        salesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String productName = snapshot.child("sr_product").getValue(String.class);
                    int soldQuantity = snapshot.child("sr_qty").getValue(Integer.class);

                    Log.d("Sales Report", "Product Name: " + productName + ", Sold Quantity: " + soldQuantity);

                    // Find the product type from the products table
                    DatabaseReference productsReference = FirebaseDatabase.getInstance().getReference().child("products");
                    productsReference.orderByChild("product_name").equalTo(productName)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot productSnapshot) {
                                    if (productSnapshot.exists()) {
                                        for (DataSnapshot product : productSnapshot.getChildren()) {
                                            String productType = product.child("product_type").getValue(String.class);
                                            if (productType != null && !TextUtils.isEmpty(productType)) {
                                                Log.d("Product Type", "Product Type for " + productName + ": " + productType);
                                                // Call method to update inventory after sale
                                                updateInventoryAfterSale(productType, soldQuantity);
                                            } else {
                                                // Handle case where product type is not found
                                                Log.e("Inventory Update", "Product type not found for product: " + productName);
                                            }
                                        }
                                    } else {
                                        // Handle case where product is not found in the products table
                                        Log.e("Inventory Update", "Product not found in products table: " + productName);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    // Handle database error
                                    Log.e("Inventory Update", "Database error: " + error.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Log.e("Inventory Update", "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void updateInventoryAfterSale(String itemType, int soldQuantity) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("ingredients");

        // Query for ingredients with the specified type
        Query query = databaseReference.orderByChild("ingredient_type").equalTo(itemType);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    InventoryModel inventoryItem = snapshot.getValue(InventoryModel.class);
                    if (inventoryItem != null) {
                        // Check if ingredient_type matches the itemType obtained from products node
                        if (inventoryItem.getIngredient_type().equals(itemType)) {
                            long currentQty = inventoryItem.getIngredient_qty();
                            if (currentQty == 10) {
                                // If the current quantity is 10, deduct 1 from the quantity
                                long newQty = currentQty - 1;
                                // Ensure quantity doesn't go negative
                                if (newQty < 0) {
                                    newQty = 0;
                                }
                                // Update the quantity in the database
                                updateQuantity(snapshot.getRef(), newQty);
                            } else {
                                // If the current quantity is not 10, simply deduct the sold quantity
                                long newQty = currentQty - soldQuantity;
                                // Ensure quantity doesn't go negative
                                if (newQty < 0) {
                                    newQty = 0;
                                }
                                // Update the quantity in the database
                                updateQuantity(snapshot.getRef(), newQty);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Log.e("Inventory Update", "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void updateQuantity(DatabaseReference ref, long newQty) {
        ref.child("ingredient_qty").setValue(newQty)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("Inventory Update", "Inventory updated successfully for item type: " + ref.getKey());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Inventory Update", "Failed to update inventory for item type: " + ref.getKey(), e);
                    }
                });
    }


}
