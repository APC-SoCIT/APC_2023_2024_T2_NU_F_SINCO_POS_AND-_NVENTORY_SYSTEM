package com.example.sincopossystemfullversion.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincopossystemfullversion.R;
import com.example.sincopossystemfullversion.SalesReportModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class HomeSSGraphNew extends FirebaseRecyclerAdapter<SalesReportModel, HomeSSGraphNew.myViewHolder> {

    private DecimalFormat currencyFormatter = new DecimalFormat("₱ #,###.00");

    public HomeSSGraphNew(@NonNull FirebaseRecyclerOptions<SalesReportModel> options, Context context) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull SalesReportModel model) {
        holder.setupBarChart(getSnapshots(), holder.itemView.getContext());
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_ssgraph, parent, false);
        return new myViewHolder(view);
    }


    static class myViewHolder extends RecyclerView.ViewHolder {

        BarChart barChart;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            barChart = itemView.findViewById(R.id.barChart);
        }

        public void setupBarChart(List<SalesReportModel> salesList, Context context) {
            ArrayList<BarEntry> barEntriesArrayList = getBarEntries(salesList);
            BarDataSet barDataSet = new BarDataSet(barEntriesArrayList, "Total Sales");
            BarData barData = new BarData(barDataSet);
            barChart.setData(barData);

            int pinColor = context.getResources().getColor(R.color.pin);
            int selectedNavColor = context.getResources().getColor(R.color.selectednav);

            int[] customColors = new int[] {pinColor, selectedNavColor};
            barDataSet.setColors(customColors);

            barDataSet.setValueTextColor(Color.BLACK);
            barDataSet.setValueTextSize(16f);
            barChart.getDescription().setEnabled(false);

            // Hide the bottom labels (months) from the X-axis
            XAxis xAxis = barChart.getXAxis();
            xAxis.setLabelCount(0); // Set the label count to 0 to hide the labels
            xAxis.setDrawGridLines(false);
            xAxis.setDrawAxisLine(false);

            // Custom value formatter to display the corresponding month labels instead of total sales
            barDataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getBarLabel(BarEntry barEntry) {
                    int index = (int) barEntry.getX(); // Index of the bar entry
                    ArrayList<String> months = getUniqueMonths(salesList);
                    return months.get(index); // Return the corresponding month
                }
            });

            barChart.invalidate(); // Refresh the chart

            barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    // Retrieve the total sales value of the clicked bar
                    int index = (int) e.getX(); // Index of the clicked bar
                    ArrayList<BarEntry> barEntries = getBarEntries(salesList);
                    long totalSales = (long) barEntries.get(index).getY();
                    DecimalFormat currencyFormatter = new DecimalFormat("₱ #,###.00");

                    // Display the total sales value (you can show it in a tooltip or another UI element)
                    Toast.makeText(context, "Total Sales: " + currencyFormatter.format(totalSales), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onNothingSelected() {
                    // Handle when nothing is selected (optional)
                }
            });
        }


        private ArrayList<String> getUniqueMonths(List<SalesReportModel> salesList) {
            // Define the order of months
            String[] monthOrder = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

            // Create a set to store unique months for the current year
            Set<String> uniqueMonths = new TreeSet<>(new MonthComparator(monthOrder));
            Calendar currentCalendar = Calendar.getInstance();
            int currentYear = currentCalendar.get(Calendar.YEAR);

            for (SalesReportModel model : salesList) {
                String date = model.getSr_date();
                if (date != null) { // Check if the date is not null
                    int year = extractYearFromDate(date);
                    if (year == currentYear) {
                        uniqueMonths.add(extractMonthFromDate(date));
                    }
                }
            }

            return new ArrayList<>(uniqueMonths);
        }

        // Custom comparator class to compare months based on their order
        private static class MonthComparator implements Comparator<String> {
            private final Map<String, Integer> monthIndices;

            public MonthComparator(String[] monthOrder) {
                // Map month names to their indices in the order array
                monthIndices = new HashMap<>();
                for (int i = 0; i < monthOrder.length; i++) {
                    monthIndices.put(monthOrder[i], i);
                }
            }

            @Override
            public int compare(String month1, String month2) {
                // Compare months based on their indices in the order array
                int index1 = monthIndices.getOrDefault(month1, Integer.MAX_VALUE);
                int index2 = monthIndices.getOrDefault(month2, Integer.MAX_VALUE);
                return Integer.compare(index1, index2);
            }
        }

        private ArrayList<BarEntry> getBarEntries(List<SalesReportModel> salesList) {
            // Define the order of months
            String[] monthOrder = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

            // Create a map to store the total sales for each month of the current year
            Map<String, Long> monthlySalesMap = new TreeMap<>(new MonthComparator(monthOrder));
            Calendar currentCalendar = Calendar.getInstance();
            int currentYear = currentCalendar.get(Calendar.YEAR);

            // Iterate over the sales list to aggregate total sales for each month of the current year
            for (SalesReportModel model : salesList) {
                String date = model.getSr_date();
                int year = extractYearFromDate(date);
                if (year == currentYear) {
                    String month = extractMonthFromDate(date);
                    long sales = model.getSr_total();

                    // If the month already exists in the map, update the total sales, otherwise, add a new entry
                    if (monthlySalesMap.containsKey(month)) {
                        long totalSales = monthlySalesMap.get(month);
                        monthlySalesMap.put(month, totalSales + sales);
                    } else {
                        monthlySalesMap.put(month, sales);
                    }
                }
            }

            // Create a list of bar entries from the aggregated monthly sales
            ArrayList<BarEntry> barEntriesArrayList = new ArrayList<>();
            int index = 0;
            for (Map.Entry<String, Long> entry : monthlySalesMap.entrySet()) {
                long totalSales = entry.getValue();
                barEntriesArrayList.add(new BarEntry(index, totalSales));
                index++;
            }
            return barEntriesArrayList;
        }

        private int extractYearFromDate(String date) {
            if (date == null || date.isEmpty()) {
                return -1; // or any other suitable default value to indicate invalid date
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                Date dateObj = dateFormat.parse(date);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dateObj);
                return calendar.get(Calendar.YEAR);
            } catch (ParseException e) {
                e.printStackTrace();
                return -1; // or any other suitable default value to indicate invalid date
            }
        }


        private String extractMonthFromDate(String date) {
            if (date == null || date.isEmpty()) {
                return "Invalid Date";
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
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
        }



    }
}
