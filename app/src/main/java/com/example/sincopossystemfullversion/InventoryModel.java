package com.example.sincopossystemfullversion;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

public class InventoryModel {
    public static List<InventoryModel> inventoryItemList = new ArrayList<>();

    // Add a method to access the inventory item list
    public static List<InventoryModel> getInventoryItemList() {
        return inventoryItemList;
    }

    public static void autoAddInventoryItem(String re_order, String ingredient_name, String ingredient_type, String notes, Long ingredient_cost, String code, Long ingredient_qty) {
        InventoryModel item = new InventoryModel(re_order, ingredient_name, ingredient_type, notes, ingredient_cost, code, ingredient_qty);
        inventoryItemList.add(item);
    }
    private String code, re_order, ingredient_name, ingredient_type, notes;
    private Long ingredient_qty, ingredient_cost;
    private boolean notified;
    public boolean archived;

    public InventoryModel() {
        // Default constructor required for Firebase
    }

    public boolean isArchived() {
        return archived;
    }

    public InventoryModel(String re_order, String ingredient_name, String ingredient_type, String notes, Long ingredient_cost, String code, Long ingredient_qty) {
        this.code = code;
        this.ingredient_cost = ingredient_cost;
        this.re_order = re_order;
        this.ingredient_name = ingredient_name;
        this.ingredient_qty = ingredient_qty;
        this.ingredient_type = ingredient_type;
        this.notes = notes;

        inventoryItemList.add(this);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getIngredient_cost() {
        return ingredient_cost;
    }

    public void setIngredient_cost(Long ingredient_cost) {
        this.ingredient_cost = ingredient_cost;
    }

    public String getRe_order() {
        return re_order;
    }

    public void setRe_order(String re_order) {
        this.re_order = re_order;
    }

    public String getIngredient_name() {
        return ingredient_name;
    }

    public void setIngredient_name(String ingredient_name) {
        this.ingredient_name = ingredient_name;
    }

    public Long getIngredient_qty() {
        return ingredient_qty;
    }

    public void setIngredient_qty(Long ingredient_qty) {
        this.ingredient_qty = ingredient_qty;
    }

    public String getIngredient_type() {
        return ingredient_type;
    }

    public void setIngredient_type(String ingredient_type) {
        this.ingredient_type = ingredient_type;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    public void sendNotification(Context mContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("low_stock_notification_channel", "Re-Order Notifications", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Check if the item has already been notified
        if (!isNotified()) {
            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

            SpannableString ingredientText = new SpannableString(ingredient_name);
            ingredientText.setSpan(new ForegroundColorSpan(Color.BLUE), 0, ingredient_name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, "low_stock_notification_channel")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Re-Order Alert")
                    .setContentText(ingredient_name + " quantity is low. Re-order now!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            // Use a unique notification ID
            int notificationId = generateNotificationId();

            notificationManager.notify(notificationId, builder.build());

            // Set the item as notified
            setNotified(true);
        }
    }

    public void sendOutOfStock(Context mContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("out_of_stock_notification_channel", "Out of Stock Notifications", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Check if the item has already been notified
        if (!isNotified()) {
            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

            SpannableString ingredientText = new SpannableString(ingredient_name);
            ingredientText.setSpan(new ForegroundColorSpan(Color.BLUE), 0, ingredient_name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, "out_of_stock_notification_channel")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Out of Stock Alert")
                    .setContentText(ingredient_name + " is Out of Stock.")
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            // Use a unique notification ID
            int notificationId = generateNotificationId();

            notificationManager.notify(notificationId, builder.build());

            // Set the item as notified
            setNotified(true);
        }
    }

    private int generateNotificationId() {
        // Generate a unique notification ID based on some criteria
        // For simplicity, you can use the hash code of the ingredient name
        return ingredient_name.hashCode();
    }
}
