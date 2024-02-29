package com.example.sincopossystemfullversion.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sincopossystemfullversion.Adapter.UsersAdapter;
import com.example.sincopossystemfullversion.R;
import com.example.sincopossystemfullversion.UserModel.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChangeImageAdmin extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    ImageView imageView;
    private Uri selectedImageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_adminpic);

        imageView = findViewById(R.id.imageView);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("user").child("admin");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    String imageUrl = dataSnapshot.child("img_url").getValue(String.class);
                    if (imageUrl != null) {
                        Glide.with(getApplicationContext())
                                .load(imageUrl)
                                .into(imageView);
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
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
                // Validate if an image is selected
                if (selectedImageUri != null) {
                    // Upload the selected image to Firebase Storage
                    uploadImageToStorage(selectedImageUri, new OnImageUploadListener() {
                        @Override
                        public void onSuccess(Uri imageUrl) {
                            // Image upload successful, update admin image URL in the database
                            DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference().child("user").child("admin");
                            adminRef.child("img_url").setValue(imageUrl.toString())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            // Reload the updated image immediately
                                            Glide.with(getApplicationContext())
                                                    .load(imageUrl)
                                                    .into(imageView);

                                            Toast.makeText(ChangeImageAdmin.this, "Admin Image Updated Successfully", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(ChangeImageAdmin.this, "Error: Failed to update admin image. Please try again.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Image upload failed
                            Toast.makeText(ChangeImageAdmin.this, "Error: Image upload failed. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // No image selected, display a message
                    Toast.makeText(ChangeImageAdmin.this, "Error: Please select an image.", Toast.LENGTH_SHORT).show();
                }
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

    private void uploadImageToStorage(Uri imageUri, ChangeImageAdmin.OnImageUploadListener listener) {
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