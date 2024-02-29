package com.example.sincopossystemfullversion.Fragments;

import static android.content.Intent.getIntent;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

//import com.example.sincopossystemfullversion.Activity.CreateUser;
//import com.example.sincopossystemfullversion.Activity.PinCodeChange;
import com.example.sincopossystemfullversion.Activity.AddingProducts;
import com.example.sincopossystemfullversion.Activity.CreateUser;
import com.example.sincopossystemfullversion.Activity.EditProducts;
import com.example.sincopossystemfullversion.Activity.GenerateSales;
import com.example.sincopossystemfullversion.Activity.PinCodeChange;
import com.example.sincopossystemfullversion.Adapter.UserTypeAdapter;
import com.example.sincopossystemfullversion.R;
import com.example.sincopossystemfullversion.print.BluetoothBrowse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends Fragment {

    private String selectedUserType;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Assuming cardView is the ID of your CardView in fragment_settings.xml
        View cardView = view.findViewById(R.id.SettingsAddProduct);
        View cardView1 = view.findViewById(R.id.SettingsCreateUser);
        View cardView2 = view.findViewById(R.id.SettingsChangePinCode);
        View cardView3 = view.findViewById(R.id.SettingsEditProduct);
        View cardView4 = view.findViewById(R.id.SettingsPrinter);
        View cardView5 = view.findViewById(R.id.downloadData);

        selectedUserType = requireActivity().getIntent().getStringExtra("USER_TYPE");

        //Add Product
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedUserType != null && selectedUserType.equals("admin")) {
                    Intent intent = new Intent(requireContext(), AddingProducts.class);
                    startActivity(intent);
                } else {
                    showAdminPinDialog();
                }
            }
        });

        //Manage Account
        cardView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedUserType != null && selectedUserType.equals("admin")) {
                    Intent intent = new Intent(requireContext(), CreateUser.class);
                    startActivity(intent);
                } else {
                    showAdminPinDialog();
                }
            }
        });

        //Change PIN Code
        cardView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the selected user type from the adapter
                String userType = UserTypeAdapter.getSelectedUserType();

                // Check if userType is not null before starting the activity
                if (userType != null) {
                    Intent pinCodeChangeIntent = new Intent(requireContext(), PinCodeChange.class);
                    pinCodeChangeIntent.putExtra("USER_TYPE", userType);
                    requireContext().startActivity(pinCodeChangeIntent);
                } else {
                    // Handle the case where userType is null
                    Toast.makeText(requireContext(), "User type not selected", Toast.LENGTH_SHORT).show();
                }
            }
        });


        //Edit Products
        cardView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedUserType != null && selectedUserType.equals("admin")) {
                    Intent intent = new Intent(requireContext(), EditProducts.class);
                    startActivity(intent);
                } else {
                    showAdminPinDialog();
                }
            }
        });

        //Bluetooth
        cardView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), BluetoothBrowse.class);
                startActivity(intent);
            }
        });

        //Download Sales Report
        cardView5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedUserType != null && selectedUserType.equals("admin")) {
                    Intent intent = new Intent(requireContext(), GenerateSales.class);
                    startActivity(intent);
                } else {
                    showAdminPinDialog();
                }
            }
        });

        return view;
    }

    private void generateCSVFile(Map<String, Object> salesData) {
        // Logic to convert sales data to CSV format and save it as a file
        // For simplicity, let's assume you have a method to convert data to CSV format

        // Create a CSV string from the sales data
        String csvContent = convertToCSV(salesData);

        // Save the CSV content to a file
        if (csvContent != null) {
            try {
                // Get the Downloads directory
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

                // Create a file in the Downloads directory
                File file = new File(downloadsDir, "sales_report.csv");

                // Write the CSV content to the file
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(csvContent.getBytes());
                fos.close();

                // Indicate successful CSV file generation
                Toast.makeText(requireContext(), "Sales report CSV file generated successfully", Toast.LENGTH_SHORT).show();

                // You may choose to share the file here if needed

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Failed to save sales report CSV file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "Failed to generate sales report", Toast.LENGTH_SHORT).show();
        }
    }


    private void initiateDownload(String downloadUrl, String title, String destinationDirectory) {
        // Log the download information
        Log.d("Download", "Initiating download...");
        Log.d("Download", "Download URL: " + downloadUrl);
        Log.d("Download", "Title: " + title);
        Log.d("Download", "Destination Directory: " + destinationDirectory);

        // Code snippet for initiating the download using DownloadManager
        DownloadManager downloadManager = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(downloadUrl);

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle(title);
        request.setDestinationInExternalFilesDir(requireContext(), destinationDirectory, title);

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
        Uri contentUri = FileProvider.getUriForFile(requireContext(), "com.example.sincopossystemfullversion.fileprovider", file);

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
            // Write the workbook to a file
            FileOutputStream fileOut = new FileOutputStream("sales_report.xlsx");
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            // Initiate download
            File file = new File("sales_report.xlsx");
            Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", file);
            initiateDownload(uri.toString(), "Sales_Report.xlsx", "sales_reports");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to generate Excel file", Toast.LENGTH_SHORT).show();
        }
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
}