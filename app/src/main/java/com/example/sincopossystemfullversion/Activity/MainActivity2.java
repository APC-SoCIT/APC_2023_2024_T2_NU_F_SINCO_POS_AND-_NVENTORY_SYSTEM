package com.example.sincopossystemfullversion.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.example.sincopossystemfullversion.Fragments.HomeFragment;
import com.example.sincopossystemfullversion.Fragments.InventoryFragment;
import com.example.sincopossystemfullversion.Fragments.ProductsFragment;
import com.example.sincopossystemfullversion.Fragments.SalesReportFragment;
import com.example.sincopossystemfullversion.Fragments.SettingsFragment;
import com.example.sincopossystemfullversion.R;

import nl.joery.animatedbottombar.AnimatedBottomBar;

public class MainActivity2 extends AppCompatActivity {


    AnimatedBottomBar BottomBar;
    private int currentTabId;
    private int previousTabId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        BottomBar = findViewById(R.id.bottom_bar);
        getWindow().setStatusBarColor(ContextCompat.getColor(MainActivity2.this, R.color.black));
        // LANDSCAPE ONLY!
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        replace(new HomeFragment());

        // BottomNavBarCode
        BottomBar.setOnTabSelectListener(new AnimatedBottomBar.OnTabSelectListener() {

            @Override
            public void onTabSelected(int i, @Nullable AnimatedBottomBar.Tab tab, int i1, @NonNull AnimatedBottomBar.Tab tab1) {
                previousTabId = currentTabId;
                currentTabId = tab1.getId();


                if (tab1.getId() == R.id.tab_products) {
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

    private void showLogoutConfirmationDialog() {
        // Create a dialog with cancel and logout buttons
        new AlertDialog.Builder(MainActivity2.this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Update user type
                        saveUserType(null);

                        // Navigate to TypeofUser activity
                        startActivity(new Intent(MainActivity2.this, TypeofUser.class));

                        finish(); // Close the current activity if needed
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked "Cancel" button, restore the previous tab
                        BottomBar.selectTabById(previousTabId, true);
                    }
                })
                .show();
    }


    private void saveUserType(String userType) {
        // Use SharedPreferences or any other method to save the user type
        // For example, if you are using SharedPreferences:
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
}