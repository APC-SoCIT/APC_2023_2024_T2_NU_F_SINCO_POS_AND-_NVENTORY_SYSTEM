package com.example.sincopossystemfullversion.UserModel;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class UserModel implements Parcelable {
    private Uri imageUri;
    private String text;

    public UserModel(Uri imageUri, String text) {
        this.imageUri = imageUri;
        this.text = text;
    }

    protected UserModel(Parcel in) {
        imageUri = in.readParcelable(Uri.class.getClassLoader());
        text = in.readString();
    }

    public static final Creator<UserModel> CREATOR = new Creator<UserModel>() {
        @Override
        public UserModel createFromParcel(Parcel in) {
            return new UserModel(in);
        }

        @Override
        public UserModel[] newArray(int size) {
            return new UserModel[size];
        }
    };

    public Uri getImageUri() {
        return imageUri;
    }

    public String getText() {
        return text;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(imageUri, flags);
        dest.writeString(text);
    }
}