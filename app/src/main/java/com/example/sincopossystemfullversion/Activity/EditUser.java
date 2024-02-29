package com.example.sincopossystemfullversion.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.util.Log;
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

public class EditUser extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private EditText etUsertype, editTextPIN;
    private Uri selectedImageUri;
    private String userKey; // Unique identifier for the user being edited

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edituser_dialog);

        // Initialize views
        imageView = findViewById(R.id.imageView);
        etUsertype = findViewById(R.id.etUsertype);
        editTextPIN = findViewById(R.id.editTextPIN);
        Button confirmButton = findViewById(R.id.confirmButton);

        // Retrieve the user's key passed from the previous activity
        userKey = getIntent().getStringExtra("userKey").replaceAll("\\s", "");
        Log.d("EditUserActivity", "Retrieved userKey: " + userKey);

        // Retrieve user data from Firebase
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("user").child(userKey);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Populate the UI with user data
                    String userType = dataSnapshot.child("user_type").getValue(String.class);
                    Long pinCode = dataSnapshot.child("pin_code").getValue(Long.class);

                    if (userType != null) {
                        etUsertype.setText(userType);
                    }
                    if (pinCode != null) {
                        editTextPIN.setText(String.valueOf(pinCode));
                    }

                    // Load the user's image into the ImageView using Glide
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

        // Set onClickListener for imageView to open gallery
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        // Set onClickListener for confirmButton to update user data
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update user data
                updateUser();
            }
        });

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
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

    private void updateUser() {
        // Retrieve updated user data
        String selectedUserType = capitalizeFirstLetter(etUsertype.getText().toString());
        String pincode = editTextPIN.getText().toString();

        // Validate pincode
        try {
            Long pincodeLong = Long.parseLong(pincode);
            // Convert selectedUserType to lowercase and remove spaces
            String key = selectedUserType.toLowerCase().replaceAll("\\s", "");

            // Upload the selected image to Firebase Storage
            uploadImageToStorage(selectedImageUri, new OnImageUploadListener() {
                @Override
                public void onSuccess(Uri imageUrl) {
                    // Image upload successful, now update user data in the database
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("user_type", selectedUserType);
                    userData.put("pin_code", pincodeLong);
                    userData.put("img_url", imageUrl.toString()); // Store the image URL in the database

                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("user").child(userKey);
                    userRef.updateChildren(userData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(EditUser.this, "User Updated Successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(EditUser.this, "Error: Failed to update user. Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            });
                }

                @Override
                public void onFailure(Exception e) {
                    // Image upload failed
                    Toast.makeText(EditUser.this, "Error: Image upload failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (NumberFormatException e) {
            // Handle invalid pincode format
            Toast.makeText(EditUser.this, "Error: Invalid PIN code format. Please enter a valid number.", Toast.LENGTH_SHORT).show();
        }
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

    private void uploadImageToStorage(Uri imageUri, OnImageUploadListener listener) {
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
