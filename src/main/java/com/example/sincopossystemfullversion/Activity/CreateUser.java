package com.example.sincopossystemfullversion.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sincopossystemfullversion.UserModel.UserModel;
import com.example.sincopossystemfullversion.R;

public class CreateUser extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imageView;
    private EditText editText;
    private Uri selectedImageUri; // Store the selected image URI here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        // Initialize views
        imageView = findViewById(R.id.imageView);
        editText = findViewById(R.id.editText);

        // Back button click listener
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // Image view click listener to pick an image from the gallery
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        // Confirm button click listener
        Button confirmButton = findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if text is written and an image is selected
                if (!editText.getText().toString().isEmpty() && selectedImageUri != null) {
                    // Add user and return to TypeofUser activity
                    addUserAndReturn();
                } else {
                    // Handle case where no text or image is selected
                    // You may want to show a toast or other feedback to the user
                    Toast.makeText(CreateUser.this, "Please enter text and select an image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Method to open the gallery for image selection
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    // Handle the result after picking an image from the gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Set the selected image URI
            selectedImageUri = data.getData();
            // Set the selected image to the ImageView
            imageView.setImageURI(selectedImageUri);
        }
    }

    private void addUserAndReturn() {
        // Create UserModel with the entered text and selected image
        UserModel userModel = new UserModel(selectedImageUri, editText.getText().toString());

        // Return to TypeofUser activity with the UserModel
        moveToTypeOfUserActivity(userModel);
    }

    private void moveToTypeOfUserActivity(UserModel userModel) {
        Intent intent = new Intent(this, TypeofUser.class);
        intent.putExtra("userModel", userModel);
        startActivity(intent);
        finish();
    }
}