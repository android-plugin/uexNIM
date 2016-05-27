package org.zywx.wbpalmstar.plugin.uexnim.vo;

/**
 * Created by Fred on 2016/3/30.
 */
public class UserInfoVo {
    private String nickName;
    private String avatarUrl;
    private String thumbAvatarUrl;
    private String sign;
    private int gender;
    private String email;
    private String birth;
    private String mobile;
    private String ext;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getThumbAvatarUrl() {
        return thumbAvatarUrl;
    }

    public void setThumbAvatarUrl(String thumbAvatarUrl) {
        this.thumbAvatarUrl = thumbAvatarUrl;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }
}
