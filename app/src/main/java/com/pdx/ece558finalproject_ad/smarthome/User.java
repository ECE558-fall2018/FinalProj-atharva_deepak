package com.pdx.ece558finalproject_ad.smarthome;

public class User {

    private String userId;
    private String mPassword;
    private String email;
    private double latitude;
    private double longitude;
    private NetworkInformation mNetworkInfo;
    private String SSID;
    private String MAC;
    private int RSSI;

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public String getMAC() {
        return MAC;
    }

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }

    public int getRSSI() {
        return RSSI;
    }

    public void setRSSI(int RSSI) {
        this.RSSI = RSSI;
    }

    public User(){

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public NetworkInformation getNetworkInfo() {
        return mNetworkInfo;
    }

    public User setNetworkInfo(NetworkInformation networkInfo) {
        this.mNetworkInfo = networkInfo;
        return this;
    }

    public User(String userId, String passWord) {
        this.userId = userId;
        this.mPassword = passWord;
    }

    public User(String userId, String passWord, NetworkInformation networkInfo){
        this.userId = userId;
        this.mPassword = passWord;
        this.mNetworkInfo = networkInfo;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        this.mPassword = password;
    }
}

