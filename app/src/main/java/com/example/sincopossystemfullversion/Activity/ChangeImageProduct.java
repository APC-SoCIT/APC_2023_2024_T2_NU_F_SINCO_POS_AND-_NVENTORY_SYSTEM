package com.example.sincopossystemfullversion.Activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.target.Target;
import com.example.sincopossystemfullversion.Adapter.EditProductNew;
import com.example.sincopossystemfullversion.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class ChangeImageProduct extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    ImageView imageView;
    private Uri selectedImageUri;
    private String userKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_productpic);

        imageView = findViewById(R.id.imageView);

        String productName = getIntent().getStringExtra("userKey");
        Log.d("ChangeImageProduct", "Retrieved productName: " + productName);

        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child("products");
        Query query = productsRef.orderByChild("product_name").equalTo(productName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String imageUrl = snapshot.child("image_url").getValue(String.class);
                        if (imageUrl != null) {
                            Glide.with(getApplicationContext())
                                    .load(imageUrl)
                                    .into(imageView);
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



        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        // Back button click listener
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        imageView = findViewById(R.id.imageView);
        Button confirmButton = findViewById(R.id.confirmButton);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new Intent to hold the selected image URI
                Intent resultIntent = new Intent();

                // Pass the selected image URI as an extra with the intent
                if (selectedImageUri != null) {
                    resultIntent.putExtra("selectedImageUri", selectedImageUri.toString());
                }

                // Set the result to be returned to the calling activity/dialog
                setResult(Activity.RESULT_OK, resultIntent);

                // Finish the current activity to return to the calling activity/dialog
                finish();
            }
        });


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
            imageView.setImageURI(selectedImageUri); // Set the selected image to the imageView
        }
    }

    private void uploadImageToStorage(Uri imageUri, ChangeImageProduct.OnImageUploadListener listener) {
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

}