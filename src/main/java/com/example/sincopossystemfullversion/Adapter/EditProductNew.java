package com.example.sincopossystemfullversion.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.HashMap;
import java.util.Map;

public class EditProductNew extends FirebaseRecyclerAdapter <ProductsModel, EditProductNew.myViewHolder>{


    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     * @param context
     */
    public EditProductNew(@NonNull FirebaseRecyclerOptions<ProductsModel> options, Context context) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull ProductsModel model) {

        holder.prodname.setText(model.getProduct_name());
        holder.proddesc.setText(model.getProduct_desc());
        holder.prodcost.setText("₱ " + String.valueOf(model.getProduct_cost()) + ".00");

        Glide.with(holder.img.getContext())
                .load(model.getImage_url())
                .placeholder(com.firebase.ui.database.R.drawable.common_google_signin_btn_icon_dark)
                .circleCrop()
                .error(com.firebase.ui.database.R.drawable.common_google_signin_btn_icon_dark_normal)
                .into(holder.img);

        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DialogPlus dialogPlus = DialogPlus.newDialog(holder.img.getContext())
                        .setContentHolder(new ViewHolder(R.layout.settings_edit_products_popout))
                        .setExpanded(true,800)
                        .create();

                View view = dialogPlus.getHolderView();

                //Edit texts
                EditText pname = view.findViewById(R.id.edtProductName);
                EditText pdesc = view.findViewById(R.id.edtProductDescription);
                EditText ptype = view.findViewById(R.id.edtProductType);
                EditText pprice = view.findViewById(R.id.edtProductPrice);
                EditText pimg = view.findViewById(R.id.edtImageUrl);

                Button btnUpdate = view.findViewById(R.id.btnUpdate);

                pname.setText(model.getProduct_name());
                pdesc.setText(model.getProduct_desc());
                ptype.setText(model.getProduct_type());
                pprice.setText(String.valueOf(model.getProduct_cost()));
                pimg.setText(model.getImage_url());

                dialogPlus.show();

                btnUpdate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String productPrice = pprice.getText().toString();
                        Long priceLong = Long.parseLong(productPrice);

                        Map<String, Object> map = new HashMap<>();
                        map.put("product_name",pname.getText().toString());
                        map.put("product_desc",pdesc.getText().toString());
                        map.put("product_cost", priceLong);
                        map.put("product_type",ptype.getText().toString());
                        map.put("image_url",pimg.getText().toString());

                        FirebaseDatabase.getInstance().getReference().child("products")
                                .child(getRef(position).getKey()).updateChildren(map)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(holder.prodname.getContext(), "Product Updated Successfully.", Toast.LENGTH_SHORT).show();
                                        dialogPlus.dismiss();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(Exception e) {
                                        Toast.makeText(holder.prodname.getContext(), "Product Update Failed. Please try again.", Toast.LENGTH_SHORT).show();
                                        dialogPlus.dismiss();
                                    }
                                });

                    }
                });
            }
        });

    }



    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.editproduct_card_layout,parent, false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder{

        ImageView img;

        TextView prodname,proddesc, prodcost, productQuantity;

        Button editButton, deleteButton;


        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            prodname = itemView.findViewById(R.id.ProductTitle);
            img = itemView.findViewById(R.id.cardImage);
            proddesc = itemView.findViewById(R.id.descriptionTitle);
            prodcost = itemView.findViewById(R.id.PriceTitle);
            productQuantity = itemView.findViewById(R.id.quantityTextView);

            editButton = itemView.findViewById(R.id.editbtn);
            deleteButton = itemView.findViewById(R.id.delbtn);


        }
    }


}
