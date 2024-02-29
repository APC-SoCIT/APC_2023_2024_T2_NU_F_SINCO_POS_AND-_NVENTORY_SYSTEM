package com.example.sincopossystemfullversion.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincopossystemfullversion.InventoryModel;
import com.example.sincopossystemfullversion.R;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArchivedItemsAdapter extends FirebaseRecyclerAdapter <InventoryModel, ArchivedItemsAdapter.myViewHolder>{

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     * @param context
     */
    private Activity mActivity;
    private String selectedUserType; private DatabaseReference usersReference;


    public ArchivedItemsAdapter(@NonNull FirebaseRecyclerOptions<InventoryModel> options, String selectedUserType, Context context) {
        super(options);
        mActivity = (Activity) context;
        this.selectedUserType = selectedUserType;
        this.usersReference = FirebaseDatabase.getInstance().getReference("user");
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull InventoryModel model) {

        Log.d("Firebase", "Selected User: " + selectedUserType);
        if (model.isArchived()) {
            // If archived, show the item in the RecyclerView
            holder.itemView.setVisibility(View.VISIBLE);
        } else {
            // If not archived, hide the item in the RecyclerView
            holder.itemView.setVisibility(View.GONE);
        }

        holder.edtItemName.setText(model.getIngredient_name());
        holder.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedUserType != null && selectedUserType.equals("admin")) {

                    showConfirmationDialog(position, holder);
                } else {
                    showAdminPinDialog();
                }

            }
        });

    }


    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.archivedtable,parent, false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder {
        TextView edtItemName;
        Button addButton;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            edtItemName = itemView.findViewById(R.id.edtItemName);
            addButton = itemView.findViewById(R.id.addbtn);

        }
    }

    private void showAdminPinDialog() {
        AlertDialog.Builder adminPinBuilder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View adminPinView = inflater.inflate(R.layout.admin_permission_dialog, null);
        Button submitButton = adminPinView.findViewById(R.id.submitButton);

        adminPinBuilder.setView(adminPinView);

        AlertDialog adminPinDialog = adminPinBuilder.create();
        adminPinDialog.show();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adminPinDialog.dismiss();
            }
        });
    }

    private void showConfirmationDialog(int position, myViewHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("Confirm Archive");
        builder.setMessage("Are you sure you want to archive this item?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String productKey = getRef(position).getKey();

                // Update the archived status of the product in the Firebase database
                FirebaseDatabase.getInstance().getReference().child("ingredients").child(productKey)
                        .child("archived").setValue(false)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(holder.edtItemName.getContext(), "Item Retrieved Successfully.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(holder.edtItemName.getContext(), "Failed to Retrieve Product. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
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
}
