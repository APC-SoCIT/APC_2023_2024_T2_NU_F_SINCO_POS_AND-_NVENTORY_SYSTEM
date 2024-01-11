package com.example.sincopossystemfullversion.Activity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sincopossystemfullversion.R;
import com.example.sincopossystemfullversion.UserModel.UserModel;

public class TypeofUser extends AppCompatActivity {

    private LinearLayout userContainer;
    private static final int CREATE_USER_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_typeof_user);

        // Get references to the ImageViews
        ImageView adminImageView = findViewById(R.id.adminimage);
        ImageView cashierImageView = findViewById(R.id.cashierimage);

        // Set click listeners for the ImageViews
        adminImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle click for admin image
                openCreateUser("admin");
            }
        });

        cashierImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle click for cashier image
                openCreateUser("cashier");
            }
        });


        // Set up scroll animation
        final HorizontalScrollView horizontalScrollView = findViewById(R.id.horizontalScrollView);
        userContainer = findViewById(R.id.userContainer);

        horizontalScrollView.getViewTreeObserver().addOnScrollChangedListener(
                new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        int scrollX = horizontalScrollView.getScrollX();
                        animateCardViewOnScroll(userContainer.getChildAt(0), scrollX, 0);
                        animateCardViewOnScroll(userContainer.getChildAt(1), scrollX, 1);
                        animateCardViewOnScroll(userContainer.getChildAt(2), scrollX, 2);
                        animateCardViewOnScroll(userContainer.getChildAt(3), scrollX, 3);
                    }
                }
        );

        // Check if there is a UserModel passed from CreateUser
        UserModel usermodel = getIntent().getParcelableExtra("userModel");
        if (usermodel != null) {
            // Add a new CardView to userContainer
            addUserCardView(usermodel);
        }
    }

    private void openCreateUser(String userType) {
        // Start the CreateUser activity with the userType
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("userType", userType);
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

    private void addUserCardView(UserModel userModel) {
        // Inflate the user_card_view layout
        View cardView = getLayoutInflater().inflate(R.layout.user_card_view, null);

        // Set the image and text to the CardView
        ImageView cardImageView = cardView.findViewById(R.id.cardImageView);
        TextView cardPositionTextView = cardView.findViewById(R.id.cardPositionTextView);

        cardImageView.setImageURI(userModel.getImageUri());
        cardPositionTextView.setText(userModel.getText());

        // Set an OnClickListener on the CardView
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click on the CardView, navigate to the login activity
                Intent intent = new Intent(TypeofUser.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // Add the CardView to userContainer
        userContainer.addView(cardView);
    }

    private void animateCardViewOnScroll(View view, int scrollX, int position) {
        int viewX = view.getLeft();
        int startScroll = position * view.getWidth();
        int endScroll = (position + 1) * view.getWidth();
        float alpha = 1.0f;

        if (scrollX > startScroll && scrollX < endScroll) {
            alpha = 1.0f - (float) (scrollX - startScroll) / view.getWidth();
        }

        // Animate the alpha property
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, alpha);
        alphaAnimator.setDuration(0);
        alphaAnimator.start();
    }
}
