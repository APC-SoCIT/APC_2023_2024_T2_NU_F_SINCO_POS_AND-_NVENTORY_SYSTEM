package com.example.sincopossystemfullversion.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sincopossystemfullversion.ProductsModel;
import com.example.sincopossystemfullversion.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ArchivedProductsAdapter extends FirebaseRecyclerAdapter<ProductsModel, ArchivedProductsAdapter.myViewHolder> {

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     * @param context
     */

    private List<ProductsModel> archivedProductsList;
    private Activity mActivity;
    private List<ProductsModel> originalList;
    private List<ProductsModel> filteredList;
    private FirebaseRecyclerOptions<ProductsModel> options;
    public ArchivedProductsAdapter(@NonNull FirebaseRecyclerOptions<ProductsModel> options, Context context) {
        super(options);
        this.options = options;
        mActivity = (Activity) context;
    }


    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull ProductsModel model) {

        if (model.isArchived()) {
            // If archived, show the item in the RecyclerView
            holder.itemView.setVisibility(View.VISIBLE);
        } else {
            // If not archived, hide the item in the RecyclerView
            holder.itemView.setVisibility(View.GONE);
        }

        holder.prodname.setText(model.getProduct_name());
        holder.prodcost.setText("₱ " + String.valueOf(model.getProduct_cost()) + ".00");

        Glide.with(holder.cardImage.getContext())
                .load(model.getImage_url())
                .placeholder(com.firebase.ui.database.R.drawable.common_google_signin_btn_icon_dark)
                .circleCrop()
                .error(com.firebase.ui.database.R.drawable.common_google_signin_btn_icon_dark_normal)
                .into(holder.cardImage);

        holder.archiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the key of the selected product
                String productKey = getRef(position).getKey();

                // Update the archived status of the product in the Firebase database
                FirebaseDatabase.getInstance().getReference().child("products").child(productKey)
                        .child("archived").setValue(false)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                Toast.makeText(holder.prodname.getContext(), "Product Retrieved Successfully.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(holder.prodname.getContext(), "Failed to Retrieve Product. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R .layout.archivedproduct_card_layout,parent, false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder{

        ImageView cardImage;

        TextView prodname,proddesc, prodcost, ptype, productQuantity;

        Button archiveButton;


        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            prodname = itemView.findViewById(R.id.ProductTitle);
            proddesc = itemView.findViewById(R.id.descriptionTitle);
            prodcost = itemView.findViewById(R.id.PriceTitle);
            //ptype = itemView.findViewById(R.id.spinnerUsertype);
            productQuantity = itemView.findViewById(R.id.quantityTextView);
            cardImage = itemView.findViewById(R.id.cardImage);

            archiveButton = itemView.findViewById(R.id.addbtn);

        }
    }


}
