package com.example.sincopossystemfullversion.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sincopossystemfullversion.R;

public class LoginActivity extends AppCompatActivity {

    private static final int MAX_TRIES = 3;
    private int remainingTries = MAX_TRIES;
    private StringBuilder enteredPin = new StringBuilder();
    private String correctPin = "1234"; // Replace with your actual correct pin
    private static final int REQUEST_CHANGE_PIN = 1;

    private Button btnChangeUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
                Toast.makeText(LoginActivity.this, "Forgot Password clicked", Toast.LENGTH_SHORT).show();
                // You can implement the logic to handle the forgot password action, such as showing a dialog or navigating to a forgot password activity.
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

    private void handlePinInput(String input) {
        if (enteredPin.length() < 4) {
            enteredPin.append(input);
            updateCircleViews();
        }

        if (enteredPin.length() == 4) {
            // Use the latest correct PIN for comparison
            String latestCorrectPin = getSharedPreferences("PIN_PREFS", MODE_PRIVATE).getString("PIN", null);

            if (latestCorrectPin != null && enteredPin.toString().equals(latestCorrectPin)) {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                resetPinAndTries();
                startNewActivity();
            } else {
                remainingTries--;
                if (remainingTries > 0) {
                    Toast.makeText(this, "Incorrect Pin. Tries remaining: " + remainingTries, Toast.LENGTH_SHORT).show();
                    clearPin();
                } else {
                    Toast.makeText(this, "No more tries. Account locked.", Toast.LENGTH_SHORT).show();
                }
            }
        }
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

    private void startNewActivity() {
        Intent intent = new Intent(this, MainActivity.class);
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
