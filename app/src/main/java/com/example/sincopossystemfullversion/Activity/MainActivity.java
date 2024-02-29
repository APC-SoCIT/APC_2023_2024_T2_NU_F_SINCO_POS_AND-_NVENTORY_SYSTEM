package com.example.sincopossystemfullversion.Activity;

import static com.example.sincopossystemfullversion.InventoryModel.inventoryItemList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.sincopossystemfullversion.Adapter.InventoryNew;
import com.example.sincopossystemfullversion.Fragments.HomeFragment;
import com.example.sincopossystemfullversion.Fragments.InventoryFragment;
import com.example.sincopossystemfullversion.Fragments.SalesReportFragment;
import com.example.sincopossystemfullversion.Fragments.ProductsFragment;
import com.example.sincopossystemfullversion.Fragments.SettingsFragment;
import com.example.sincopossystemfullversion.InventoryModel;
import com.example.sincopossystemfullversion.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import nl.joery.animatedbottombar.AnimatedBottomBar;

public class MainActivity extends AppCompatActivity {

    AnimatedBottomBar BottomBar;
    private int currentTabId;
    private int previousTabId;

    private void notifyLowInventory(InventoryModel inventoryItem) {
        // Check if the inventory item quantity is low
        if (inventoryItem.getIngredient_qty() < 5 && inventoryItem.getIngredient_qty() !=0 && !inventoryItem.isNotified()) {
            // Send notification
            inventoryItem.sendNotification(this);
        }
    }

    // Call this method to check inventory levels for all items and send notifications if necessary
    private void checkInventoryLevels() {
        // Loop through all inventory items and notify if inventory is low
        for (InventoryModel item : inventoryItemList) {
            notifyLowInventory(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomBar = findViewById(R.id.bottom_bar);
        getWindow().setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.black));
        // LANDSCAPE ONLY!
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Handle fragment transactions
        handleFragmentTransactions();

        populateInventoryItems();

        // Send notifications for low inventory items
        sendNotificationsForLowInventory();

        String selectedUserType = getIntent().getStringExtra("USER_TYPE");
        if (selectedUserType != null) {
            HomeFragment homeFragment = new HomeFragment();
            homeFragment.setUserType(selectedUserType); // Pass the selected user type to HomeFragment
            replaceFragment(homeFragment); // Replace the current fragment with HomeFragment
        } else {
            Toast.makeText(MainActivity.this, "Invalid user type", Toast.LENGTH_SHORT).show();
        }


        // BottomNavBarCode
        BottomBar.setOnTabSelectListener(new AnimatedBottomBar.OnTabSelectListener() {

            @Override
            public void onTabSelected(int i, @Nullable AnimatedBottomBar.Tab tab, int i1, @NonNull AnimatedBottomBar.Tab tab1) {
                previousTabId = currentTabId;
                currentTabId = tab1.getId();

                if (tab1.getId() == R.id.tab_home) {
                    replace(new HomeFragment());
                } else if (tab1.getId() == R.id.tab_products) {
                    replace(new ProductsFragment());
                } else if (tab1.getId() == R.id.tab_salesreport) {
                    replace(new SalesReportFragment());
                } else if (tab1.getId() == R.id.tab_inventory) {
                    replace(new InventoryFragment());
                } else if (tab1.getId() == R.id.tab_settings) {
                    replace(new SettingsFragment());
                } else if (tab1.getId() == R.id.tab_logout) {
                    showLogoutConfirmationDialog();
                }
            }


            @Override
            public void onTabReselected(int i, @NonNull AnimatedBottomBar.Tab tab) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkInventoryLevels(); // Check inventory levels and send notifications if necessary
    }

    private void populateInventoryItems() {
        // Get a reference to your Firebase database
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("ingredients");

        // Clear existing items from the list
        inventoryItemList.clear();

        // Attach a listener to retrieve the data
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Loop through the data snapshot to retrieve ingredient names
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Retrieve ingredient name and quantity from the snapshot
                    String ingredientName = snapshot.child("ingredient_name").getValue(String.class);
                    Long ingredientQty = snapshot.child("ingredient_qty").getValue(Long.class);

                    // Check if ingredientName and ingredientQty are not null
                    if (ingredientName != null && ingredientQty != null) {
                        // Check if the ingredient quantity is less than 5
                        if (ingredientQty < 5) {
                            // Add the ingredient to the inventoryItemList
                            InventoryModel.autoAddInventoryItem("1", ingredientName, "Type", "Notes", 100L, "CODE", ingredientQty);
                        }
                    }
                }

                // Send notifications for low inventory items
                sendNotificationsForLowInventory();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private void sendNotificationsForLowInventory() {
        // Iterate through each inventory item and send notifications for low inventory
        for (InventoryModel item : InventoryModel.getInventoryItemList()) {
            // Check if the ingredient quantity is less than 5 and not equal to 0
            if (item.getIngredient_qty() < 5 && item.getIngredient_qty() != 0) {
                item.sendNotification(this);
            }
            if (item.getIngredient_qty() <= 0) {
                item.sendOutOfStock(this);
            }
        }
    }

    private void showLogoutConfirmationDialog() {
        // Create a dialog with cancel and logout buttons
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Update user type
                        saveUserType(null);

                        // Navigate to TypeofUser activity
                        startActivity(new Intent(MainActivity.this, TypeofUser.class));

                        finish(); // Close the current activity if needed
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked "Cancel" button, restore the previous tab
                        if (previousTabId != 0) {
                            BottomBar.selectTabById(previousTabId, true);
                        } else {
                            // If previousTabId is 0, fallback to selecting the Home tab
                            BottomBar.selectTabById(R.id.tab_home, true);
                        }
                    }
                })
                .setCancelable(false) // Dialog cannot be dismissed by clicking outside
                .create();

        dialog.show();
    }


    private void saveUserType(String userType) {
        SharedPreferences preferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userType", userType);
        editor.apply();
    }


    private void replace(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.framelayout, fragment);
        transaction.commit();
    }

    private void handleFragmentTransactions() {
        if (getIntent() != null && getIntent().hasExtra("openFragment")) {
            String fragmentToOpen = getIntent().getStringExtra("openFragment");
            if (fragmentToOpen.equals("products")) {
                // Replace or add the products fragment here
                replaceFragment(new ProductsFragment());

                // Select the "Products" tab in the bottom navigation bar
                BottomBar.selectTabById(R.id.tab_products, true);
            }
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.framelayout, fragment);
        transaction.commit();
    }
}