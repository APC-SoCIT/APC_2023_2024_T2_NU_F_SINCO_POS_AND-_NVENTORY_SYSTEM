package com.example.sincopossystemfullversion.Adapter;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincopossystemfullversion.InventoryModel;
import com.example.sincopossystemfullversion.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InventoryNew extends FirebaseRecyclerAdapter <InventoryModel, InventoryNew.myViewHolder>{

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     * @param context
     */

    DecimalFormat currencyFormatter = new DecimalFormat("₱ #,###.00");
    private List<InventoryModel> mDataList;
    private Context mContext;
    private String selectedUserType; private DatabaseReference usersReference;

    private static final String PREF_NAME = "notification_prefs";
    private SharedPreferences sharedPreferences;
    private Set<String> notifiedItems = new HashSet<>();

    public InventoryNew(@NonNull FirebaseRecyclerOptions<InventoryModel> options, String selectedUserType, Context context) {
        super(options);
        mContext = context;
        mDataList = new ArrayList<>();
        this.selectedUserType = selectedUserType;
        this.usersReference = FirebaseDatabase.getInstance().getReference("user");
    }


    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull InventoryModel model) {

        Log.d("Firebase", "Selected User: " + selectedUserType);

        if (model.isArchived()) {
            // If archived, show the item in the RecyclerView
            holder.itemView.setVisibility(View.GONE);
        } else {
            // If not archived, hide the item in the RecyclerView
            holder.itemView.setVisibility(View.VISIBLE);
        }

        holder.ingcode.setText((String.valueOf(model.getCode())));
        holder.ingcost.setText(currencyFormatter.format(model.getIngredient_cost()));
        holder.ingreorder.setText(model.getRe_order());
        holder.ingname.setText(model.getIngredient_name());
        holder.ingtype.setText(model.getIngredient_type());
        holder.ingqty.setText(String.valueOf(model.getIngredient_qty()));
        holder.ingnotes.setText(model.getNotes());

        if (model.getIngredient_qty() < 5) {
            holder.ingreorder.setText("Yes");
        } else {
            holder.ingreorder.setText("No");
        }

        holder.editIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedUserType != null && selectedUserType.equals("admin")) {
                    final DialogPlus dialogPlus = DialogPlus.newDialog(holder.ingnotes.getContext())
                            .setContentHolder(new ViewHolder(R.layout.inventory_edit_popout))
                            .setExpanded(true,800)
                            .create();

                    View view = dialogPlus.getHolderView();

                    //Edit texts
                    EditText ingredient_name = view.findViewById(R.id.edtItemName);
                    Spinner ingtype = view.findViewById(R.id.spinnerItemType);
                    EditText ingredient_qty = view.findViewById(R.id.edtQuantity);
                    EditText ingredient_cost = view.findViewById(R.id.edtCost);
                    EditText re_order = view.findViewById(R.id.editTextReorder);
                    EditText notes = view.findViewById(R.id.edtNotes);

                    Button btnUpdate = view.findViewById(R.id.btnUpdate);

                    ingredient_name.setText(model.getIngredient_name());
                    ingredient_qty.setText(String.valueOf(model.getIngredient_qty()));
                    ingredient_cost.setText(String.valueOf(model.getIngredient_cost()));
                    re_order.setText(model.getRe_order());
                    notes.setText(model.getNotes());
                    String selectedProductType = model.getIngredient_type();

                    String[] productTypes = view.getContext().getResources().getStringArray(R.array.item_types);
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, productTypes);
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    ingtype.setAdapter(spinnerAdapter);

                    // Set the selected product type in the Spinner
                    int selectedTypeIndex = Arrays.asList(productTypes).indexOf(selectedProductType);
                    if (selectedTypeIndex >= 0) {
                        ingtype.setSelection(selectedTypeIndex);
                    }

                    dialogPlus.show();

                    btnUpdate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Get values from the dialog
                            String itemName = capitalizeFirstLetter(ingredient_name.getText().toString().trim());
                            String updatedItemType = ingtype.getSelectedItem().toString().trim();
                            String quantity = ingredient_qty.getText().toString().trim();
                            String itemCost = ingredient_cost.getText().toString().trim();
                            String reorderValue = capitalizeFirstLetter(re_order.getText().toString().trim());
                            String updatedNotes = capitalizeFirstLetter(notes.getText().toString().trim());

                            // Check if any field is empty
                            boolean anyFieldEmpty = TextUtils.isEmpty(itemName)
                                    || TextUtils.isEmpty(updatedItemType)
                                    || TextUtils.isEmpty(quantity)
                                    || TextUtils.isEmpty(itemCost)
                                    || TextUtils.isEmpty(reorderValue)
                                    || TextUtils.isEmpty(updatedNotes);

                            if (!anyFieldEmpty) {
                                InventoryModel originalProduct = getItem(position);

                                // Check if any field value has changed
                                if (originalProduct.getIngredient_name().equals(itemName)
                                        && originalProduct.getIngredient_type().equals(updatedItemType)
                                        && originalProduct.getIngredient_qty().equals(Long.parseLong(quantity))
                                        && originalProduct.getIngredient_cost().equals(Long.parseLong(itemCost))
                                        && originalProduct.getRe_order().equals(reorderValue)
                                        && originalProduct.getNotes().equals(updatedNotes)) {
                                    // No changes made
                                    Toast.makeText(view.getContext(), "No changes made to the product.", Toast.LENGTH_SHORT).show();
                                    dialogPlus.dismiss();
                                } else {
                                    // Changes made, proceed with updating the data in Firebase Realtime Database
                                    Long quantityLong = Long.parseLong(quantity);
                                    Long costLong = Long.parseLong(itemCost);

                                    // Check if the updated item name already exists in the database
                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ingredients");
                                    databaseReference.orderByChild("ingredient_name").equalTo(itemName).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                // Item name already exists, show error message
                                                Toast.makeText(view.getContext(), "Item with the same name already exists", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // Item name does not exist, proceed with updating the data
                                                // Create a map to store updated product data
                                                Map<String, Object> map = new HashMap<>();
                                                map.put("ingredient_name", itemName);
                                                map.put("ingredient_type", updatedItemType);
                                                map.put("ingredient_qty", quantityLong);
                                                map.put("ingredient_cost", costLong);
                                                map.put("re_order", reorderValue);
                                                map.put("notes", updatedNotes);
                                                map.put("archived", false);

                                                // Update the data in Firebase Realtime Database
                                                FirebaseDatabase.getInstance().getReference().child("ingredients")
                                                        .child(getRef(position).getKey()).updateChildren(map)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                Toast.makeText(holder.ingcode.getContext(), "Product Updated Successfully.", Toast.LENGTH_SHORT).show();
                                                                dialogPlus.dismiss();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(Exception e) {
                                                                Toast.makeText(holder.ingcode.getContext(), "Product Update Failed. Please try again.", Toast.LENGTH_SHORT).show();
                                                                dialogPlus.dismiss();
                                                            }
                                                        });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Log.d("Firebase", "onCancelled", databaseError.toException());
                                        }
                                    });
                                }
                            } else {
                                Toast.makeText(view.getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {showAdminPinDialog();
                }
            }
        });
        holder.archiveIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedUserType != null && selectedUserType.equals("admin")) {
                    String productKey = getRef(position).getKey();

                    // Update the archived status of the product in the Firebase database
                    FirebaseDatabase.getInstance().getReference().child("ingredients").child(productKey)
                            .child("archived").setValue(true)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                    Toast.makeText(holder.ingname.getContext(), "Ingredient Archived Successfully.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(holder.ingname.getContext(), "Failed to Archive Product. Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    showAdminPinDialog();
                }
            }
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
                            long newQty = currentQty - soldQuantity;
                            if (newQty < 0) {
                                newQty = 0; // Ensure quantity doesn't go negative
                            }
                            // Update the quantity in the database
                            snapshot.getRef().child("ingredient_qty").setValue(newQty)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("Inventory Update", "Inventory updated successfully for item type: " + itemType);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("Inventory Update", "Failed to update inventory for item type: " + itemType, e);
                                        }
                                    });
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


    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.inventorytable,parent, false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder {
        TextView ingcode, ingcost, ingreorder, ingname, ingqty, ingtype, ingnotes;
        ImageView editIcon, archiveIcon;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            ingcode = itemView.findViewById(R.id.textViewCode);
            ingcost = itemView.findViewById(R.id.textViewItemCost);
            ingreorder = itemView.findViewById(R.id.textViewReorder);
            ingname = itemView.findViewById(R.id.textViewItemName);
            ingqty = itemView.findViewById(R.id.textViewQty);
            ingtype = itemView.findViewById(R.id.textViewItemType);
            ingnotes = itemView.findViewById(R.id.textViewNotes);

            editIcon = itemView.findViewById(R.id.editIcon);
            archiveIcon = itemView.findViewById(R.id.archiveIcon);

        }
    }


    private void showAdminPinDialog() {
        AlertDialog.Builder adminPinBuilder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View adminPinView = inflater.inflate(R.layout.admin_permission_dialog, null);
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

    // Internal method to show the edit dialog
    private void showEditDialogInternal(InventoryModel model, int position, myViewHolder holder) {
        final DialogPlus dialogPlus = DialogPlus.newDialog(holder.ingnotes.getContext())
                .setContentHolder(new ViewHolder(R.layout.inventory_edit_popout))
                .setExpanded(true,800)
                .create();

        View view = dialogPlus.getHolderView();

        //Edit texts
        EditText ingredient_name = view.findViewById(R.id.edtItemName);
        Spinner ingtype = view.findViewById(R.id.spinnerItemType);
        EditText ingredient_qty = view.findViewById(R.id.edtQuantity);
        EditText ingredient_cost = view.findViewById(R.id.edtCost);
        EditText re_order = view.findViewById(R.id.editTextReorder);
        EditText notes = view.findViewById(R.id.edtNotes);

        Button btnUpdate = view.findViewById(R.id.btnUpdate);

        ingredient_name.setText(model.getIngredient_name());
        ingredient_qty.setText(String.valueOf(model.getIngredient_qty()));
        ingredient_cost.setText(String.valueOf(model.getIngredient_cost()));
        re_order.setText(model.getRe_order());
        notes.setText(model.getNotes());
        String selectedProductType = model.getIngredient_type();

        String[] productTypes = view.getContext().getResources().getStringArray(R.array.item_types);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, productTypes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ingtype.setAdapter(spinnerAdapter);

        // Set the selected product type in the Spinner
        int selectedTypeIndex = Arrays.asList(productTypes).indexOf(selectedProductType);
        if (selectedTypeIndex >= 0) {
            ingtype.setSelection(selectedTypeIndex);
        }

        dialogPlus.show();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get values from the dialog
                String itemName = capitalizeFirstLetter(ingredient_name.getText().toString().trim());
                String updatedItemType = ingtype.getSelectedItem().toString().trim();
                String quantity = ingredient_qty.getText().toString().trim();
                String itemCost = ingredient_cost.getText().toString().trim();
                String reorderValue = capitalizeFirstLetter(re_order.getText().toString().trim());
                String updatedNotes = capitalizeFirstLetter(notes.getText().toString().trim());

                // Check if any field is empty
                boolean anyFieldEmpty = TextUtils.isEmpty(itemName)
                        || TextUtils.isEmpty(updatedItemType)
                        || TextUtils.isEmpty(quantity)
                        || TextUtils.isEmpty(itemCost)
                        || TextUtils.isEmpty(reorderValue)
                        || TextUtils.isEmpty(updatedNotes);

                if (!anyFieldEmpty) {
                    // Proceed only if all fields are filled
                    // Get the original product data
                    InventoryModel originalProduct = getItem(position);

                    // Check if any field value has changed
                    if (originalProduct.getIngredient_name().equals(itemName)
                            && originalProduct.getIngredient_type().equals(updatedItemType)
                            && originalProduct.getIngredient_qty().equals(Long.parseLong(quantity))
                            && originalProduct.getIngredient_cost().equals(Long.parseLong(itemCost))
                            && originalProduct.getRe_order().equals(reorderValue)
                            && originalProduct.getNotes().equals(updatedNotes)) {
                        // No changes made
                        Toast.makeText(view.getContext(), "No changes made to the product.", Toast.LENGTH_SHORT).show();
                        dialogPlus.dismiss();
                    } else {
                        // Changes made, proceed with updating the data in Firebase Realtime Database
                        Long quantityLong = Long.parseLong(quantity);
                        Long costLong = Long.parseLong(itemCost);

                        // Check if the updated item name already exists in the database
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ingredients");
                        databaseReference.orderByChild("ingredient_name").equalTo(itemName).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("ingredient_name", itemName);
                                    map.put("ingredient_type", updatedItemType);
                                    map.put("ingredient_qty", quantityLong);
                                    map.put("ingredient_cost", costLong);
                                    map.put("re_order", reorderValue);
                                    map.put("notes", updatedNotes);
                                    map.put("archived", false);

                                    // Update the data in Firebase Realtime Database
                                    FirebaseDatabase.getInstance().getReference().child("ingredients")
                                            .child(getRef(position).getKey()).updateChildren(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Toast.makeText(holder.ingcode.getContext(), "Product Updated Successfully.", Toast.LENGTH_SHORT).show();
                                                    dialogPlus.dismiss();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(Exception e) {
                                                    Toast.makeText(holder.ingcode.getContext(), "Product Update Failed. Please try again.", Toast.LENGTH_SHORT).show();
                                                    dialogPlus.dismiss();
                                                }
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d("Firebase", "onCancelled", databaseError.toException());
                            }
                        });
                    }
                } else {
                    Toast.makeText(view.getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



}