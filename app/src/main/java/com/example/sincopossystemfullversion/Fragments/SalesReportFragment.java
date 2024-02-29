package com.example.sincopossystemfullversion.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.sincopossystemfullversion.Adapter.SalesReportNew;
import com.example.sincopossystemfullversion.R;
import com.example.sincopossystemfullversion.SalesReportModel;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.apache.xmlbeans.impl.xb.xsdschema.All;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SalesReportFragment extends Fragment {
    SalesReportNew newAdapter;
    DecimalFormat currencyFormatter = new DecimalFormat("₱ #,###.00");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    SwipeRefreshLayout swipeRefreshLayout;
    private List<CardView> cardViews;
    private ProgressBar progressBar;

    // Define TextViews as class variables
    TextView AllsalesTextView,DailyTextView, WeeklyTextView, MonthlyTextView, YearlyTextView, totalSalesTV;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sales_report, container, false);

        totalSalesTV = view.findViewById(R.id.totalSalesTextView);

        // Initialize the list of CardViews
        cardViews = new ArrayList<>();
        CardView Allsales = view.findViewById(R.id.AllsalesCardView);
        CardView dailyCardView = view.findViewById(R.id.DailyCardView);
        CardView weeklyCardView = view.findViewById(R.id.WeeklyCardView);
        CardView monthlyCardView = view.findViewById(R.id.MonthlyCardView);
        CardView yearlyCardView = view.findViewById(R.id.YearlyCardView);

        cardViews.add(Allsales);
        cardViews.add(dailyCardView);
        cardViews.add(weeklyCardView);
        cardViews.add(monthlyCardView);
        cardViews.add(yearlyCardView);

        // Find the SwipeRefreshLayout by its ID
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        RecyclerView recyclerView = view.findViewById(R.id.salesreportRV);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        // Assign TextViews to class variables
        AllsalesTextView =view.findViewById(R.id.AllsalesTextView);
        DailyTextView = view.findViewById(R.id.DailyTextView);
        WeeklyTextView = view.findViewById(R.id.WeeklyTextView);
        MonthlyTextView = view.findViewById(R.id.MonthlyTextView);
        YearlyTextView = view.findViewById(R.id.YearlyTextView);

        // Find the ProgressBar by its ID
        progressBar = view.findViewById(R.id.progressBar);

        // Get the current date for comparison
        Date currentDate = Calendar.getInstance().getTime();

        // Show ProgressBar when loading starts
        progressBar.setVisibility(View.VISIBLE);
        Query baseQuery = FirebaseDatabase.getInstance().getReference().child("sales_report")
                .orderByChild("sr_date")
                .startAt(currentDate.getTime()); // Filter to get reports on or after the current date

        FirebaseRecyclerOptions<SalesReportModel> options =
                new FirebaseRecyclerOptions.Builder<SalesReportModel>()
                        .setQuery(baseQuery, SalesReportModel.class)
                        .build();

        newAdapter = new SalesReportNew(options, requireContext());
        recyclerView.setAdapter(newAdapter);
        newAdapter.startListening();

        // Hide ProgressBar when loading finishes (move this inside the listener)
        newAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                // Hide ProgressBar when loading finishes
                progressBar.setVisibility(View.GONE);calculateAndDisplayTotalSales("");
            }
        });



        // Set OnClickListener for each card view
        for (int i = 0; i < cardViews.size(); i++) {
            final int index = i;
            cardViews.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Reset the background color and text color of all card views
                    for (CardView cardView : cardViews) {
                        cardView.setCardBackgroundColor(getResources().getColor(R.color.lightcolor)); // Change to your normal color
                    }
                    // Reset the text color of all navigation labels
                    resetNavigationTextColors(AllsalesTextView,DailyTextView, WeeklyTextView, MonthlyTextView, YearlyTextView);


                    // Highlight the clicked card view
                    cardViews.get(index).setCardBackgroundColor(getResources().getColor(R.color.selectednav)); // Change to your selected color

                    // Highlight the corresponding navigation label
                    highlightNavigationLabel(index,AllsalesTextView, DailyTextView, WeeklyTextView, MonthlyTextView, YearlyTextView);

                    // Filter the RecyclerView based on the selected option
                    String filterOption = "";
                    switch (index) {
                        case 0: // AllsalesCardView
                            filterOption = ""; // Empty string means no filtering, include all sales
                            break;
                        case 1: // DailyCardView
                            filterOption = "daily";
                            break;
                        case 2: // WeeklyCardView
                            filterOption = "weekly";
                            break;
                        case 3: // MonthlyCardView
                            filterOption = "monthly";
                            break;
                        case 4: // YearlyCardView
                            filterOption = "yearly";
                            break;
                    }
                    // Pass the filter option to the adapter to apply the filter
                    newAdapter.setFilterOption(filterOption);
                    calculateAndDisplayTotalSales(filterOption);
                }
            });
        }

        // Reset the background color and text color of all card views
        for (CardView cardView : cardViews) {
            cardView.setCardBackgroundColor(getResources().getColor(R.color.lightcolor)); // Change to your normal color
        }
        // Reset the text color of all navigation labels
        resetNavigationTextColors(AllsalesTextView,DailyTextView, WeeklyTextView, MonthlyTextView, YearlyTextView);

        // Highlight the "All" card view
        cardViews.get(0).setCardBackgroundColor(getResources().getColor(R.color.selectednav)); // Change to your selected color
        // Highlight the corresponding navigation label
        highlightNavigationLabel(0, AllsalesTextView,DailyTextView, WeeklyTextView, MonthlyTextView, YearlyTextView);

        // Set the listener for the refresh action
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(false);
        });

        return view;
    }


