package com.example.sincopossystemfullversion.Activity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincopossystemfullversion.Adapter.SalesReportNew;
import com.example.sincopossystemfullversion.R;
import com.example.sincopossystemfullversion.SalesReportModel;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class GenerateSales extends AppCompatActivity {
    private List<CardView> cardViews;

    private Map<String, Object> salesData;
    private TextView GenerateAllsalesTextView, GenerateDailyTextView, GenerateWeeklyTextView, GenerateMonthlyTextView, GenerateYearlyTextView;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private boolean cardClicked = false; // Flag to track whether a card has been clicked

    private DatabaseReference salesReportRef;


    private SalesReportNew newAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generate_sales);


        // Initialize the list of CardViews
        cardViews = new ArrayList<>();
        CardView GenerateAllsales = findViewById(R.id.GenerateAllsalesCardView);
        CardView GeneratedailyCardView = findViewById(R.id.GenerateDailyCardView);
        CardView GenerateweeklyCardView = findViewById(R.id.GenerateWeeklyCardView);
        CardView GeneratemonthlyCardView = findViewById(R.id.GenerateMonthlyCardView);
        CardView GenerateyearlyCardView = findViewById(R.id.GenerateYearlyCardView);

        cardViews.add(GenerateAllsales);
        cardViews.add(GeneratedailyCardView);
        cardViews.add(GenerateweeklyCardView);
        cardViews.add(GeneratemonthlyCardView);
        cardViews.add(GenerateyearlyCardView);

        // Assign TextViews to class variables
        GenerateAllsalesTextView = findViewById(R.id.GenerateAllsalesTextView);
        GenerateDailyTextView = findViewById(R.id.GenerateDailyTextView);
        GenerateWeeklyTextView = findViewById(R.id.GenerateWeeklyTextView);
        GenerateMonthlyTextView = findViewById(R.id.GenerateMonthlyTextView);
        GenerateYearlyTextView = findViewById(R.id.GenerateYearlyTextView);

        recyclerView = findViewById(R.id.salesreportRV);

        // Initialize salesReportRef with the appropriate Firebase Database reference
        salesReportRef = FirebaseDatabase.getInstance().getReference().child("sales_report");


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        progressBar = findViewById(R.id.progressBar);



        // Retrieve sales data from Firebase
        salesReportRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    salesData = new HashMap<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String key = dataSnapshot.getKey();
                        Object value = dataSnapshot.getValue();
                        salesData.put(key, value);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        Date currentDate = Calendar.getInstance().getTime();

        progressBar.setVisibility(View.VISIBLE);
        Query baseQuery = FirebaseDatabase.getInstance().getReference().child("sales_report")
                .orderByChild("sr_date")
                .startAt(currentDate.getTime());

        FirebaseRecyclerOptions<SalesReportModel> options =
                new FirebaseRecyclerOptions.Builder<SalesReportModel>()
                        .setQuery(baseQuery, SalesReportModel.class)
                        .build();

        newAdapter = new SalesReportNew(options, this);
        recyclerView.setAdapter(newAdapter);
        newAdapter.startListening();

        newAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                // Hide ProgressBar when loading finishes
                progressBar.setVisibility(View.GONE);
            }
        });

        for (int i = 0; i < cardViews.size(); i++) {
            final int index = i;
            cardViews.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cardClicked = true;
                    handleCardViewClick(index);
                }
            });
        }
        CardView generateCardView = findViewById(R.id.GenerateCardview);
        generateCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cardClicked) {
                generateCSVFile();
                } else {
                    Toast.makeText(GenerateSales.this, "Please select a filter option first.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void handleCardViewClick(int index) {
        resetCardViewsBackground();
        resetTextViewColors();


        cardViews.get(index).setCardBackgroundColor(getResources().getColor(R.color.selectednav));


        highlightNavigationLabel(index);


        String filterOption = "";
        switch (index) {
            case 0: // AllsalesCardView
                filterOption = "";
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
        newAdapter.setFilterOption(filterOption);
    }

    private void resetCardViewsBackground() {
        for (CardView cardView : cardViews) {
            cardView.setCardBackgroundColor(getResources().getColor(R.color.lightcolor));
        }
    }

    private void resetTextViewColors() {
        GenerateAllsalesTextView.setTextColor(getResources().getColor(R.color.selectednav));
        GenerateDailyTextView.setTextColor(getResources().getColor(R.color.selectednav));
        GenerateWeeklyTextView.setTextColor(getResources().getColor(R.color.selectednav));
        GenerateMonthlyTextView.setTextColor(getResources().getColor(R.color.selectednav));
        GenerateYearlyTextView.setTextColor(getResources().getColor(R.color.selectednav));
    }

    private void highlightNavigationLabel(int index) {
        switch (index) {
            case 0:
                GenerateAllsalesTextView.setTextColor(getResources().getColor(R.color.black));
                break;
            case 1:
                GenerateDailyTextView.setTextColor(getResources().getColor(R.color.black));
                break;
            case 2:
                GenerateWeeklyTextView.setTextColor(getResources().getColor(R.color.black));
                break;
            case 3:
                GenerateMonthlyTextView.setTextColor(getResources().getColor(R.color.black));
                break;
            case 4:
                GenerateYearlyTextView.setTextColor(getResources().getColor(R.color.black));
                break;
        }
    }


    private void generateCSVFile() {
        if (salesData == null || salesData.isEmpty()) {
            Toast.makeText(this, "No sales data available to generate CSV", Toast.LENGTH_SHORT).show();
            return;
        }

        String filterOption = newAdapter.getFilterOption();
        String csvContent = convertToCSV(filterData(salesData, filterOption));


        // Save the CSV content to a file
        if (csvContent != null) {
            try {
                // Get the Downloads directory
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

                String fileName = "sales_report_" + System.currentTimeMillis() + ".csv";
                File file = new File(downloadsDir, fileName);

                // Write the CSV content to the file
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(csvContent.getBytes());
                fos.close();


                Toast.makeText(this, "Sales report CSV file generated successfully", Toast.LENGTH_SHORT).show();



            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to save sales report CSV file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to generate sales report", Toast.LENGTH_SHORT).show();
        }
    }


    private Map<String, Object> filterData(Map<String, Object> salesData, String filterOption) {
        Map<String, Object> filteredData = new HashMap<>();

        for (Map.Entry<String, Object> entry : salesData.entrySet()) {
            Map<String, Object> salesEntry = (Map<String, Object>) entry.getValue();

            // Check if the entry matches the selected filter option
            if (matchesFilter(salesEntry, filterOption)) {
                filteredData.put(entry.getKey(), entry.getValue());
            }
        }

        return filteredData;
    }


    private boolean matchesFilter(Map<String, Object> salesEntry, String filterOption) {
        // Assuming "sr_date" is the key where the date is stored as a timestamp
        if (salesEntry.containsKey("sr_date")) {
            String dateString = (String) salesEntry.get("sr_date");

            try {
                // Parse the date string to get a Date object
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = dateFormat.parse(dateString);

                // Convert the Date object to a timestamp
                long entryDateTimestamp = date.getTime();
                long currentDateTimestamp = Calendar.getInstance().getTimeInMillis();

                switch (filterOption) {
                    case "daily":
                        return isSameDay(entryDateTimestamp, currentDateTimestamp);
                    case "weekly":
                        return isSameWeek(entryDateTimestamp, currentDateTimestamp);
                    case "monthly":
                        return isSameMonth(entryDateTimestamp, currentDateTimestamp);
                    case "yearly":
                        return isSameYear(entryDateTimestamp, currentDateTimestamp);
                    default:
                        return true;
                }
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }


    private boolean isSameDay(long timestamp1, long timestamp2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(timestamp1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(timestamp2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    // Method to check if two timestamps correspond to the same week
    private boolean isSameWeek(long timestamp1, long timestamp2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(timestamp1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(timestamp2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
    }


    private boolean isSameMonth(long timestamp1, long timestamp2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(timestamp1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(timestamp2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
    }


    private boolean isSameYear(long timestamp1, long timestamp2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(timestamp1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(timestamp2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
    }
    private void initiateDownload(String downloadUrl, String title, String destinationDirectory) {
        // Log the download information
        Log.d("Download", "Initiating download...");
        Log.d("Download", "Download URL: " + downloadUrl);
        Log.d("Download", "Title: " + title);
        Log.d("Download", "Destination Directory: " + destinationDirectory);

        // Code snippet for initiating the download using DownloadManager
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(downloadUrl);

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle(title);
        request.setDestinationInExternalFilesDir(this, destinationDirectory, title);

        // Enqueue the download request
        long downloadId = downloadManager.enqueue(request);

        // Check if the download was successful
        if (downloadId != -1) {
            Log.d("Download", "Download enqueued successfully with ID: " + downloadId);
        } else {
            Log.e("Download", "Failed to enqueue download");
        }
    }


    private String convertToCSV(Map<String, Object> salesData) {
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("Date,Payment Method,Price,Product,Quantity,Total,Transaction ID\n");

        // Iterate over each entry in the salesData map
        for (Map.Entry<String, Object> entry : salesData.entrySet()) {
            Map<String, Object> salesEntry = (Map<String, Object>) entry.getValue();
            String date = salesEntry.get("sr_date").toString();
            String paymentMethod = salesEntry.get("sr_payment").toString();
            String price = salesEntry.get("sr_price").toString();
            String product = salesEntry.get("sr_product").toString();
            String quantity = salesEntry.get("sr_qty").toString();
            String total = salesEntry.get("sr_total").toString();
            String transactionId = salesEntry.get("sr_transactionid").toString();

            // Append the fields as a CSV row
            csvBuilder.append(date).append(",");
            csvBuilder.append(paymentMethod).append(",");
            csvBuilder.append(price).append(",");
            csvBuilder.append(product).append(",");
            csvBuilder.append(quantity).append(",");
            csvBuilder.append(total).append(",");
            csvBuilder.append(transactionId).append("\n");
        }

        return csvBuilder.toString();
    }

    private void shareSalesReport(File file) {
        // Get the content URI using FileProvider
        Uri contentUri = FileProvider.getUriForFile(this, "com.example.sincopossystemfullversion.fileprovider", file);

        // Create an intent to share the file
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/csv");
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Start the chooser for sharing
        startActivity(Intent.createChooser(shareIntent, "Share Sales Report"));
    }


    private void generateExcelFile(Map<String, Object> salesData) {
        // Create a new Excel workbook
        Workbook workbook = new XSSFWorkbook();
        // Create a new sheet
        Sheet sheet = workbook.createSheet("Sales Report");
        // Create a header row
        Row headerRow = sheet.createRow(0);
        // Define cell styles
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = workbook.createFont();
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerCellStyle.setFont(headerFont);

        // Create header cells
        String[] columns = {"Date", "Payment Method", "Price", "Product", "Quantity", "Total", "Transaction ID"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Populate data rows
        int rowNum = 1;
        for (Map.Entry<String, Object> entry : salesData.entrySet()) {
            Map<String, Object> salesEntry = (Map<String, Object>) entry.getValue();
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(salesEntry.get("sr_date").toString());
            row.createCell(1).setCellValue(salesEntry.get("sr_payment").toString());
            row.createCell(2).setCellValue(salesEntry.get("sr_price").toString());
            row.createCell(3).setCellValue(salesEntry.get("sr_product").toString());
            row.createCell(4).setCellValue(salesEntry.get("sr_qty").toString());
            row.createCell(5).setCellValue(salesEntry.get("sr_total").toString());
            row.createCell(6).setCellValue(salesEntry.get("sr_transactionid").toString());
        }

        // Resize columns to fit content
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        try {
            // Perform file writing operation (hypothetical)
            FileWriter writer = new FileWriter("sales_report.xlsx");
            writer.write("Sample content");
            writer.close();

            // Initiate download
            File file = new File("sales_report.xlsx");
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            initiateDownload(uri.toString(), "Sales_Report.xlsx", "sales_reports");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate Excel file", Toast.LENGTH_SHORT).show();
        }
    }
}