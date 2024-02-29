package com.example.sincopossystemfullversion.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sincopossystemfullversion.R;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddingProducts extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText edtProductName, edtProductDescription, edtProductPrice;
    private Button btnAddProduct, btnaddsize;
    private ImageView imgSelectedImage;
    private ProgressBar loadingProgressBar;
    private MaterialAutoCompleteTextView spinnerProductType, spinnerIngredientType;
    private Map<String, Long> sizesMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding_products);

        // Initialize views
        edtProductName = findViewById(R.id.edtProductName);
        edtProductPrice = findViewById(R.id.edtProductPrice);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnaddsize = findViewById(R.id.btnaddsize);
        imgSelectedImage = findViewById(R.id.imgSelectedImage);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        spinnerProductType = findViewById(R.id.spinnerProductType);
        spinnerIngredientType = findViewById(R.id.spinnerIngredientType);

        // Set up spinners
        String[] productTypes = getResources().getStringArray(R.array.product_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(AddingProducts.this, android.R.layout.simple_spinner_dropdown_item, productTypes);
        spinnerProductType.setAdapter(adapter);

        String[] ingredientTypes = getResources().getStringArray(R.array.item_types);
        ArrayAdapter<String> ingredientAdapter = new ArrayAdapter<>(AddingProducts.this, android.R.layout.simple_spinner_dropdown_item, ingredientTypes);
        spinnerIngredientType.setAdapter(ingredientAdapter);

        // Back button click listener
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Initialize sizesMap
        sizesMap = new HashMap<>();

        // Add product button click listener
        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if sizesMap has been initialized
                if (sizesMap.isEmpty()) {
                    // Sizes not added, show error message
                    Toast.makeText(AddingProducts.this, "Please add sizes and prices", Toast.LENGTH_SHORT).show();
                } else {
                    // Sizes added, proceed with adding product
                    insertData();
                }
            }
        });

        // Add size button click listener
        btnaddsize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSizeInputDialog();
            }
        });

        // Image selection click listener
        imgSelectedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
    }

    private void showSizeInputDialog() {
        // Create a dialog for inputting prices for each size
        AlertDialog.Builder builder = new AlertDialog.Builder(AddingProducts.this);
        builder.setTitle("Enter Prices for Each Size");

        // Create a ScrollView
        ScrollView scrollView = new ScrollView(AddingProducts.this);

        // Create a LinearLayout for EditTexts
        LinearLayout layout = new LinearLayout(AddingProducts.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        String[] sizes = {"uno", "dos", "tres", "quatro", "sinco"};
        final EditText[] editTexts = new EditText[sizes.length];

        for (int i = 0; i < sizes.length; i++) {
            EditText editText = new EditText(AddingProducts.this);
            editText.setHint(sizes[i] + " Price");
            layout.addView(editText);
            editTexts[i] = editText;
        }

        // Add the LinearLayout to ScrollView
        scrollView.addView(layout);

        builder.setView(scrollView);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Retrieve prices for each size and save them into sizesMap
                ArrayList<Long> prices = new ArrayList<>();
                for (EditText editText : editTexts) {
                    String priceString = editText.getText().toString();
                    if (!priceString.isEmpty()) {
                        try {
                            Long price = Long.parseLong(priceString);
                            prices.add(price);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            Toast.makeText(AddingProducts.this, "Invalid price format", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        // Handle if price is not entered for a size
                        Toast.makeText(AddingProducts.this, "Please enter price for all sizes", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // Update sizesMap with collected prices
                for (int i = 0; i < sizes.length; i++) {
                    sizesMap.put(sizes[i], prices.get(i));
                }

                // Notify user that sizes and prices have been added
                Toast.makeText(AddingProducts.this, "Sizes and prices added successfully", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }



    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            imgSelectedImage.setVisibility(View.VISIBLE);
            imgSelectedImage.setImageURI(selectedImageUri);
        }
    }

    private void insertData() {
        // Disable user input
        loadingProgressBar.setVisibility(View.VISIBLE); // Show loading icon

        // Retrieve other product details
        String productPrice = edtProductPrice.getText().toString();

        // Proceed with adding product
        if (!productPrice.isEmpty()) { // Check if productPrice is not empty
            try {
                Long priceLong = Long.parseLong(productPrice);

                Map<Object, Object> map = new HashMap<>();
                map.put("product_name", capitalizeFirstLetter(edtProductName.getText().toString()));
                map.put("product_cost", priceLong);
                map.put("archived", false);

                // Retrieve selected product type from the spinner
                String selectedProductType = spinnerProductType.getText().toString();
                map.put("product_type", selectedProductType); // Add selected product type to the map

                // Retrieve selected ingredient type from the spinner
                String selectedIngredientType = spinnerIngredientType.getText().toString();
                map.put("ingredient_type", selectedIngredientType); // Add selected ingredient type to the map

                // Add sizes map to the main map
                map.put("sizes", sizesMap);

                // Use the actual image URI instead of the text input
                if (imgSelectedImage.getDrawable() != null) {
                    Uri imageUri = getImageUri();
                    // Get a reference to the Firebase storage location
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("product_images").child(imageUri.getLastPathSegment());

                    // Upload the image to Firebase storage
                    storageRef.putFile(imageUri)
                            .addOnSuccessListener(taskSnapshot -> {
                                // Image uploaded successfully, get the download URL
                                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    map.put("image_url", uri.toString());
                                    // Once image URL is obtained, add other product data to the database
                                    DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child("products");
                                    String productId = productsRef.push().getKey(); // Generate unique key for the product
                                    productsRef.child(productId).setValue(map)
                                            .addOnSuccessListener(unused -> {
                                                Toast.makeText(AddingProducts.this, "New Product Added Successfully", Toast.LENGTH_SHORT).show();
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(AddingProducts.this, "Error: Cannot add new product. Please try again.", Toast.LENGTH_SHORT).show();
                                                loadingProgressBar.setVisibility(View.GONE); // Hide loading icon on failure
                                            });
                                });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AddingProducts.this, "Error: Image upload failed. Please try again.", Toast.LENGTH_SHORT).show();
                                loadingProgressBar.setVisibility(View.GONE); // Hide loading icon on failure
                            });
                } else {
                    // If no image is selected, directly add other product data to the database
                    DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child("products");
                    String productId = productsRef.push().getKey(); // Generate unique key for the product
                    productsRef.child(productId).setValue(map)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(AddingProducts.this, "New Product Added Successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AddingProducts.this, "Error: Cannot add new product. Please try again.", Toast.LENGTH_SHORT).show();
                                loadingProgressBar.setVisibility(View.GONE); // Hide loading icon on failure
                            });
                }
            } catch (NumberFormatException e) {
                Toast.makeText(AddingProducts.this, "Error: Invalid price format.", Toast.LENGTH_SHORT).show();
                loadingProgressBar.setVisibility(View.GONE); // Hide loading icon on failure
            }
        } else {
            Toast.makeText(AddingProducts.this, "Error: Please enter a price.", Toast.LENGTH_SHORT).show();
            loadingProgressBar.setVisibility(View.GONE); // Hide loading icon on failure
        }
    }

    private Uri getImageUri() {
        // Get the URI from the ImageView
        Uri imageUri = null;
        Drawable drawable = imgSelectedImage.getDrawable();

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();

            // Save the bitmap to a temporary file
            File tempFile = createTempImageFile();
            saveBitmapToFile(bitmap, tempFile);

            // Get the URI from the temporary file
            imageUri = Uri.fromFile(tempFile);
        }

        return imageUri;
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
    // Helper method to create a temporary image file
    private File createTempImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Helper method to save a bitmap to a file
    private void saveBitmapToFile(Bitmap bitmap, File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
