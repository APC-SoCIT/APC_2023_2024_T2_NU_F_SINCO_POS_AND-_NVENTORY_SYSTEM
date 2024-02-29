package com.example.sincopossystemfullversion.Fragments;


import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.sincopossystemfullversion.Adapter.HomeBestSellingNew;
import com.example.sincopossystemfullversion.Adapter.HomeLSNew;
import com.example.sincopossystemfullversion.Adapter.HomeSSGraphNew;
import com.example.sincopossystemfullversion.Adapter.HomeSSNew;
import com.example.sincopossystemfullversion.InventoryModel;
import com.example.sincopossystemfullversion.R;
import com.example.sincopossystemfullversion.SalesReportModel;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormatSymbols;
import java.util.Calendar;

public class HomeFragment extends Fragment {

    HomeLSNew newAdapter;
    HomeSSNew newAdapter1;
    HomeBestSellingNew newAdapter2;
    HomeSSGraphNew newAdapter3;

    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_SELECTED_USER_TYPE = "selected_user_type";
    private String selectedUserType;
    private DatabaseReference usersReference;
    private SharedPreferences sharedPreferences;
    private TextView userTV;

    private ProgressBar SalesprogressBar, BestSellingprogressBar;

    public void setUserType(String userType) {
        this.selectedUserType = userType;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SalesprogressBar = view.findViewById(R.id.SalesprogressBar);
        BestSellingprogressBar = view.findViewById(R.id.BestSellingprogressBar);
        TextView month = view.findViewById(R.id.monthTV);
        month.setText(getCurrentMonthName());

        RecyclerView recyclerView = view.findViewById(R.id.homelowstockRv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        FirebaseRecyclerOptions<InventoryModel> options =
                new FirebaseRecyclerOptions.Builder<InventoryModel>()
                        .setQuery(
                                FirebaseDatabase.getInstance().getReference().child("ingredients")
                                        .orderByChild("ingredient_qty").endAt(4), // Only items with quantity < 5 and not equal to 0
                                InventoryModel.class)
                        .build();

        newAdapter = new HomeLSNew(options, requireContext());
        recyclerView.setAdapter(newAdapter);
        newAdapter.startListening();


        RecyclerView recyclerView1 = view.findViewById(R.id.homesalesRV);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(requireContext());
        recyclerView1.setLayoutManager(linearLayoutManager1);

        FirebaseRecyclerOptions<SalesReportModel> options1 =
                new FirebaseRecyclerOptions.Builder<SalesReportModel>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("sales_report"), SalesReportModel.class)
                        .build();

        newAdapter1 = new HomeSSNew(options1, requireContext());
        recyclerView1.setAdapter(newAdapter1);
        newAdapter1.startListening();

        SalesprogressBar.setVisibility(View.VISIBLE);
        newAdapter1.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                // Hide progress bar after data loaded
                SalesprogressBar.setVisibility(View.GONE);
            }
        });

        RecyclerView recyclerView2 = view.findViewById(R.id.bestSellingRV);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView2.setLayoutManager(linearLayoutManager2);

        FirebaseRecyclerOptions<SalesReportModel> options2 =
                new FirebaseRecyclerOptions.Builder<SalesReportModel>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("sales_report"), SalesReportModel.class)
                        .build();

        newAdapter2 = new HomeBestSellingNew(options2, requireContext());
        recyclerView2.setAdapter(newAdapter2);
        newAdapter2.startListening();

        BestSellingprogressBar.setVisibility(View.VISIBLE);
        newAdapter2.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                // Hide progress bar after data loaded
                BestSellingprogressBar.setVisibility(View.GONE);
            }
        });

        RecyclerView recyclerView4 = view.findViewById(R.id.salesOverviewRV); // Correct RecyclerView
        LinearLayoutManager linearLayoutManager4 = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView4.setLayoutManager(linearLayoutManager4);

        FirebaseRecyclerOptions<SalesReportModel> options3 =
                new FirebaseRecyclerOptions.Builder<SalesReportModel>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("sales_report"), SalesReportModel.class)
                        .build();

        newAdapter3 = new HomeSSGraphNew(options3, requireContext());
        recyclerView4.setAdapter(newAdapter3);
        newAdapter3.startListening();


        userTV = view.findViewById(R.id.usernameTV);

        if (selectedUserType != null) {
            displayAndSaveSelectedUserType(selectedUserType);
        } else {
            Log.e("HomeFragment", "Selected user type is null");
            userTV.setText("Unknown User");
        }

        Log.d("HomeFragment", "Selected User Type: " + selectedUserType);

        return view;
    }

    private String getCurrentMonthName() {
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH); // Calendar.MONTH is zero-based
        String[] monthNames = new DateFormatSymbols().getMonths();
        return monthNames[currentMonth];
    }

    @Override
    public void onResume() {
        super.onResume();
        String savedSelectedUser = sharedPreferences.getString(KEY_SELECTED_USER_TYPE, null);

        if (savedSelectedUser != null) {
            displaySelectedUserType(savedSelectedUser);
        } else {
            Log.e("HomeFragment", "Selected user type is null");
            userTV.setText("Unknown User");
        }
    }

    private void displayAndSaveSelectedUserType(String userType) {
        this.selectedUserType = userType; // Set selectedUserType
        saveSelectedUserTypeToPrefs(userType); // Save selectedUserType to SharedPreferences
    }


    private void displaySelectedUserType(String selectedUserType) {
        String greeting;
        int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        // Determine the time of day and set the greeting message accordingly
        if (hourOfDay >= 0 && hourOfDay < 12) {
            greeting = "Good morning";
        } else if (hourOfDay >= 12 && hourOfDay < 18) {
            greeting = "Good afternoon";
        } else {
            greeting = "Good evening";
        }

        // Capitalize the first letter of selectedUserType
        String capitalizedUserType = selectedUserType.substring(0, 1).toUpperCase() + selectedUserType.substring(1);

        // Display the greeting message along with the username
        String fullGreeting = greeting + ", " + capitalizedUserType + "!";
        userTV.setText(fullGreeting);
    }

    private void saveSelectedUserTypeToPrefs(String userType) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SELECTED_USER_TYPE, userType);
        editor.apply();
    }

}