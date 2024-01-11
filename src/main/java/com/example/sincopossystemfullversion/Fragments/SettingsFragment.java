package com.example.sincopossystemfullversion.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

//import com.example.sincopossystemfullversion.Activity.CreateUser;
//import com.example.sincopossystemfullversion.Activity.PinCodeChange;
import com.example.sincopossystemfullversion.Activity.CreateUser;
import com.example.sincopossystemfullversion.Activity.EditProducts;
import com.example.sincopossystemfullversion.Activity.PinCodeChange;
import com.example.sincopossystemfullversion.Adapter.EditProductAdapter;
import com.example.sincopossystemfullversion.R;
import com.example.sincopossystemfullversion.SharedViewModel.SharedViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imgProduct;
    private Uri selectedImageUri;
    private SharedViewModel sharedViewModel;
    BottomSheetDialog bottomSheetDialog;
    EditText edtProductName, edtProductDescription, edtProductPrice, edtProductType, edtProductImg;
    Button btnAddProduct;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Assuming cardView is the ID of your CardView in fragment_settings.xml
        View cardView = view.findViewById(R.id.SettingsAddProduct);
        View cardView1= view.findViewById(R.id.SettingsCreateUser);
        View cardView2= view.findViewById(R.id.SettingsChangePinCode);
        View cardView3= view.findViewById(R.id.SettingsEditProduct);

    // add product OnClick
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProductInputDialog();
            }
        });

    // add Createuser OnlClick
        cardView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace MyNewActivity.class with the name of the activity you want to navigate to
                Intent intent = new Intent(requireContext(), CreateUser.class);
                startActivity(intent);
            }
        });

    // add ChangePinCode OnlClick
        cardView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), PinCodeChange.class);
                startActivity(intent);
            }
        });

    //edit products
        cardView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), EditProducts.class);
                startActivity(intent);
            }
        });

        return view;
    }



    private void showProductInputDialog() {
        // Create an instance of the bottom sheet dialog
        bottomSheetDialog = new BottomSheetDialog(requireContext());

        // Inflate the bottom sheet layout
        View bottomSheetView = getLayoutInflater().inflate(R.layout.settings_adding_products_popout, null);

        // Find the input fields and button in the bottom sheet
        edtProductName = bottomSheetView.findViewById(R.id.edtProductName);
        edtProductDescription = bottomSheetView.findViewById(R.id.edtProductDescription);
        edtProductPrice = bottomSheetView.findViewById(R.id.edtProductPrice);
        edtProductType = bottomSheetView.findViewById(R.id.edtProductType);
        edtProductImg = bottomSheetView.findViewById(R.id.edtImageUrl);
        btnAddProduct = bottomSheetView.findViewById(R.id.btnAddProduct);

        // Set up the ViewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Set a click listener for the "Add Product" button
        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertData();
                /* // Handle adding the product details here
                String productName = edtProductName.getText().toString();
                String productDescription = edtProductDescription.getText().toString();
                String productPrice = edtProductPrice.getText().toString();
                String productType = edtProductType.getText().toString();
                String productImg = edtProductImg.getText().toString();

                // Check if all fields are filled
                if (!productName.isEmpty() && !productDescription.isEmpty() && !productPrice.isEmpty() && !productType.isEmpty() && !productImg.isEmpty()) {
                    // Convert the product price to include the "Price:" prefix, peso sign, and ".00"
                    String formattedPrice = "Price: ₱" + productPrice + ".00";

                    // Create a new Product instance with the selected image
                    /*if (selectedImageUri != null) {
                        // Update the SharedViewModel to add the product to the list
                        sharedViewModel.addProduct(productName, selectedImageUri, productDescription, formattedPrice);

                        // Dismiss the bottom sheet after handling the input
                        bottomSheetDialog.dismiss();
                    } else {
                        Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Show a message if any of the fields are empty
                    Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } */
            }
        });

        // Set a click listener for the image view
        /*imgProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the gallery to select an image
                openGallery();
            }
        });*/

        // Set the view for the bottom sheet dialog
        bottomSheetDialog.setContentView(bottomSheetView);

        // Show the bottom sheet dialog
        bottomSheetDialog.show();
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            // Handle the selected image
            selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), selectedImageUri);

                // Set the selected image to the ImageView
                imgProduct.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void insertData() {
        String productPrice = edtProductPrice.getText().toString();
        Long priceLong = Long.parseLong(productPrice);

        Map<Object, Object> map = new HashMap<>();
        map.put("product_name",edtProductName.getText().toString());
        map.put("product_desc",edtProductDescription.getText().toString());
        map.put("product_cost", priceLong);
        map.put("product_type",edtProductType.getText().toString());
        map.put("image_url",edtProductImg.getText().toString());

        FirebaseDatabase.getInstance().getReference().child("products").push()
                .setValue(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(requireContext(), "New Product Added Successfully", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(requireContext(), "Error: Cannot add new product. Please try again.", Toast.LENGTH_SHORT).show();

                    }
                });
    }
}