package com.example.sincopossystemfullversion.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincopossystemfullversion.R;
import com.example.sincopossystemfullversion.SalesReportModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class HomeSSNew extends FirebaseRecyclerAdapter <SalesReportModel, HomeSSNew.myViewHolder>{

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     * @param context
     */

    // private Map<String, Long> monthSalesMap = new HashMap<>();
    private Set<String> encounteredMonths = new HashSet<>(); // Set to store encountered months
    private TreeMap<String, Long> monthSalesMap = new TreeMap<>(); // TreeMap to automatically sort by keys
    private List<String> monthList = new ArrayList<>();

    DecimalFormat currencyFormatter = new DecimalFormat("₱ #,###.00");

    public HomeSSNew(@NonNull FirebaseRecyclerOptions<SalesReportModel> options, Context context) {
        super(options);
        fetchDataAndCalculateTotalSales(); initializeMonthList();
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull SalesReportModel model) {
        if (position < monthList.size()) {
            String month = monthList.get(position);
            holder.ssmonth.setText(month);

            // Retrieve total sales for the corresponding month
            Long totalSales = monthSalesMap.get(month);
            if (totalSales != null) {
                holder.sstotal.setText(currencyFormatter.format((totalSales)));
            } else {
                // Handle the case when totalSales is null
                holder.sstotal.setText("N/A");
            }
        } else {
            // Handle the case when position exceeds the size of monthList
            holder.ssmonth.setText("N/A");
            holder.sstotal.setText("N/A");
            holder.ssmonth.setVisibility(View.GONE);
            holder.sstotal.setVisibility(View.GONE);
        }
    }


    private List<String> sortMonthsChronologically(Map<String, Long> monthSalesMap) {
        // Create a list of month names
        String[] monthNames = new DateFormatSymbols().getMonths();

        // Create a TreeMap to hold the months sorted by their index in the monthNames array
        TreeMap<Integer, String> sortedMonths = new TreeMap<>();

        // Iterate through the monthSalesMap
        for (Map.Entry<String, Long> entry : monthSalesMap.entrySet()) {
            String monthName = entry.getKey();
            int monthIndex = Arrays.asList(monthNames).indexOf(monthName);

            // Ensure the month index is valid
            if (monthIndex >= 0) {
                sortedMonths.put(monthIndex, monthName);
            }
        }

        // Convert TreeMap values to a list (sorted by month index)
        List<String> sortedMonthList = new ArrayList<>(sortedMonths.values());
        return sortedMonthList;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_salessummary, parent, false);
        return new myViewHolder(view);
    }

    static class myViewHolder extends RecyclerView.ViewHolder {
        TextView ssmonth, sstotal;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            ssmonth = itemView.findViewById(R.id.homeSsMonth);
            sstotal = itemView.findViewById(R.id.homeSsSales);
        }
    }

    private void fetchDataAndCalculateTotalSales() {
        DatabaseReference salesRef = FirebaseDatabase.getInstance().getReference().child("sales_report");

        salesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear existing data to avoid duplication
                monthSalesMap.clear();

                // Iterate over the dataSnapshot to process each SalesReportModel object
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    SalesReportModel model = snapshot.getValue(SalesReportModel.class);
                    String month = extractMonthFromDate(model.getSr_date());

                    // Update total sales for the corresponding month
                    Long totalSales = monthSalesMap.getOrDefault(month, 0L);
                    totalSales += model.getSr_total();
                    monthSalesMap.put(month, totalSales);
                }

                // Sort the months chronologically
                List<String> sortedMonths = sortMonthsChronologically(monthSalesMap);

            // Print the sorted months
                Log.d("Sorted Months", sortedMonths.toString());

            // Notify the adapter with sorted data
                monthList.clear();
                monthList.addAll(sortedMonths);

                notifyDataSetChanged(); // Refresh the RecyclerView once data has been processed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors here
                Log.e("Firebase", "Error reading data: " + databaseError.getMessage());
            }
        });
    }


    private void initializeMonthList() {
        monthList.add("January");
        monthList.add("February");
        monthList.add("March");
        monthList.add("April");
        monthList.add("May");
        monthList.add("June");
        monthList.add("July");
        monthList.add("August");
        monthList.add("September");
        monthList.add("October");
        monthList.add("November");
        monthList.add("December");
    }

    private String extractMonthFromDate(String date) {
        if (date != null && !date.isEmpty()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            try {
                Date dateObj = dateFormat.parse(date);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dateObj);
                int month = calendar.get(Calendar.MONTH); // Month is zero-based
                return new DateFormatSymbols().getMonths()[month];
            } catch (ParseException e) {
                e.printStackTrace();
                return "Invalid Date";
            }
        } else {
            return "Invalid Date";
        }
    }


}

