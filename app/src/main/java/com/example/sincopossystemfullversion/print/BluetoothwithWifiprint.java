package com.example.sincopossystemfullversion.print;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.sincopossystemfullversion.Activity.CheckoutDialogFragment;
import com.example.sincopossystemfullversion.Activity.MainActivity;
import com.example.sincopossystemfullversion.ProductsModel;
import com.example.sincopossystemfullversion.R;
import com.example.sincopossystemfullversion.async.AsyncBluetoothEscPosPrint;
import com.example.sincopossystemfullversion.async.AsyncEscPosPrint;
import com.example.sincopossystemfullversion.async.AsyncEscPosPrinter;
import com.example.sincopossystemfullversion.async.AsyncTcpEscPosPrint;
import com.example.sincopossystemfullversion.connection.DeviceConnection;
import com.example.sincopossystemfullversion.connection.bluetooth.BluetoothConnection;
import com.example.sincopossystemfullversion.connection.bluetooth.BluetoothPrintersConnections;
import com.example.sincopossystemfullversion.connection.tcp.TcpConnection;
import com.example.sincopossystemfullversion.textparser.PrinterTextParserImg;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BluetoothwithWifiprint extends AppCompatActivity {

    private Button button;

    private AlertDialog alertDialog;

    private List<ProductsModel> selectedProducts;
    private float discount;
    Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_with_wifiprint);


        intent = getIntent();

        // Retrieve selectedProducts, subtotal, and cashAmount from the intent
        selectedProducts = intent.getParcelableArrayListExtra("selectedProducts");
        double subtotal = intent.getDoubleExtra("subtotal", 0.0);
        double cashAmount = intent.getDoubleExtra("cashAmount", 0.0); // Retrieve cashAmount from intent

        // Initialize selectedProducts if null
        if (selectedProducts == null) {
            selectedProducts = new ArrayList<>(); // Or initialize it with your data if available
        }

        // Initialize selectedProducts
        selectedProducts = new ArrayList<>(); // Or initialize it with your data if available
        button = findViewById(R.id.button_bluetooth);
        button.setOnClickListener(view -> printBluetooth(subtotal, cashAmount));

        ImageButton PrinterbtnBack = findViewById(R.id.PrintReceiptbtnBack);
        // Set an OnClickListener for the ImageButton
        PrinterbtnBack.setOnClickListener(v -> onBackPressed());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dismiss the dialog if it's still showing to prevent WindowLeaked exception
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    /*==============================================================================================
    ======================================BLUETOOTH PART============================================
    ==============================================================================================*/

    public interface OnBluetoothPermissionsGranted {
        void onPermissionsGranted();
    }

    public static final int PERMISSION_BLUETOOTH = 1;
    public static final int PERMISSION_BLUETOOTH_ADMIN = 2;
    public static final int PERMISSION_BLUETOOTH_CONNECT = 3;
    public static final int PERMISSION_BLUETOOTH_SCAN = 4;

    public OnBluetoothPermissionsGranted onBluetoothPermissionsGranted;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case PERMISSION_BLUETOOTH:
                case PERMISSION_BLUETOOTH_ADMIN:
                case PERMISSION_BLUETOOTH_CONNECT:
                case PERMISSION_BLUETOOTH_SCAN:
                    this.checkBluetoothPermissions(this.onBluetoothPermissionsGranted);
                    break;
            }
        }
    }

    public void checkBluetoothPermissions(OnBluetoothPermissionsGranted onBluetoothPermissionsGranted) {
        this.onBluetoothPermissionsGranted = onBluetoothPermissionsGranted;
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, PERMISSION_BLUETOOTH);
        } else if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, PERMISSION_BLUETOOTH_ADMIN);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_BLUETOOTH_CONNECT);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, PERMISSION_BLUETOOTH_SCAN);
        } else {
            this.onBluetoothPermissionsGranted.onPermissionsGranted();
        }
    }

    private BluetoothConnection selectedDevice;

    public void browseBluetoothDevice() {
        this.checkBluetoothPermissions(() -> {
            final BluetoothConnection[] bluetoothDevicesList = (new BluetoothPrintersConnections()).getList();

            if (bluetoothDevicesList != null) {
                final String[] items = new String[bluetoothDevicesList.length + 1];
                items[0] = "Default printer";
                int i = 0;
                for (BluetoothConnection device : bluetoothDevicesList) {
                    items[++i] = device.getDevice().getName();
                }

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(BluetoothwithWifiprint.this);
                alertDialog.setTitle("Bluetooth printer selection");
                alertDialog.setItems(
                        items,
                        (dialogInterface, i1) -> {
                            int index = i1 - 1;
                            if (index == -1) {
                                selectedDevice = null;
                            } else {
                                selectedDevice = bluetoothDevicesList[index];
                            }
                            Button button = findViewById(R.id.button_bluetooth_browse);
                            button.setText(items[i1]);
                        }
                );

                AlertDialog alert = alertDialog.create();
                alert.setCanceledOnTouchOutside(false);
                alert.show();
            }
        });

    }


    // Modify the printBluetooth method to accept the subtotal parameter
    public void printBluetooth(double subtotal, double cashAmount) {
        if (selectedProducts == null) {
            Log.e("BluetoothwithWifiprint", "Selected products list is null");
            return;
        }

        this.checkBluetoothPermissions(() -> {
            String customerName = ((EditText) findViewById(R.id.CustomerName)).getText().toString();
            if (customerName.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter customer name", Toast.LENGTH_SHORT).show();
                return;
            }
            double vatRate = 0.12;
            double vatAmount = subtotal * vatRate;
            double totalPrice = subtotal + vatAmount;
            double change = cashAmount - totalPrice;

            new AsyncBluetoothEscPosPrint(
                    this,
                    new AsyncEscPosPrint.OnPrintFinished() {
                        @Override
                        public void onError(AsyncEscPosPrinter asyncEscPosPrinter, int codeException) {
                            Log.e("Async.OnPrintFinished", "AsyncEscPosPrint.OnPrintFinished : An error occurred !");
                            // Navigation logic to move to MainActivity and open ProductsFragment
                            Intent intent = new Intent(BluetoothwithWifiprint.this, MainActivity.class);
                            intent.putExtra("openFragment", "products");
                            startActivity(intent);
                        }

                        @Override
                        public void onSuccess(AsyncEscPosPrinter asyncEscPosPrinter) {
                            Log.i("Async.OnPrintFinished", "AsyncEscPosPrint.OnPrintFinished : Print is finished !");
                            // Navigation logic to move to MainActivity and open ProductsFragment
                            Intent intent = new Intent(BluetoothwithWifiprint.this, MainActivity.class);
                            intent.putExtra("openFragment", "products");
                            startActivity(intent);
                        }
                    }
            ).execute(getAsyncEscPosPrinter(selectedDevice, customerName, subtotal, totalPrice, cashAmount, change, vatRate));
        });
    }

    //POS PRINTING PROCESS

    public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection, String customerName, double subtotal, double totalPrice, double cashAmount, double change, double vatRate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 32);

        // Extract data from the intent
        List<ProductsModel> selectedProducts = intent.getParcelableArrayListExtra("selectedProducts");
        if (selectedProducts == null) {
            Log.e("BluetoothwithWifiprint", "Selected products list is null");
            return null;
        }

        StringBuilder productsText = new StringBuilder();
        double totalCost = 0.0;
        double vatAmount = subtotal * vatRate;

        String header = String.format("%-20s %-7s %-7s %-7s\n", "Item", "Qty", "Size", "Total");
        productsText.append(header);

        for (ProductsModel product : selectedProducts) {
            String productName = product.getProduct_name();
            int qty = product.getQuantity();
            String size = product.getSelectedSize();
            double cost = product.getProduct_cost();
            double total = cost * qty;
            totalCost += total;

            String productLine = String.format("%-20s %-7d %-7s ₱%.2f\n", productName, qty, size, total);
            productsText.append(productLine);
        }

        Log.d("PrintingReceipt", "Receipt details -\n" +
                "Customer: " + customerName + "\n" +
                "Products:\n" + productsText.toString() +
                "Subtotal: ₱" + String.format("%.2f", subtotal) + "\n" +
                "Total Price: ₱" + String.format("%.2f", totalPrice) + "\n" +
                "Subtotal (VAT Inclusive) ₱" + String.format("%.2f", subtotal) + "\n" +
                "VAT (" + (vatRate * 100) + "%)            ₱" + String.format("%.2f", vatAmount) + "\n" +
                "Cash: ₱" + String.format("%.2f", cashAmount) + "\n" +
                "Change: ₱" + String.format("%.2f", change));

        return printer.addTextToPrint(
                "SINCO CAFE\n" +
                        "5th Floor, National University Fairview\n" +
                        "Tel: +63(2)5368053\n" +
                        "=====================================\n" +
                        "Date: " + dateFormat.format(new Date()) + "  Time: " + timeFormat.format(new Date()) + "\n" +
                        "Customer: " + customerName + "\n" +
                        "-------------------------------------\n" +
                        productsText.toString() +
                        "-------------------------------------\n" +
                        "Subtotal                  ₱" + String.format("%.2f", subtotal) + "\n" +
                        "VAT (" + (vatRate * 100) + "%)            ₱" + String.format("%.2f", vatAmount) + "\n" +
                        "Total Price               ₱" + String.format("%.2f", totalPrice) + "\n" +
                        "Cash                      ₱" + String.format("%.2f", cashAmount) + "\n" +
                        "Change                    ₱" + String.format("%.2f", change) + "\n" +
                        "=====================================\n" +
                        "Thank You! Visit us again soon!\n" +
                        "[C]<qrcode size='20'>https://www.facebook.com/sincocafe</qrcode>\n"
        );
    }
}