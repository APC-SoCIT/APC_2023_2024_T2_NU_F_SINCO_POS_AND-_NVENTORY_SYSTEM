package com.example.sincopossystemfullversion.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincopossystemfullversion.R;
import com.example.sincopossystemfullversion.SalesReportModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SalesReportNew extends FirebaseRecyclerAdapter<SalesReportModel, SalesReportNew.myViewHolder> {
    private String filterOption = ""; // Filter option
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Activity mActivity;





    public SalesReportNew(@NonNull FirebaseRecyclerOptions<SalesReportModel> options, Context context) {
        super(options);
        mActivity = (Activity) context;

    }

    public String getFilterOption() {
        return filterOption;
    }


    DecimalFormat currencyFormatter = new DecimalFormat("₱ #,###.00");

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull SalesReportModel model) {
        if (shouldIncludeSale(model)) {
            holder.itemView.setVisibility(View.VISIBLE);
            holder.srdate.setText(model.getSr_date());
            holder.srtid.setText(String.valueOf(model.getSr_transactionid()));
            holder.srproduct.setText(model.getSr_product());
            holder.srqty.setText(String.valueOf(model.getSr_qty()));
            holder.srprice.setText(currencyFormatter.format(model.getSr_price()));
            holder.srtotal.setText(currencyFormatter.format(model.getSr_total()));
            holder.srpaymethod.setText(model.getSr_payment());
        } else {
            // If not in the filter range, hide the item and set its height to zero
            holder.itemView.setVisibility(View.GONE);
            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
            params.height = 0;
            holder.itemView.setLayoutParams(params);
        }

        holder.deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create and show a confirmation dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle("Confirm Delete");
                builder.setMessage("Are you sure you want to delete this item?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get the position of the item clicked
                        int position = holder.getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            // Get the key of the item to be deleted from Firebase
                            String itemId = getRef(position).getKey();
                            // Remove the item from Firebase
                            getRef(position).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Item removed successfully
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Failed to remove item
                                }
                            });
                        }
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Dismiss the dialog if "No" is clicked
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sreport_layout, parent, false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder {
        TextView srdate, srtid, srproduct, srqty, srprice, srtotal, srpaymethod;
        ImageView deleteIcon;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            srdate = itemView.findViewById(R.id.tvDate);
            srtid = itemView.findViewById(R.id.tvID);
            srproduct = itemView.findViewById(R.id.tvProduct);
            srqty = itemView.findViewById(R.id.tvQty);
            srprice = itemView.findViewById(R.id.tvPrice);
            srtotal = itemView.findViewById(R.id.tvTotal);
            srpaymethod = itemView.findViewById(R.id.tvPaymethod);

            deleteIcon = itemView.findViewById(R.id.deleteIcon);
        }
    }

    // Method to set the filter option
    public void setFilterOption(String option) {
        filterOption = option;
        notifyDataSetChanged(); // Notify RecyclerView to refresh with new data
    }


    // Method to get the sales data map
    public Map<String, Object> getSalesData() {
        // Initialize a new map to store the sales data
        Map<String, Object> salesData = new HashMap<>();

        // Iterate over the items in the adapter and add them to the map
        for (int i = 0; i < getItemCount(); i++) {
            // Get the item at the current position
            SalesReportModel model = getItem(i);
            // Add the item to the map using its key (you can use a unique identifier as key)
            salesData.put(String.valueOf(i), model);
        }

        return salesData;
    }





    // Method to determine whether the sale should be included based on the filter option
    private boolean shouldIncludeSale(SalesReportModel model) {
        if (filterOption.equals("daily")) {
            return isWithinCurrentDay(model.getSr_date());
        } else if (filterOption.equals("weekly")) {
            return isWithinCurrentWeek(model.getSr_date());
        } else if (filterOption.equals("monthly")) {
            return isWithinCurrentMonth(model.getSr_date());
        } else if (filterOption.equals("yearly")) {
            return isWithinCurrentYear(model.getSr_date());
        }
        // For "All" option, include all sales
        return true;
    }

    // Method to check if the sale date is within the current day
    private boolean isWithinCurrentDay(String saleDate) {
        try {
            // Parse the sale date string into a Date object
            Date date = dateFormat.parse(saleDate);

            // Get Calendar instances for the current date and the sale date
            Calendar currentCalendar = Calendar.getInstance();
            Calendar saleCalendar = Calendar.getInstance();
            saleCalendar.setTime(date);

            // Check if the sale date is today
            return currentCalendar.get(Calendar.YEAR) == saleCalendar.get(Calendar.YEAR) &&
                    currentCalendar.get(Calendar.DAY_OF_YEAR) == saleCalendar.get(Calendar.DAY_OF_YEAR);
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Return false in case of an exception
        }
    }

    // Method to check if the sale date is within the current week
    private boolean isWithinCurrentWeek(String saleDate) {
        try {
            // Parse the sale date string into a Date object
            Date date = dateFormat.parse(saleDate);

            // Get Calendar instances for the current date and the sale date
            Calendar currentCalendar = Calendar.getInstance();
            Calendar saleCalendar = Calendar.getInstance();
            saleCalendar.setTime(date);

            // Check if the sale week is the same as the current week
            return currentCalendar.get(Calendar.YEAR) == saleCalendar.get(Calendar.YEAR) &&
                    currentCalendar.get(Calendar.WEEK_OF_YEAR) == saleCalendar.get(Calendar.WEEK_OF_YEAR);
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Return false in case of an exception
        }
    }

    // Method to check if the sale date is within the current month
    private boolean isWithinCurrentMonth(String saleDate) {
        try {
            // Parse the sale date string into a Date object
            Date date = dateFormat.parse(saleDate);

            // Get Calendar instances for the current date and the sale date
            Calendar currentCalendar = Calendar.getInstance();
            Calendar saleCalendar = Calendar.getInstance();
            saleCalendar.setTime(date);

            // Check if the sale month and year are the same as the current month and year
            return currentCalendar.get(Calendar.YEAR) == saleCalendar.get(Calendar.YEAR) &&
                    currentCalendar.get(Calendar.MONTH) == saleCalendar.get(Calendar.MONTH);
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Return false in case of an exception
        }
    }

    // Method to check if the sale date is within the current year
    private boolean isWithinCurrentYear(String saleDate) {
        try {
            // Parse the sale date string into a Date object
            Date date = dateFormat.parse(saleDate);

            // Get Calendar instances for the current date and the sale date
            Calendar currentCalendar = Calendar.getInstance();
            Calendar saleCalendar = Calendar.getInstance();
            saleCalendar.setTime(date);

            // Check if the sale year is the same as the current year
            return currentCalendar.get(Calendar.YEAR) == saleCalendar.get(Calendar.YEAR);
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Return false in case of an exception
        }
    }
}
