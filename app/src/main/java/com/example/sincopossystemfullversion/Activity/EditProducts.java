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

import com.example.sincopossystemfullversion.Fragments.EditProductsFragment;
import com.example.sincopossystemfullversion.R;
import com.example.sincopossystemfullversion.UserModel.UserModel;

public class EditProducts extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editproducts);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new EditProductsFragment())
                .commit();

    }

}