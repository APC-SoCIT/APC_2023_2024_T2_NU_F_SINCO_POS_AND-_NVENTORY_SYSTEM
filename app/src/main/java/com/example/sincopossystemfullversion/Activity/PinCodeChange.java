package com.example.sincopossystemfullversion.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sincopossystemfullversion.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PinCodeChange extends AppCompatActivity {
    private DatabaseReference usersReference;
    private String selectedUserType, correctPin = "", correctUserType = "";
    private EditText etOldPin, etNewPin, etConfirmNewPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_code_change);

        usersReference = FirebaseDatabase.getInstance().getReference().child("user");
        selectedUserType = getIntent().getStringExtra("USER_TYPE");

        if (selectedUserType != null) {
            selectedUserType = selectedUserType.toLowerCase();
            fetchPinCodeFromFirebase(selectedUserType);
            Log.d("PinCodeChange", "selectedUserType in fetchPinCodeFromFirebase: " + selectedUserType);
        } else {
            Log.d("PinCodeChange", "selectedUserType in fetchPinCodeFromFirebase: " + selectedUserType);
            Toast.makeText(PinCodeChange.this, "Invalid user type", Toast.LENGTH_SHORT).show();
        }


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
        etOldPin  = findViewById(R.id.editTextOldPIN);
        etNewPin  = findViewById(R.id.editTextNewPIN);
        etConfirmNewPin  = findViewById(R.id.editTextConfirmPIN);

        etOldPin.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        etNewPin.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        etConfirmNewPin.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
    }

    private void fetchPinCodeFromFirebase(String selectedUserType) {
        DatabaseReference userRef = usersReference.child(selectedUserType);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // User found, extract user details
                    Long pinCodeLong = snapshot.child("pin_code").getValue(Long.class);
                    correctUserType = snapshot.child("user_type").getValue(String.class).toLowerCase().replace(" ", ""); // Set correctUserType in lowercase without spaces

                    Log.d("PinCodeChange", "correctUserType in fetchPinCodeFromFirebase: " + correctUserType);

                    if (pinCodeLong != null) {
                        // Use the pinCode for further login process
                        correctPin = pinCodeLong.toString();

                        Log.d("PinCodeChange", "correctPin in fetchPinCodeFromFirebase: " + correctPin);
                    } else {
                        // Handle the case where pinCode is null or not found
                        Toast.makeText(PinCodeChange.this, "PIN not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // User not found, handle accordingly
                    Toast.makeText(PinCodeChange.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PinCodeChange.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleChangePIN() {
        // Check if the user type is set
        if (selectedUserType == null || selectedUserType.isEmpty()) {
            Log.d("PinCodeChange", "correctUserType in fetchPinCodeFromFirebase: " + selectedUserType);
            Toast.makeText(PinCodeChange.this, "User type not set", Toast.LENGTH_SHORT).show();
            return;
        }

        String oldPintext = etOldPin.getText().toString();
        String newPintext = etNewPin.getText().toString();
        String confirmNewPintext = etConfirmNewPin.getText().toString();

        // Check if all fields are filled
        if (TextUtils.isEmpty(oldPintext) || TextUtils.isEmpty(newPintext) || TextUtils.isEmpty(confirmNewPintext)) {
            Toast.makeText(PinCodeChange.this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse values safely
        Long oldPin = parseLongSafely(oldPintext);
        Long newPin = parseLongSafely(newPintext);
        Long confirmNewPin = parseLongSafely(confirmNewPintext);

        // Check if parsing was successful
        if (oldPin != null && newPin != null && confirmNewPin != null) {
            // Check if oldPin matches the stored PIN in Firebase
            checkOldPin(oldPin, newPin, confirmNewPin);
        } else {
            // Handle parsing errors
            Toast.makeText(PinCodeChange.this, "Error parsing PIN values", Toast.LENGTH_SHORT).show();
        }
    }


    private Long parseLongSafely(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            Log.e("PinCodeChange", "Error parsing Long: " + value);
            return null; // Indicate parsing error with null
        }
    }

    private void checkOldPin(Long oldPin, Long newPin, Long confirmNewPin) {
        DatabaseReference userTypeRef = usersReference.child(correctUserType);

        userTypeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Long storedOldPin = dataSnapshot.child("pin_code").getValue(Long.class);

                    if (storedOldPin != null && storedOldPin.equals(oldPin)) {
                        // Old PIN matches, proceed to check new PINs
                        checkNewPins(newPin, confirmNewPin, oldPin);
                    } else {
                        Log.d("PinCodeChange", "enteredPinLong: " + oldPin);
                        Toast.makeText(PinCodeChange.this, "Incorrect old PIN", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PinCodeChange.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkNewPins(Long oldPin, Long newPin, Long confirmNewPin) {
        Log.d("PinCodeChange", "oldpin: " + oldPin);
        Log.d("PinCodeChange", "newpin: " + newPin);
        Log.d("PinCodeChange", "confirmpin: " + confirmNewPin);


        if (oldPin.equals(newPin)) {
            // Check if new PINs match and are different from the old PIN
            if (!oldPin.equals(confirmNewPin)) {
                // New PINs match and are different from the old PIN
                // Update the PIN in Firebase
                updatePinInFirebase(oldPin);
            } else {
                // New PIN matches the old PIN
                Toast.makeText(PinCodeChange.this, "New PIN should be different from the old PIN", Toast.LENGTH_SHORT).show();
            }
        } else {
            // New PINs do not match
            Toast.makeText(PinCodeChange.this, "New PINs do not match", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePinInFirebase(Long newPin) {
        DatabaseReference userTypeRef = usersReference.child(selectedUserType);
        userTypeRef.child("pin_code").setValue(newPin);

        Toast.makeText(PinCodeChange.this, "PIN changed successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

}
