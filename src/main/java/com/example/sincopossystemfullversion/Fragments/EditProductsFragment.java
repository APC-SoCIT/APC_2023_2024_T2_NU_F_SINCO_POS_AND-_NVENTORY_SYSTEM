package com.example.sincopossystemfullversion.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincopossystemfullversion.Activity.CheckoutDialogFragment;
import com.example.sincopossystemfullversion.Activity.EditProducts;
import com.example.sincopossystemfullversion.Activity.LoginActivity;
import com.example.sincopossystemfullversion.Activity.TypeofUser;
import com.example.sincopossystemfullversion.Adapter.EditProductAdapter;
import com.example.sincopossystemfullversion.Adapter.EditProductNew;
import com.example.sincopossystemfullversion.Adapter.ProductAdapter;
import com.example.sincopossystemfullversion.Adapter.ProductNew;
import com.example.sincopossystemfullversion.Java.Product;
import com.example.sincopossystemfullversion.ProductsModel;
import com.example.sincopossystemfullversion.R;
import com.example.sincopossystemfullversion.SharedViewModel.SharedViewModel;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;

public class EditProductsFragment extends Fragment implements EditProductAdapter.OnAddButtonClickListener {

    private View view; // Store the fragment view
    private SharedViewModel sharedViewModel; // Declare the shared ViewModel
    private Button backbtn;

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


        // Initialize the shared ViewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);


        // Set up RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView1);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 4); // Set the span count to 3
        recyclerView.setLayoutManager(gridLayoutManager);


        //New
        FirebaseRecyclerOptions<ProductsModel> options =
                new FirebaseRecyclerOptions.Builder<ProductsModel>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("products"), ProductsModel.class)
                        .build();

        EditProductNew adapter = new EditProductNew(options, requireContext());
        recyclerView.setAdapter(adapter);
        adapter.startListening();

        return view;


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

}