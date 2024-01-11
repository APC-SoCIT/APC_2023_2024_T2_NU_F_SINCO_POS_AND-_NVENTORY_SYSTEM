package com.example.sincopossystemfullversion.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sincopossystemfullversion.ProductsModel;
import com.example.sincopossystemfullversion.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class ProductNew extends FirebaseRecyclerAdapter <ProductsModel, ProductNew.myViewHolder>{


    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     * @param context
     */
    public ProductNew(@NonNull FirebaseRecyclerOptions<ProductsModel> options, Context context) {
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

    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout,parent, false);


        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder{

        ImageView img;

        TextView prodname,proddesc, prodcost, productQuantity;

        ImageButton btnPositive, btnNegative;
        Button btnAdd;


        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            prodname = itemView.findViewById(R.id.ProductTitle);
            img = itemView.findViewById(R.id.cardImage);
            proddesc = itemView.findViewById(R.id.descriptionTitle);
            prodcost = itemView.findViewById(R.id.PriceTitle);
            productQuantity = itemView.findViewById(R.id.quantityTextView);
            btnAdd = itemView.findViewById(R.id.addbutton);
            btnPositive = itemView.findViewById(R.id.positiveIcon);
            btnNegative = itemView.findViewById(R.id.negativeIcon);


        }
    }


}
