package com.example.sincopossystemfullversion;

public class UsersModel {

    String user_type, img_url;
            Long pin_code ;


    UsersModel(){


    }

    public UsersModel(Long pin_code, String user_type, String img_url) {
        this.user_type = user_type;
        this.pin_code = pin_code;
        this.img_url = img_url;
    }

    public Long getPin_code() {
        return pin_code;
    }

    public void setPin_code(Long pin_code) {
        this.pin_code = pin_code;
    }

    public String getUser_type() {return user_type;}

    public void setUser_type(String user_type) { this.user_type = user_type;}
    public String getImg_url() {return img_url;}

    public void setImg_url(String img_url) { this.img_url = img_url;}


}