package com.example.sincopossystemfullversion.Activity;

import static androidx.core.content.ContentProviderCompat.requireContext;

import static com.example.sincopossystemfullversion.Adapter.HomeLSNew.PERMISSION_REQUEST_CODE;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincopossystemfullversion.Adapter.HomeLSNew;
import com.example.sincopossystemfullversion.Adapter.ProductAdapter;
import com.example.sincopossystemfullversion.Adapter.ProductNew;
import com.example.sincopossystemfullversion.Adapter.SalesReportNew;
import com.example.sincopossystemfullversion.Adapter.UserTypeAdapter;
import com.example.sincopossystemfullversion.SalesReportModel;
import com.example.sincopossystemfullversion.UsersModel;
import com.example.sincopossystemfullversion.R;
import com.example.sincopossystemfullversion.UserModel.UserModel;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TypeofUser extends AppCompatActivity {

    private static final int CREATE_USER_REQUEST_CODE = 1;
    private static final int PERMISSION_REQUEST_CODE = 22;
    private static final int NOTIFICATION_ID = 123;
    private static final String CHANNEL_ID = "low_stock_notification_channel";

    private DatabaseReference usersReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_typeof_user);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission(); // Request permission again
            }
        }

        createNotificationChannel();

        usersReference = FirebaseDatabase.getInstance().getReference().child("user");

        RecyclerView recyclerView = findViewById(R.id.usertypeRV);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(gridLayoutManager);

        FirebaseRecyclerOptions<UsersModel> options =
                new FirebaseRecyclerOptions.Builder<UsersModel>()
                        .setQuery(usersReference, UsersModel.class)
                        .build();

        UserTypeAdapter adapter = new UserTypeAdapter(options, this);
        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    private void openCreateUser(String userType) {
        // Start the CreateUser activity with the userType
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("USER_TYPE", userType);
        startActivityForResult(intent, CREATE_USER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_USER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // Handle the updated user type from CreateUser activity
            String updatedUserType = data.getStringExtra("updatedUserType");

            // Update the user type as needed (e.g., add a new CardView)
            if (updatedUserType != null) {
                openCreateUser(updatedUserType);
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    // Permission result handling
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {

                } else {
                    // Permission denied, request again after restarting app
                    Toast.makeText(this, "Notification permission denied.", Toast.LENGTH_SHORT).show();
                }
            });


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


}

