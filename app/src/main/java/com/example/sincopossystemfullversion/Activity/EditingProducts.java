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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.sincopossystemfullversion.ProductsModel;
import com.example.sincopossystemfullversion.R;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
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
import java.util.UUID;

public class EditingProducts extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText edtProductName, edtProductDescription, edtProductPrice;
    private Button btnUpdate, btnaddsize;
    private ImageView imgSelectedImage;
    private ProgressBar loadingProgressBar;
    private Spinner spinnerProductType, spinnerIngredientType;
    private Map<String, Long> sizesMap;
    String productName;
    ProductsModel product = new ProductsModel();
    private Uri selectedImageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_edit_products_popout);

        edtProductName = findViewById(R.id.edtProductName);
        edtProductPrice = findViewById(R.id.edtProductPrice);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnaddsize = findViewById(R.id.btnaddsize);
        imgSelectedImage = findViewById(R.id.imgSelectedImage);
        spinnerProductType = findViewById(R.id.spinnerProductType);
        spinnerIngredientType = findViewById(R.id.spinnerIngredientType);

        String[] productTypes = getResources().getStringArray(R.array.product_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(EditingProducts.this, android.R.layout.simple_spinner_dropdown_item, productTypes);
        spinnerProductType.setAdapter(adapter);

        String[] ingredientTypes = getResources().getStringArray(R.array.item_types);
        ArrayAdapter<String> ingredientAdapter = new ArrayAdapter<>(EditingProducts.this, android.R.layout.simple_spinner_dropdown_item, ingredientTypes);
        spinnerIngredientType.setAdapter(ingredientAdapter);

        productName = getIntent().getStringExtra("userKey");
        Log.d("ChangeImageProduct", "Retrieved productName: " + productName);

        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child("products");
        Query query = productsRef.orderByChild("product_name").equalTo(productName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Populate the product object with retrieved data
                        String imageUrl = snapshot.child("image_url").getValue(String.class);
                        String prodName = snapshot.child("product_name").getValue(String.class);
                        Long productPrice = snapshot.child("product_cost").getValue(Long.class);
                        String selectedProductType = snapshot.child("product_type").getValue(String.class);
                        String selectedIngredientType = snapshot.child("ingredient_type").getValue(String.class);

                        // Retrieve sizes data
                        Map<String, Long> sizesData = new HashMap<>();
                        DataSnapshot sizesSnapshot = snapshot.child("sizes");
                        if (sizesSnapshot.exists()) {
                            for (DataSnapshot sizeSnapshot : sizesSnapshot.getChildren()) {
                                String sizeKey = sizeSnapshot.getKey();
                                Long sizeValue = sizeSnapshot.getValue(Long.class);
                                sizesData.put(sizeKey, sizeValue);
                            }
                        }

                        product.setImage_url(imageUrl);
                        product.setProduct_name(prodName);
                        product.setProduct_cost(productPrice);
                        product.setProduct_type(selectedProductType);
                        product.setIngredient_type(selectedIngredientType);
                        product.setSizes(sizesData); // Set the sizes data

                        edtProductName.setText(prodName);
                        edtProductPrice.setText(String.valueOf(productPrice));

                        // Set the spinner selections
                        int productTypePosition = adapter.getPosition(selectedProductType);
                        spinnerProductType.setSelection(productTypePosition);

                        int ingredientTypePosition = ingredientAdapter.getPosition(selectedIngredientType);
                        spinnerIngredientType.setSelection(ingredientTypePosition);

                        if (imageUrl != null) {
                            Glide.with(getApplicationContext())
                                    .load(imageUrl)
                                    .into(imgSelectedImage);
                        } else {
                            Log.d("ChangeImageProduct", "No image URL found for productName: " + productName);
                        }
                    }
                } else {
                    Log.d("ChangeImageProduct", "No data found for productName: " + productName);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChangeImageProduct", "Database error: " + error.getMessage());
                // Handle onCancelled event
            }
        });


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

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    insertData();
            }
        });

        // Add size button click listener
        btnaddsize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSizeInputDialog(product);
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

    private void showSizeInputDialog(ProductsModel product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditingProducts.this);
        builder.setTitle("Enter Prices for Each Size");

        // Create a ScrollView to allow scrolling if content exceeds screen size
        ScrollView scrollView = new ScrollView(EditingProducts.this);

        // Create a LinearLayout to hold the TextView and EditText fields
        LinearLayout layout = new LinearLayout(EditingProducts.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Get the sizes and corresponding prices from the ProductsModel
        Map<String, Long> sizes = product.getSizes();

        // Define the order of sizes
        String[] orderedSizes = {"uno", "dos", "tres", "quatro", "sinco"};

        if (sizes != null) {
            // Create an array to hold the EditText fields
            EditText[] editTexts = new EditText[orderedSizes.length];

            // Iterate over the ordered sizes
            for (int i = 0; i < orderedSizes.length; i++) {
                String size = orderedSizes[i];
                // Check if the size exists in the product's sizes
                if (sizes.containsKey(size)) {
                    // Get the corresponding price
                    Long price = sizes.get(size);

                    // Create a TextView to display the size
                    TextView sizeTextView = new TextView(EditingProducts.this);
                    sizeTextView.setText(size);
                    layout.addView(sizeTextView);

                    // Create an EditText for the size
                    EditText editText = new EditText(EditingProducts.this);
                    editText.setHint("Price");
                    // Set the price if available, otherwise leave it empty
                    editText.setText(price != null ? String.valueOf(price) : "");
                    layout.addView(editText);

                    // Add the EditText to the array
                    editTexts[i] = editText;
                }
            }

            scrollView.addView(layout); // Add the LinearLayout to the ScrollView

            builder.setView(scrollView); // Set the ScrollView as the view of the AlertDialog

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Retrieve prices for each size and save them into sizesMap
                    Map<String, Long> updatedSizesMap = new HashMap<>();
                    ArrayList<Long> prices = new ArrayList<>();
                    for (EditText editText : editTexts) {
                        String priceString = editText.getText().toString();
                        if (!priceString.isEmpty()) {
                            try {
                                Long price = Long.parseLong(priceString);
                                prices.add(price);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                Toast.makeText(EditingProducts.this, "Invalid price format", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } else {
                            // Handle if price is not entered for a size
                            Toast.makeText(EditingProducts.this, "Please enter price for all sizes", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    // Update sizesMap with collected prices
                    for (int i = 0; i < orderedSizes.length; i++) {
                        String size = orderedSizes[i];
                        if (sizes.containsKey(size)) {
                            updatedSizesMap.put(size, prices.get(i));
                        }
                    }

                    // Update the instance variable sizesMap with the updated sizes
                    sizesMap = updatedSizesMap;

                    // Notify user that sizes and prices have been added
                    Toast.makeText(EditingProducts.this, "Sizes and prices added successfully", Toast.LENGTH_SHORT).show();
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
    }


    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imgSelectedImage.setImageURI(selectedImageUri); // Set the selected image to the imageView
        }
    }
    private void insertData() {
        // Show progress bar or loading indicator

        // First, upload the new image to Firebase Storage if a new image is selected
        if (selectedImageUri != null) {
            uploadImageToStorage(selectedImageUri, new OnImageUploadListener() {
                @Override
                public void onSuccess(Uri imageUrl) {
                    // Image upload successful, now update the product details in the database
                    updateProductDetails(imageUrl);
                }

                @Override
                public void onFailure(Exception e) {
                    // Image upload failed, show an error message
                    Toast.makeText(EditingProducts.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            // No new image selected, update the product details directly
            // Retain original sizes if not modified
            if (sizesMap == null || sizesMap.isEmpty()) {
                // Retrieve original product data to get sizes
                DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child("products");
                Query query = productsRef.orderByChild("product_name").equalTo(productName);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                // Get original sizes from the snapshot
                                Map<String, Long> originalSizes = new HashMap<>();
                                DataSnapshot sizesSnapshot = snapshot.child("sizes");
                                if (sizesSnapshot.exists()) {
                                    for (DataSnapshot sizeSnapshot : sizesSnapshot.getChildren()) {
                                        String sizeKey = sizeSnapshot.getKey();
                                        Long sizeValue = sizeSnapshot.getValue(Long.class);
                                        originalSizes.put(sizeKey, sizeValue);
                                    }
                                }
                                // Update sizesMap with original sizes
                                sizesMap = originalSizes;

                                // Proceed with updating product details
                                updateProductDetails(null);
                                return; // Exit onDataChange after updating sizesMap
                            }
                        } else {
                            // No data found for the product name, show an error message
                            Toast.makeText(EditingProducts.this, "No data found for product name: " + productName, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Database error occurred, show an error message
                        Toast.makeText(EditingProducts.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Sizes were modified, update product details directly
                updateProductDetails(null);
            }
        }
    }


    private void updateProductDetails(Uri imageUrl) {
        // Get the updated values from the EditText fields and spinners
        String updatedProductName = capitalizeFirstLetter(edtProductName.getText().toString().trim());
        String updatedProductPriceStr = edtProductPrice.getText().toString().trim();
        String selectedProductType = spinnerProductType.getSelectedItem().toString();
        String selectedIngredientType = spinnerIngredientType.getSelectedItem().toString();

        if (TextUtils.isEmpty(updatedProductName) || TextUtils.isEmpty(updatedProductPriceStr)) {
            // Product name or price is empty, show an error message
            Toast.makeText(EditingProducts.this, "Please enter product name and price", Toast.LENGTH_SHORT).show();
            return;
        }

        Long updatedProductPrice = Long.parseLong(updatedProductPriceStr);

        // Create a new HashMap to store the updated product details
        HashMap<String, Object> updatedProductDetails = new HashMap<>();
        updatedProductDetails.put("product_name", updatedProductName);
        updatedProductDetails.put("product_cost", updatedProductPrice);
        updatedProductDetails.put("product_type", selectedProductType);
        updatedProductDetails.put("ingredient_type", selectedIngredientType);
        updatedProductDetails.put("archived", false);
        updatedProductDetails.put("sizes", sizesMap);

        // Check if a new image is uploaded or not
        if (imageUrl != null) {
            // If a new image is uploaded, set the image URL in the updated product details
            updatedProductDetails.put("image_url", imageUrl.toString());
        }

        // Update the product in the database
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child("products");
        Query query = productsRef.orderByChild("product_name").equalTo(productName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Update the product details with the new values
                        snapshot.getRef().updateChildren(updatedProductDetails);

                        // Show a success message
                        Toast.makeText(EditingProducts.this, "Product details updated successfully", Toast.LENGTH_SHORT).show();

                        // Finish the activity
                        finish();
                    }
                } else {
                    // No data found for the product name, show an error message
                    Toast.makeText(EditingProducts.this, "No data found for product name: " + productName, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Database error occurred, show an error message
                Toast.makeText(EditingProducts.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void uploadImageToStorage(Uri imageUri, EditingProducts.OnImageUploadListener listener) {
        if (imageUri != null) {
            // Create a reference to the storage location where you want to upload the image
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("user_images/" + UUID.randomUUID().toString());

            // Upload the image to Firebase Storage
            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Image upload successful, get the download URL of the image
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Pass the image URL to the listener
                            listener.onSuccess(uri);
                        }).addOnFailureListener(e -> {
                            // Image upload failed, notify the listener
                            listener.onFailure(e);
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Image upload failed, notify the listener
                        listener.onFailure(e);
                    });
        } else {
            // Image URI is null, notify the listener of failure
            listener.onFailure(new Exception("Image URI is null"));
        }
    }

    // Define an interface for handling image upload events
    interface OnImageUploadListener {
        void onSuccess(Uri imageUrl);

        void onFailure(Exception e);
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

}
