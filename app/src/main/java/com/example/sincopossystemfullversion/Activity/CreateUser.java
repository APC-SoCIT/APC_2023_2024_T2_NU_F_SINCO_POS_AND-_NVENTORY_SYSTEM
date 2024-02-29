package com.example.sincopossystemfullversion.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sincopossystemfullversion.Adapter.InventoryNew;
import com.example.sincopossystemfullversion.Adapter.UserTypeAdapter;
import com.example.sincopossystemfullversion.Adapter.UsersAdapter;
import com.example.sincopossystemfullversion.InventoryModel;
import com.example.sincopossystemfullversion.UserModel.UserModel;
import com.example.sincopossystemfullversion.R;
import com.example.sincopossystemfullversion.UsersModel;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class CreateUser extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    UsersAdapter adapter;
    RecyclerView recyclerView;
    EditText edtTextAdmin;
    ImageView imageView8, addUser, editIcon, okIcon;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        edtTextAdmin = findViewById(R.id.edtTextAdmin);
        imageView8 = findViewById(R.id.imageView8);


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("user").child("admin");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String email = dataSnapshot.child("email_add").getValue(String.class);
                    String imageUrl = dataSnapshot.child("img_url").getValue(String.class); // Assuming "image_url" is the key for the image URL
                    if (email != null) {
                        edtTextAdmin.setText(email);
                    }

                    if (imageUrl != null) {
                        Glide.with(getApplicationContext())
                                .load(imageUrl)
                                .into(imageView8);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled event
            }
        });

        imageView8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ChangeImageAdmin.class);
                startActivity(intent);
            }
        });

        editIcon = findViewById(R.id.editIcon);
        okIcon = findViewById(R.id.okIcon);
        editIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When the editIcon is clicked
                edtTextAdmin.setEnabled(true); // Enable editing the text

                // Hide the editIcon and show the okIcon
                editIcon.setVisibility(View.GONE);
                okIcon.setVisibility(View.VISIBLE);
            }
        });

        okIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When the okIcon is clicked
                //edtTextAdmin.setEnabled(false); // Disable editing the text

                // Hide the okIcon and show the editIcon
                okIcon.setVisibility(View.GONE);
                editIcon.setVisibility(View.VISIBLE);

                // Show a confirmation dialog
                showConfirmationDialog();
            }
        });


        addUser = findViewById(R.id.addUser);
        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreateNewUser.class);
                startActivity(intent);
            }
        });

        recyclerView = findViewById(R.id.usersRV);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        FirebaseRecyclerOptions<UsersModel> options =
                new FirebaseRecyclerOptions.Builder<UsersModel>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("user"), UsersModel.class)
                        .build();

        adapter = new UsersAdapter(options, this); // Create adapter instance
        recyclerView.setAdapter(adapter);
        adapter.startListening();


        // Back button click listener
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

    // Method to show the confirmation dialog
    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Email Update");
        builder.setMessage("Are you sure you want to update the admin's email?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Update the admin's email
                updateAdminEmail();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked "No", do nothing
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Method to update the admin's email in Firebase
    private void updateAdminEmail() {
        String newEmail = edtTextAdmin.getText().toString().trim();
        if (isValidEmail(newEmail)) {
            DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference().child("user").child("admin");
            adminRef.child("email_add").setValue(newEmail)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            edtTextAdmin.setEnabled(false);
                            Toast.makeText(CreateUser.this, "Admin Email Updated Successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CreateUser.this, "Error: Failed to update admin email. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(CreateUser.this, "Error: Invalid email format.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to validate email format
    private boolean isValidEmail(String email) {
        // Use a simple regex pattern for email validation
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }
}