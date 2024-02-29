package com.example.sincopossystemfullversion.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sincopossystemfullversion.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LockedPINChange extends AppCompatActivity {
    private DatabaseReference usersReference;
    private String selectedUserType, correctPin = "", correctUserType = "";
    private EditText etOldPin, etNewPin, etConfirmNewPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locked_pinchange);

        usersReference = FirebaseDatabase.getInstance().getReference().child("user");
        selectedUserType = getIntent().getStringExtra("USER_TYPE");

        if (selectedUserType != null) {
            selectedUserType = selectedUserType.toLowerCase();
            fetchPinCodeFromFirebase(selectedUserType);
            Log.d("PinCodeChange", "selectedUserType in fetchPinCodeFromFirebase: " + selectedUserType);
        } else {
            Log.d("PinCodeChange", "selectedUserType in fetchPinCodeFromFirebase: " + selectedUserType);
            Toast.makeText(LockedPINChange.this, "Invalid user type", Toast.LENGTH_SHORT).show();
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
        etNewPin  = findViewById(R.id.editTextNewPIN);
        etConfirmNewPin  = findViewById(R.id.editTextConfirmPIN);

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
                        Toast.makeText(LockedPINChange.this, "PIN not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // User not found, handle accordingly
                    Toast.makeText(LockedPINChange.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LockedPINChange.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleChangePIN() {
        // Check if the user type is set
        if (selectedUserType == null || selectedUserType.isEmpty()) {
            Log.d("PinCodeChange", "correctUserType in fetchPinCodeFromFirebase: " + selectedUserType);
            Toast.makeText(LockedPINChange.this, "User type not set", Toast.LENGTH_SHORT).show();
            return;
        }

        String newPintext = etNewPin.getText().toString();
        String confirmNewPintext = etConfirmNewPin.getText().toString();

        // Check if all fields are filled
        if (TextUtils.isEmpty(newPintext) || TextUtils.isEmpty(confirmNewPintext)) {
            Toast.makeText(LockedPINChange.this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse values safely
        Long newPin = parseLongSafely(newPintext);
        Long confirmNewPin = parseLongSafely(confirmNewPintext);

        // Check if parsing was successful
        if (newPin != null && confirmNewPin != null) {
            // Check if oldPin matches the stored PIN in Firebase
            checkOldPin(newPin, confirmNewPin);
        } else {
            // Handle parsing errors
            Toast.makeText(LockedPINChange.this, "Error parsing PIN values", Toast.LENGTH_SHORT).show();
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

    private void checkOldPin(Long newPin, Long confirmNewPin) {
        DatabaseReference userTypeRef = usersReference.child(correctUserType);

        userTypeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Long storedOldPin = dataSnapshot.child("pin_code").getValue(Long.class);

                    if (storedOldPin != null) {
                        // Old PIN matches, proceed to check new PINs
                        checkNewPins(newPin, confirmNewPin);
                    } else {
                        Toast.makeText(LockedPINChange.this, "Incorrect old PIN", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LockedPINChange.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkNewPins(Long newPin, Long confirmNewPin) {
        Log.d("PinCodeChange", "newpin: " + newPin);
        Log.d("PinCodeChange", "confirmpin: " + confirmNewPin);

        if (newPin.equals(confirmNewPin)) {
                updatePinInFirebase(newPin);
        } else {
            // New PINs do not match
            Toast.makeText(LockedPINChange.this, "New PINs do not match", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePinInFirebase(Long newPin) {
        DatabaseReference userTypeRef = usersReference.child(selectedUserType);
        userTypeRef.child("pin_code").setValue(newPin);

        Toast.makeText(LockedPINChange.this, "PIN changed successfully", Toast.LENGTH_SHORT).show();

        // Create intent to navigate to TypeofUser.class
        Intent intent = new Intent(LockedPINChange.this, TypeofUser.class);
        startActivity(intent);

        finish(); // Finish the current activity
    }


}
