package com.example.sincopossystemfullversion.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sincopossystemfullversion.Activity.LoginActivity;
import com.example.sincopossystemfullversion.Fragments.HomeFragment;
import com.example.sincopossystemfullversion.R;
import com.example.sincopossystemfullversion.UsersModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class UserTypeAdapter extends FirebaseRecyclerAdapter <UsersModel, UserTypeAdapter.myViewHolder>{
    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     * @param context
     */

    private OnItemClickListener onItemClickListener;
    private static String selectedUserType;

    private Context context;

    public interface OnItemClickListener {
        void onItemClick(UsersModel usersModel);
    }

    public static String getSelectedUserType() {
        return selectedUserType;
    }

    public static void setSelectedUserType(String userType) {
        selectedUserType = userType;
    }

    // Constructor and other methods

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    public UserTypeAdapter(@NonNull FirebaseRecyclerOptions<UsersModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull UsersModel model) {

        holder.usertype.setText(model.getUser_type());

        Glide.with(holder.img.getContext())
                .load(model.getImg_url())
                .placeholder(com.firebase.ui.database.R.drawable.common_google_signin_btn_icon_dark)
                .circleCrop()
                .error(com.firebase.ui.database.R.drawable.common_google_signin_btn_icon_dark_normal)
                .into(holder.img);

        holder.cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userType = getItem(position).getUser_type().toLowerCase().replaceAll("\\s", "");

                // Pass selected userType to LoginActivity
                Intent loginIntent = new Intent(context, LoginActivity.class);
                loginIntent.putExtra("USER_TYPE", userType);
                context.startActivity(loginIntent);

                UserTypeAdapter.setSelectedUserType(userType);
            }
        });

    }


    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.typeofuser_card,parent, false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder{
        TextView usertype, pincode;
        ImageView img;

        CardView cardview;


        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            usertype = itemView.findViewById(R.id.userTypeTv);
            pincode = itemView.findViewById(R.id.tvID);
            img = itemView.findViewById(R.id.usertypeImage);
            cardview = itemView.findViewById(R.id.adminCardView);

        }

    }

}