// Method to reset the text color of all navigation labels
    private void resetNavigationTextColors(TextView allsalesTextView, TextView dailyTextView, TextView weeklyTextView, TextView monthlyTextView, TextView yearlyTextView) {
        TextView[] navigationLabels = new TextView[]{
                allsalesTextView,
                dailyTextView,
                weeklyTextView,
                monthlyTextView,
                yearlyTextView
        };
        for (TextView textView : navigationLabels) {
            textView.setTextColor(getResources().getColor(R.color.selectednav)); // Change to your selected color
        }
    }



    // Method to highlight the navigation label corresponding to the selected card view
    private void highlightNavigationLabel(int index, TextView allsalesTextView, TextView dailyTextView, TextView weeklyTextView, TextView monthlyTextView, TextView yearlyTextView) {
        TextView[] navigationLabels = new TextView[]{
                allsalesTextView,
                dailyTextView,
                weeklyTextView,
                monthlyTextView,
                yearlyTextView
        };
        for (int i = 0; i < navigationLabels.length; i++) {
            TextView textView = navigationLabels[i];
            if (i == index) {
                // Highlight the selected navigation label
                textView.setTextColor(getResources().getColor(R.color.white)); // Change to your selected color
            } else {
                // Reset the color of unselected navigation labels
                textView.setTextColor(getResources().getColor(R.color.selectednav)); // Change to your normal color
            }
        }
    }

    private void calculateAndDisplayTotalSales(String filterOption) {
        // Get the adapter data and iterate through it to calculate the total sales
        double totalSales = 0.0;
        for (int i = 0; i < newAdapter.getItemCount(); i++) {
            SalesReportModel model = newAdapter.getItem(i);
            if (model != null && shouldIncludeSale(model, filterOption)) {
                totalSales += model.getSr_total();
            }
        }
        // Display the total sales in totalSalesTextView
        totalSalesTV.setText("Total Sales: " + currencyFormatter.format(totalSales));
    }

    // Method to determine whether the sale should be included based on the filter option
    private boolean shouldIncludeSale(SalesReportModel model, String filterOption) {
        if (filterOption.equals("daily")) {
            return isWithinCurrentDay(model.getSr_date());
        } else if (filterOption.equals("weekly")) {
            return isWithinCurrentWeek(model.getSr_date());
        } else if (filterOption.equals("monthly")) {
            return isWithinCurrentMonth(model.getSr_date());
        } else if (filterOption.equals("yearly")) {
            return isWithinCurrentYear(model.getSr_date());
        }

        return true;
    }

    private boolean isWithinCurrentDay(String saleDate) {
        try {
            // Parse the sale date string into a Date object
            Date date = dateFormat.parse(saleDate);

            // Get Calendar instances for the current date and the sale date
            Calendar currentCalendar = Calendar.getInstance();
            Calendar saleCalendar = Calendar.getInstance();
            saleCalendar.setTime(date);

            // Check if the sale date is today
            return currentCalendar.get(Calendar.YEAR) == saleCalendar.get(Calendar.YEAR) &&
                    currentCalendar.get(Calendar.DAY_OF_YEAR) == saleCalendar.get(Calendar.DAY_OF_YEAR);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to check if the sale date is within the current week
    private boolean isWithinCurrentWeek(String saleDate) {
        try {
            // Parse the sale date string into a Date object
            Date date = dateFormat.parse(saleDate);

            // Get Calendar instances for the current date and the sale date
            Calendar currentCalendar = Calendar.getInstance();
            Calendar saleCalendar = Calendar.getInstance();
            saleCalendar.setTime(date);

            // Check if the sale week is the same as the current week
            return currentCalendar.get(Calendar.YEAR) == saleCalendar.get(Calendar.YEAR) &&
                    currentCalendar.get(Calendar.WEEK_OF_YEAR) == saleCalendar.get(Calendar.WEEK_OF_YEAR);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to check if the sale date is within the current month
    private boolean isWithinCurrentMonth(String saleDate) {
        try {
            // Parse the sale date string into a Date object
            Date date = dateFormat.parse(saleDate);

            // Get Calendar instances for the current date and the sale date
            Calendar currentCalendar = Calendar.getInstance();
            Calendar saleCalendar = Calendar.getInstance();
            saleCalendar.setTime(date);

            // Check if the sale month and year are the same as the current month and year
            return currentCalendar.get(Calendar.YEAR) == saleCalendar.get(Calendar.YEAR) &&
                    currentCalendar.get(Calendar.MONTH) == saleCalendar.get(Calendar.MONTH);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to check if the sale date is within the current year
    private boolean isWithinCurrentYear(String saleDate) {
        try {
            // Parse the sale date string into a Date object
            Date date = dateFormat.parse(saleDate);

            // Get Calendar instances for the current date and the sale date
            Calendar currentCalendar = Calendar.getInstance();
            Calendar saleCalendar = Calendar.getInstance();
            saleCalendar.setTime(date);

            // Check if the sale year is the same as the current year
            return currentCalendar.get(Calendar.YEAR) == saleCalendar.get(Calendar.YEAR);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}