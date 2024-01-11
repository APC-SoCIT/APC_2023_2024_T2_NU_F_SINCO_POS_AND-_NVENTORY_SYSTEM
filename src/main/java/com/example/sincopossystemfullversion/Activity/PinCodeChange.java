package com.example.sincopossystemfullversion.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sincopossystemfullversion.R;

public class PinCodeChange extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_code_change);

        View backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Button changeButton = findViewById(R.id.buttonChangePIN);
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleChangePIN();
            }
        });

        // Apply InputFilter to restrict EditText to 4 digits
        EditText oldPIN = findViewById(R.id.editTextOldPIN);
        EditText newPIN = findViewById(R.id.editTextNewPIN);
        EditText confirmPIN = findViewById(R.id.editTextConfirmPIN);

        oldPIN.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        newPIN.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        confirmPIN.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
    }

    private void handleChangePIN() {
        EditText oldPIN = findViewById(R.id.editTextOldPIN);
        EditText newPIN = findViewById(R.id.editTextNewPIN);
        EditText confirmPIN = findViewById(R.id.editTextConfirmPIN);
        TextView messageTextView = findViewById(R.id.textViewMessage);

        String oldPINText = oldPIN.getText().toString();
        String newPINText = newPIN.getText().toString();
        String confirmPINText = confirmPIN.getText().toString();

        // Check if the oldPIN, newPIN, and confirmPIN are not empty
        if (!oldPINText.isEmpty() && !newPINText.isEmpty() && !confirmPINText.isEmpty()) {
            // Check if the length of oldPIN and newPIN is exactly 4
            if (oldPINText.length() == 4 && newPINText.length() == 4) {
                // Check if newPIN and confirmPIN match
                if (newPINText.equals(confirmPINText)) {
                    // Perform your PIN change logic here
                    // and handle the success or failure accordingly
                    if (changePINSuccess(newPINText)) {
                        // PIN change successful
                        messageTextView.setText("PIN Change Successful");
                        messageTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        // Set the result and finish the PinCodeChange activity
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("NEW_PIN", newPINText);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        // PIN change failed
                        messageTextView.setText("PIN Change Failed. Please try again.");
                        messageTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    }
                } else {
                    // PINs do not match
                    messageTextView.setText("New PIN and Confirm PIN do not match.");
                    messageTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }
            } else {
                // Old PIN and New PIN must be exactly 4 digits
                messageTextView.setText("Old PIN and New PIN must be exactly 4 digits.");
                messageTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        } else {
            // All fields are required
            messageTextView.setText("All fields are required.");
            messageTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private boolean changePINSuccess(String newPIN) {
        SharedPreferences preferences = getSharedPreferences("PIN_PREFS", MODE_PRIVATE);
        String oldPIN = preferences.getString("PIN", null);

        // Check if there is an old PIN
        if (oldPIN != null) {
            // Remove the old PIN before saving the new one
            preferences.edit().remove("PIN").apply();
        }

        // Save the new PIN
        preferences.edit().putString("PIN", newPIN).apply();

        return true;
    }
}
