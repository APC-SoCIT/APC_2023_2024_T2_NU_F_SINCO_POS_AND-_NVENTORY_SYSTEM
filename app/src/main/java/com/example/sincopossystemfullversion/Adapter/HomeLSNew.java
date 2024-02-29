package com.example.sincopossystemfullversion.Adapter;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincopossystemfullversion.InventoryModel;
import com.example.sincopossystemfullversion.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.util.HashSet;
import java.util.Set;

public class HomeLSNew extends FirebaseRecyclerAdapter <InventoryModel, HomeLSNew.myViewHolder>{

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     * @param context
     */
    private static final String CHANNEL_ID = "low_stock_notification_channel";
    private static final int NOTIFICATION_ID = 123;
    public static final int PERMISSION_REQUEST_CODE = 22;
    private static final String PREF_NAME = "notification_prefs";
    private SharedPreferences sharedPreferences;
    private Context mContext;
    private Set<String> notifiedItems = new HashSet<>();

    public HomeLSNew(@NonNull FirebaseRecyclerOptions<InventoryModel> options, Context context) {
        super(options);
        mContext = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        notifiedItems = sharedPreferences.getStringSet("notifiedItems", new HashSet<>());
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull InventoryModel model) {

        if (model.getIngredient_qty() < 5 && model.getIngredient_qty() != 0) {
            holder.ingname.setText(model.getIngredient_name());
            holder.ingqty.setText(String.valueOf(model.getIngredient_qty()));
        } else if (model.getIngredient_qty() == 0) {
            holder.ingname.setVisibility(View.GONE);
            holder.ingqty.setVisibility(View.GONE);
        }
    }


    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Re-trigger the notification logic when creating the view holder
        return new myViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.home_lowstock, parent, false));
    }

    static class myViewHolder extends RecyclerView.ViewHolder {
        TextView ingname, ingqty;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            ingname = itemView.findViewById(R.id.homelsItem);
            ingqty = itemView.findViewById(R.id.homelsTotal);
        }
    }

    public void sendNotification(String ingredientName) {
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Re-Order Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        SpannableString ingredientText = new SpannableString(ingredientName);
        ingredientText.setSpan(new ForegroundColorSpan(Color.BLUE), 0, ingredientName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Re-Order Alert")
                .setContentText(ingredientName + " quantity is low. Re-order now!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Generate a unique notification ID based on ingredientName
        int notificationId = ingredientName.hashCode();
        notificationManager.notify(notificationId, builder.build());
    }



    private void saveNotifiedItems() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("notifiedItems", notifiedItems);
        editor.apply();
    }

    // Add method to reset SharedPreferences
    public void resetSharedPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void resetNotifiedItems() {
        notifiedItems.clear(); // Clear the set of notified items
        saveNotifiedItems(); // Save the changes to SharedPreferences
    }
}