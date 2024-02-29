package com.example.sincopossystemfullversion.Fragments;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.sincopossystemfullversion.Adapter.ArchivedProductsAdapter;
import com.example.sincopossystemfullversion.Adapter.EditProductAdapter;
import com.example.sincopossystemfullversion.Adapter.EditProductNew;
import com.example.sincopossystemfullversion.Adapter.InventoryNew;
import com.example.sincopossystemfullversion.InventoryModel;
import com.example.sincopossystemfullversion.RecycleViewFix.GridSpacingItemDecoration;
import com.example.sincopossystemfullversion.ProductsModel;
import com.example.sincopossystemfullversion.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public class EditProductsFragment extends Fragment implements EditProductAdapter.OnAddButtonClickListener {

    private View view; // Store the fragment view
    private Button backbtn;

    EditProductNew adapter;

    private SwipeRefreshLayout swipeRefreshLayout; // Added SwipeRefreshLayout
    private static final int PICK_IMAGE_REQUEST = 1;
    private DialogPlus dialogPlus;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_editproduct, container, false);



        backbtn = view.findViewById(R.id.backbtn);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });


        // Set up RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.addedProductsRecyclerView);

        int spanCount = 3;

        int spacing = 16;

        GridSpacingItemDecoration itemDecoration = new GridSpacingItemDecoration(spanCount, spacing, true);

        recyclerView.addItemDecoration(itemDecoration);

        FirebaseRecyclerOptions<ProductsModel> options =
                new FirebaseRecyclerOptions.Builder<ProductsModel>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("products").orderByChild("archived").equalTo(false), ProductsModel.class)
                        .build();

        adapter = new EditProductNew(options, requireContext());
        recyclerView.setAdapter(adapter);
        adapter.startListening();

        RecyclerView recyclerView1 = view.findViewById(R.id.archivedProducts);
        recyclerView1.addItemDecoration(itemDecoration);

        FirebaseRecyclerOptions<ProductsModel> archivedOptions =
                new FirebaseRecyclerOptions.Builder<ProductsModel>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("products").orderByChild("archived").equalTo(true), ProductsModel.class)
                        .build();

        ArchivedProductsAdapter adapter1 = new ArchivedProductsAdapter(archivedOptions, requireContext());
        recyclerView1.setAdapter(adapter1);
        adapter1.startListening();

        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                txtSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                txtSearch(query);
                return false;
            }
        });

        return view;

    }

    private void txtSearch(String str) {
        String capitalizedStr = capitalizeSearch(str);

        FirebaseRecyclerOptions<ProductsModel> options =
                new FirebaseRecyclerOptions.Builder<ProductsModel>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("products")
                                        .orderByChild("product_name")
                                        .startAt(capitalizedStr)
                                        .endAt(capitalizedStr + "~"),
                                ProductsModel.class)
                        .build();

        adapter = new EditProductNew(options, requireContext());
        adapter.startListening();
        RecyclerView recyclerView = view.findViewById(R.id.addedProductsRecyclerView);
        recyclerView.setAdapter(adapter);
    }

    private String capitalizeSearch(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }


    @Override
    public void onAddButtonClick(int position) {

    }

    @Override
    public void onPositiveClick(int position) {

    }

    @Override
    public void onNegativeClick(int position) {

    }

    @Override
    public void onEditButtonClick(int position) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getStringExtra("selectedImageUri") != null) {
            // Retrieve the selected image URI from the intent data
            String selectedImageUriString = data.getStringExtra("selectedImageUri");
            Uri selectedImageUri = Uri.parse(selectedImageUriString);

            // Pass the selected image URI to the adapter
            adapter.setSelectedImageUri(selectedImageUri);

            // Trigger the dialog again to reload it with the updated image URI
            showEditDialog(selectedImageUri);
        }
    }

    private void showEditDialog(Uri selectedImageUri) {
        // Create a new instance of the dialog
        dialogPlus = DialogPlus.newDialog(requireContext())
                .setContentHolder(new ViewHolder(R.layout.settings_edit_products_popout))
                .setExpanded(true)
                .create();

        // Get the view of the dialog
        View dialogView = dialogPlus.getHolderView();

        // Find the ImageView in the dialog layout
        ImageView imageView = dialogView.findViewById(R.id.imgSelectedImage);

        // Load the selected image into the ImageView
        Glide.with(requireContext())
                .load(selectedImageUri)
                .into(imageView);

        // Show the dialog
        dialogPlus.show();
    }


}
