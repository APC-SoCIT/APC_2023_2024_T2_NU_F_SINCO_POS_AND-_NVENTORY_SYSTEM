package com.example.sincopossystemfullversion.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sincopossystemfullversion.Adapter.UsersAdapter;
import com.example.sincopossystemfullversion.R;
import com.example.sincopossystemfullversion.UserModel.UserModel;
import com.example.sincopossystemfullversion.UsersModel;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

public class CreateNewUser extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    UsersAdapter adapter;
    RecyclerView recyclerView;
    EditText edtTextAdmin;
    ImageView imageView, addUser;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createuser_dialog);

        // Back button click listener
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        imageView = findViewById(R.id.imageView);
        EditText etUsertype = findViewById(R.id.etUsertype);
        EditText editTextPIN = findViewById(R.id.editTextPIN);
        EditText editTextConfirmPIN = findViewById(R.id.editTextConfirmPIN);
        Button confirmButton = findViewById(R.id.confirmButton);

        editTextPIN.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        editTextConfirmPIN.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedUserType = capitalizeFirstLetter(etUsertype.getText().toString());
                String pincode = editTextPIN.getText().toString();
                String pincodeconf = editTextConfirmPIN.getText().toString();

                if (!pincode.equals(pincodeconf)) {
                    // Display a Toast message when PIN codes don't match
                    Toast.makeText(CreateNewUser.this, "PIN codes do not match. Please check again.", Toast.LENGTH_SHORT).show();
                    return; // Exit the onClick method if there is an error
                }

                try {
                    Long pincodeLong = Long.parseLong(pincode);

                    // Convert selectedUserType to lowercase and remove spaces
                    String key = selectedUserType.toLowerCase().replaceAll("\\s", "");

                    // Upload the selected image to Firebase Storage
                    uploadImageToStorage(selectedImageUri, new OnImageUploadListener() {
                        @Override
                        public void onSuccess(Uri imageUrl) {
                            // Image upload successful, now add user data to the database

                            Map<Object, Object> map = new HashMap<>();
                            map.put("user_type", selectedUserType);
                            map.put("pin_code", pincodeLong);
                            map.put("img_url", imageUrl.toString()); // Store the image URL in the database

                            FirebaseDatabase.getInstance().getReference().child("user").child(key)
                                    .setValue(map)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(CreateNewUser.this, "New User Added Successfully", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(Exception e) {
                                            Toast.makeText(CreateNewUser.this, "Error: Cannot add new user. Please try again.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Image upload failed
                            Toast.makeText(CreateNewUser.this, "Error: Image upload failed. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (NumberFormatException e) {
                    // Handle the case where pincode is not a valid Long
                    Toast.makeText(CreateNewUser.this, "Error: Invalid PIN code format. Please enter a valid number.", Toast.LENGTH_SHORT).show();
                }
            }
        });

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

    private void moveToTypeOfUserActivity(UserModel userModel) {
        Intent intent = new Intent(this, TypeofUser.class);
        intent.putExtra("userModel", userModel);
        startActivity(intent);
        finish();
    }
}