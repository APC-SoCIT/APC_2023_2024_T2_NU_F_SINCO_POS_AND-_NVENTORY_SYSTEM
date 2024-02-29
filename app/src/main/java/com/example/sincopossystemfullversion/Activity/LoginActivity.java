package com.example.sincopossystemfullversion.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sincopossystemfullversion.R;
import com.example.sincopossystemfullversion.UsersModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {

    private static final String SMTP_SERVER = "smtp.gmail.com";
    private static final String SMTP_PORT = "587"; // Port for TLS
    private static final String EMAIL = "";
    private static final String PASSWORD = "";
    private static final int MAX_TRIES = 3;
    private int remainingTries = MAX_TRIES;
    private StringBuilder enteredPin = new StringBuilder();
    private static final String PREF_NAME = "user_prefs";
    private String correctPin = ""; // Replace with your actual correct pin
    private static final int REQUEST_CHANGE_PIN = 1;

    private Button btnChangeUser;
    private DatabaseReference usersReference;
    private SharedPreferences sharedPreferences;
    private String correctUserType = "", selectedUserType; // Added to store selected userType

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        usersReference = FirebaseDatabase.getInstance().getReference().child("user");

        selectedUserType = getIntent().getStringExtra("USER_TYPE");

        if (selectedUserType != null) {
            selectedUserType = selectedUserType.toLowerCase();
            fetchPinCodeFromFirebase(selectedUserType);
        } else {
            Toast.makeText(LoginActivity.this, "Invalid user type", Toast.LENGTH_SHORT).show();
        }


        Button btnClear = findViewById(R.id.btnClear);
        ImageButton btnDelete = findViewById(R.id.btnDelete);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnChangeUser = findViewById(R.id.btnchangeuser);

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearPin();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteLastDigit();
            }
        });

        Button btn1 = findViewById(R.id.pin1btn);
        Button btn2 = findViewById(R.id.pin2btn);
        Button btn3 = findViewById(R.id.pin3btn);
        Button btn4 = findViewById(R.id.pin4btn);
        Button btn5 = findViewById(R.id.pin5btn);
        Button btn6 = findViewById(R.id.pin6btn);
        Button btn7 = findViewById(R.id.pin7btn);
        Button btn8 = findViewById(R.id.pin8btn);
        Button btn9 = findViewById(R.id.pin9btn);
        Button btn0 = findViewById(R.id.pin0btn);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePinInput("1");
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePinInput("2");
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePinInput("3");
            }
        });

        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePinInput("4");
            }
        });

        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePinInput("5");
            }
        });

        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePinInput("6");
            }
        });

        btn7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePinInput("7");
            }
        });

        btn8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePinInput("8");
            }
        });

        btn9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePinInput("9");
            }
        });

        btn0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePinInput("0");
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedUserType != null && selectedUserType.equals("admin")) {
                    forgotPasswordAdmin();
                } else {
                    Toast.makeText(LoginActivity.this, "Forgot Password clicked", Toast.LENGTH_SHORT).show();
                    forgotPassword();
                }

            }
        });

        btnChangeUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Change User clicked", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(LoginActivity.this, TypeofUser.class);
                startActivityForResult(intent, REQUEST_CHANGE_PIN);
            }
        });
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

                    Log.d("LoginActivity", "correctUserType in fetchPinCodeFromFirebase: " + correctUserType);

                    if (pinCodeLong != null) {
                        String pinCode = String.valueOf(pinCodeLong);
                        // Use the pinCode for further login process
                        correctPin = pinCode;

                        Log.d("LoginActivity", "correctPin in fetchPinCodeFromFirebase: " + correctPin);
                    } else {
                        // Handle the case where pinCode is null or not found
                        Toast.makeText(LoginActivity.this, "PIN not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // User not found, handle accordingly
                    Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void handlePinInput(String input) {
        if (enteredPin.length() < 4) {
            enteredPin.append(input);
            updateCircleViews();
        }

        if (enteredPin.length() == 4) {
            DatabaseReference userTypeRef = usersReference.child(correctUserType);
            userTypeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Long pinFromFirebase = dataSnapshot.child("pin_code").getValue(Long.class);
                    long enteredPinLong = Long.parseLong(enteredPin.toString());

                    if (dataSnapshot.exists()) {
                        if (pinFromFirebase != null && pinFromFirebase.equals(enteredPinLong)) {
                            // Correct PIN, login successful
                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            resetPinAndTries();
                            startNewActivity(correctUserType);
                        } else {
                            // Incorrect PIN
                            remainingTries--;
                            if (remainingTries <= 0) {
                                // Lock the account or implement additional measures for too many failed attempts
                                disableInputAndPromptForAdminPIN();
                            } else {
                                // Display remaining tries or other appropriate message
                                Toast.makeText(LoginActivity.this, "Incorrect PIN. Tries left: " + remainingTries, Toast.LENGTH_SHORT).show();
                                clearPin();
                            }
                        }
                    } else {
                        // User not found in Firebase
                        Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                        // Handle this case as needed
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle error
                    Toast.makeText(LoginActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void disableInputAndPromptForAdminPIN() {
        if (correctUserType.equals("admin")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            LayoutInflater inflater = LayoutInflater.from(LoginActivity.this);
            View view = inflater.inflate(R.layout.admin_send_code, null);
            EditText adminPinEditText = view.findViewById(R.id.adminPinEditText);
            Button submitButton = view.findViewById(R.id.submitButton);

            builder.setView(view);

            AlertDialog dialog = builder.create();
            dialog.show();

            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseReference adminRef = usersReference.child("admin"); // Reference to the admin user
                    adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String adminEmail = dataSnapshot.child("email_add").getValue(String.class);
                                String adminText = adminPinEditText.getText().toString();

                                if (adminEmail != null && adminText.equals(adminEmail)) { // Corrected string comparison
                                    // Call the sendEmail method with admin email and pin code
                                    Intent intent = new Intent(LoginActivity.this, LockedPINChange.class);
                                    intent.putExtra("USER_TYPE", selectedUserType); // Pass the selected user type to the next activity
                                    startActivityForResult(intent, REQUEST_CHANGE_PIN);
                                    dialog.dismiss(); // Dismiss the dialog after sending the email
                                } else {
                                    Toast.makeText(LoginActivity.this, "Admin email or PIN code not found", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, "Admin data not found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle error
                            Toast.makeText(LoginActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });


        } else {
            // Open the default dialog for other user types
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            LayoutInflater inflater = LayoutInflater.from(LoginActivity.this);
            View view = inflater.inflate(R.layout.admin_pin_dialog, null);
            EditText adminPinEditText = view.findViewById(R.id.adminPinEditText);
            Button submitButton = view.findViewById(R.id.submitButton);

            adminPinEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

            builder.setView(view);

            AlertDialog dialog = builder.create();
            dialog.show();

            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Check if Admin's PIN is correct
                    String adminPin = adminPinEditText.getText().toString().trim(); // Get the entered PIN
                    String selectedUserType = getIntent().getStringExtra("USER_TYPE");

                    DatabaseReference adminRef = usersReference.child("admin"); // Reference to the admin user
                    adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Long adminCorrectPin = dataSnapshot.child("pin_code").getValue(Long.class); // Retrieve the admin's correct PIN from Firebase

                                if (adminCorrectPin != null && adminPin.equals(String.valueOf(adminCorrectPin))) {
                                    // Correct Admin's PIN, navigate to PIN change activity
                                    Intent intent = new Intent(LoginActivity.this, LockedPINChange.class);
                                    intent.putExtra("USER_TYPE", selectedUserType); // Pass the selected user type to the next activity
                                    startActivityForResult(intent, REQUEST_CHANGE_PIN);
                                    dialog.dismiss(); // Dismiss the dialog
                                } else {
                                    // Incorrect Admin's PIN, display error message
                                    Toast.makeText(LoginActivity.this, "Incorrect Admin's PIN", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Admin data not found
                                Toast.makeText(LoginActivity.this, "Admin data not found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle error
                            Toast.makeText(LoginActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    public static void sendEmail(String adminEmail, Long pinCode) {
        // Set up the email template with the PIN code
        String emailContent = "Hello,\n\n" +
                "Here is your PIN code: " + pinCode + "\n\n" +
                "If you didn’t request this PIN code, you can ignore this email.\n\n" +
                "Thanks,\n" +
                "Your Sinco team";

        // Send the email using Firebase Authentication's sendPasswordResetEmail method
        FirebaseAuth.getInstance().sendPasswordResetEmail(adminEmail)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Password reset email sent successfully
                            Log.d("LoginActivity", "Email sent successfully!" + adminEmail);
                        } else {
                            // Failed to send password reset email
                            Log.e("LoginActivity", "Failed to send email: " + task.getException().getMessage());
                        }
                    }
                });
    }


    private void forgotPassword() {

        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        LayoutInflater inflater = LayoutInflater.from(LoginActivity.this);
        View view = inflater.inflate(R.layout.forgot_pass_dialog, null);
        EditText adminPinEditText = view.findViewById(R.id.adminPinEditText);
        Button submitButton = view.findViewById(R.id.submitButton);

        adminPinEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if Admin's PIN is correct
                String adminPin = adminPinEditText.getText().toString().trim(); // Get the entered PIN
                String selectedUserType = getIntent().getStringExtra("USER_TYPE");

                DatabaseReference adminRef = usersReference.child("admin"); // Reference to the admin user
                adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Long adminCorrectPin = dataSnapshot.child("pin_code").getValue(Long.class); // Retrieve the admin's correct PIN from Firebase

                            if (adminCorrectPin != null && adminPin.equals(String.valueOf(adminCorrectPin))) {
                                // Correct Admin's PIN, navigate to PIN change activity
                                Intent intent = new Intent(LoginActivity.this, LockedPINChange.class);
                                intent.putExtra("USER_TYPE", selectedUserType); // Pass the selected user type to the next activity
                                startActivityForResult(intent, REQUEST_CHANGE_PIN);
                                dialog.dismiss(); // Dismiss the dialog
                            } else {
                                // Incorrect Admin's PIN, display error message
                                Toast.makeText(LoginActivity.this, "Incorrect Admin's PIN", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Admin data not found
                            Toast.makeText(LoginActivity.this, "Admin data not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                        Toast.makeText(LoginActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }
    private void forgotPasswordAdmin() {

        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        LayoutInflater inflater = LayoutInflater.from(LoginActivity.this);
        View view = inflater.inflate(R.layout.admin_send_code, null);
        EditText adminPinEditText = view.findViewById(R.id.adminPinEditText);
        Button submitButton = view.findViewById(R.id.submitButton);

        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference adminRef = usersReference.child("admin"); // Reference to the admin user
                adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String adminEmail = dataSnapshot.child("email_add").getValue(String.class);
                            String adminText = adminPinEditText.getText().toString();

                            if (adminEmail != null && adminText.equals(adminEmail)) { // Corrected string comparison
                                // Call the sendEmail method with admin email and pin code
                                Intent intent = new Intent(LoginActivity.this, LockedPINChange.class);
                                intent.putExtra("USER_TYPE", selectedUserType); // Pass the selected user type to the next activity
                                startActivityForResult(intent, REQUEST_CHANGE_PIN);
                                dialog.dismiss(); // Dismiss the dialog after sending the email
                            } else {
                                Toast.makeText(LoginActivity.this, "Admin email or PIN code not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Admin data not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                        Toast.makeText(LoginActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    private void clearPin() {
        enteredPin.setLength(0);
        updateCircleViews();
    }

    private void deleteLastDigit() {
        if (enteredPin.length() > 0) {
            enteredPin.deleteCharAt(enteredPin.length() - 1);
            updateCircleViews();
        }
    }

    private void updateCircleViews() {
        View circle1 = findViewById(R.id.circle1);
        View circle2 = findViewById(R.id.circle2);
        View circle3 = findViewById(R.id.circle3);
        View circle4 = findViewById(R.id.circle4);

        switch (enteredPin.length()) {
            case 0:
                circle1.setBackgroundResource(R.drawable.circle_pin_background);
                circle2.setBackgroundResource(R.drawable.circle_pin_background);
                circle3.setBackgroundResource(R.drawable.circle_pin_background);
                circle4.setBackgroundResource(R.drawable.circle_pin_background);
                break;
            case 1:
                circle1.setBackgroundResource(R.drawable.circle_pin_background_white);
                circle2.setBackgroundResource(R.drawable.circle_pin_background);
                circle3.setBackgroundResource(R.drawable.circle_pin_background);
                circle4.setBackgroundResource(R.drawable.circle_pin_background);
                break;
            case 2:
                circle1.setBackgroundResource(R.drawable.circle_pin_background_white);
                circle2.setBackgroundResource(R.drawable.circle_pin_background_white);
                circle3.setBackgroundResource(R.drawable.circle_pin_background);
                circle4.setBackgroundResource(R.drawable.circle_pin_background);
                break;
            case 3:
                circle1.setBackgroundResource(R.drawable.circle_pin_background_white);
                circle2.setBackgroundResource(R.drawable.circle_pin_background_white);
                circle3.setBackgroundResource(R.drawable.circle_pin_background_white);
                circle4.setBackgroundResource(R.drawable.circle_pin_background);
                break;
            case 4:
                circle1.setBackgroundResource(R.drawable.circle_pin_background_white);
                circle2.setBackgroundResource(R.drawable.circle_pin_background_white);
                circle3.setBackgroundResource(R.drawable.circle_pin_background_white);
                circle4.setBackgroundResource(R.drawable.circle_pin_background_white);
                break;
            default:
                break;
        }
    }

    private void resetPinAndTries() {
        enteredPin.setLength(0);
        remainingTries = MAX_TRIES;
        updateCircleViews();
    }

    private void startNewActivity(String selectedUserType) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("USER_TYPE", selectedUserType);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CHANGE_PIN && resultCode == RESULT_OK) {
            // Handle PIN change result
            if (data != null && data.hasExtra("NEW_PIN")) {
                String newPIN = data.getStringExtra("NEW_PIN");
                // Update the correctPin with the new PIN
                correctPin = newPIN;
                Toast.makeText(this, "PIN Changed Successfully", Toast.LENGTH_SHORT).show();
            }
        }
    }

}