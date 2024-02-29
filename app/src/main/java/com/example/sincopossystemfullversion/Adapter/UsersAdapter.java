package com.example.sincopossystemfullversion.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sincopossystemfullversion.Activity.CreateUser;
import com.example.sincopossystemfullversion.Activity.EditUser;
import com.example.sincopossystemfullversion.Activity.LoginActivity;
import com.example.sincopossystemfullversion.R;
import com.example.sincopossystemfullversion.UserModel.UserModel;
import com.example.sincopossystemfullversion.UsersModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.HashMap;
import java.util.Map;

public class UsersAdapter extends FirebaseRecyclerAdapter <UsersModel, UsersAdapter.myViewHolder>{
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
    public UsersAdapter(@NonNull FirebaseRecyclerOptions<UsersModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull UsersModel model) {

        if (!model.getUser_type().equals("Admin")) {
            holder.userstv.setText(model.getUser_type());
            holder.pincodetv.setText(String.valueOf(model.getPin_code()));
        } else {
            // Hide the item for admin users
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }


        holder.editIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userKey = model.getUser_type().toLowerCase();
                Intent intent = new Intent(v.getContext(), EditUser.class);
                intent.putExtra("userKey", userKey);
                v.getContext().startActivity(intent);
            }
        });


        holder.deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDialog(position);
            }
        });


    }


    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.userstable,parent, false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder{
        TextView userstv, pincodetv;
        ImageView editIcon, deleteIcon;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            userstv = itemView.findViewById(R.id.userstv);
            pincodetv = itemView.findViewById(R.id.pincodetv);
            editIcon = itemView.findViewById(R.id.editIcon);
            deleteIcon = itemView.findViewById(R.id.deleteIcon);

        }

    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        // Split the input string by space
        String[] words = input.split("\\s+");
        StringBuilder capitalizedString = new StringBuilder();
        // Capitalize the first letter of each word and append it to the StringBuilder
        for (String word : words) {
            capitalizedString.append(word.substring(0, 1).toUpperCase()).append(word.substring(1)).append(" ");
        }
        // Trim any extra space at the end and return the capitalized string
        return capitalizedString.toString().trim();
    }

    private void insertData(){

    }
    private void showConfirmationDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this user?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // If user confirms, delete the item
                deleteItem(position);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // If user cancels, dismiss the dialog
                dialog.dismiss();
            }
        });
        builder.show();
    }
    private void deleteItem(int position) {
        String itemId = getRef(position).getKey();
        getRef(position).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Item removed successfully
                        Toast.makeText(context, "User removed successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to remove item
                        Toast.makeText(context, "Failed to remove user", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
