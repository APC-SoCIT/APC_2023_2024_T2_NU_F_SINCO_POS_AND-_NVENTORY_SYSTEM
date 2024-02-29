package com.example.sincopossystemfullversion.Adapter;

import static android.app.PendingIntent.getActivity;
import static android.content.Intent.getIntent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sincopossystemfullversion.Activity.AddingProducts;
import com.example.sincopossystemfullversion.Activity.ChangeImageProduct;
import com.example.sincopossystemfullversion.Activity.EditUser;
import com.example.sincopossystemfullversion.Activity.EditingProducts;
import com.example.sincopossystemfullversion.Activity.LoginActivity;
import com.example.sincopossystemfullversion.ProductsModel;
import com.example.sincopossystemfullversion.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditProductNew extends FirebaseRecyclerAdapter <ProductsModel, EditProductNew.myViewHolder>{

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     * @param context
     */

    private Map<String, Long> sizesMap;
    private static final int PICK_IMAGE_REQUEST = 1;
    DecimalFormat currencyFormatter = new DecimalFormat("₱ #,###.00");
    private Activity mActivity;
    ProductsModel model;
    private List<ProductsModel> originalList;
    private List<ProductsModel> filteredList;
    private ImageView imgSelectedImage;
    private FirebaseRecyclerOptions<ProductsModel> options;
    private Uri selectedImageUri;

    public EditProductNew(@NonNull FirebaseRecyclerOptions<ProductsModel> options, Context context) {
        super(options);
        this.options = options;
        mActivity = (Activity) context;
        originalList = new ArrayList<>();
        filteredList = new ArrayList<>();
    }

    // Rest of your adapter class implementation...

    public void updateOptions(FirebaseRecyclerOptions<ProductsModel> options) {
        this.options = options;
        notifyDataSetChanged();
        startListening();
    }

    public void setOriginalList(List<ProductsModel> originalList) {
        this.originalList = originalList;
        notifyDataSetChanged(); // Notify RecyclerView to update
    }

    // This method is used to filter the list based on the provided query
    public void filterList(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            String searchQuery = query.toLowerCase().trim();
            for (ProductsModel product : originalList) {
                if (product.getProduct_name().toLowerCase().contains(searchQuery)) {
                    filteredList.add(product);
                }
            }
        }
        notifyDataSetChanged(); // Notify RecyclerView to update
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull ProductsModel model) {

        sizesMap = new HashMap<>();

        if (model.isArchived()) {
            // If archived, show the item in the RecyclerView
            holder.itemView.setVisibility(View.GONE);
        } else {
            // If not archived, hide the item in the RecyclerView
            holder.itemView.setVisibility(View.VISIBLE);
        }

        holder.prodname.setText(model.getProduct_name());
        holder.prodcost.setText(currencyFormatter.format(model.getProduct_cost()));

        Glide.with(holder.img.getContext())
                .load(model.getImage_url())
                .placeholder(com.firebase.ui.database.R.drawable.common_google_signin_btn_icon_dark)
                .circleCrop()
                .error(com.firebase.ui.database.R.drawable.common_google_signin_btn_icon_dark_normal)
                .into(holder.img);


        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userKey = model.getProduct_name();
                Intent intent = new Intent(v.getContext(), EditingProducts.class);
                intent.putExtra("userKey", userKey);
                v.getContext().startActivity(intent);
            }
        });
        holder.archiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the key of the selected product
                String productKey = getRef(position).getKey();

                // Update the archived status of the product in the Firebase database
                FirebaseDatabase.getInstance().getReference().child("products").child(productKey)
                        .child("archived").setValue(true)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                Toast.makeText(holder.prodname.getContext(), "Product Archived Successfully.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(holder.prodname.getContext(), "Failed to Archive Product. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });



    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R .layout.editproduct_card_layout,parent, false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder{

        ImageView img, imgSelectedImage, imageUrl;

        TextView prodname, prodcost, productQuantity;

        Button editButton, archiveButton;


        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            prodname = itemView.findViewById(R.id.ProductTitle);
            img = itemView.findViewById(R.id.cardImage);
            prodcost = itemView.findViewById(R.id.PriceTitle);
            productQuantity = itemView.findViewById(R.id.quantityTextView);
            editButton = itemView.findViewById(R.id.editbtn);
            archiveButton = itemView.findViewById(R.id.delbtn);
            imgSelectedImage = itemView.findViewById(R.id.imgSelectedImage);
            imageUrl = itemView.findViewById(R.id.productImage);

            String selectedImageUriString = mActivity.getIntent().getStringExtra("selectedImageUri");
            if (selectedImageUriString != null) {
                // Convert the string URI to a Uri object
                selectedImageUri = Uri.parse(selectedImageUriString);
            }

        }
    }


    public void setSelectedImageUri(Uri selectedImageUri) {
        this.selectedImageUri = selectedImageUri;
        notifyDataSetChanged(); // Notify adapter to refresh views
    }
}
