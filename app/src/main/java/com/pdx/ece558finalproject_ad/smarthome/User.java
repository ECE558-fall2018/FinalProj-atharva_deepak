package com.pdx.ece558finalproject_ad.smarthome;

public class User {

    private String userId;
    private String passWord;
    private String email;
    private double lattitude;
    private double longitude;
    private NetworkInformation mNetworkInfo;

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

    public void setNetworkInfo(NetworkInformation networkInfo) {
        this.mNetworkInfo = networkInfo;
    }

    public User(String userId, String passWord) {
        this.userId = userId;
        this.passWord = passWord;
    }

    public User(String userId, String passWord, String apName, String apMAC, int apRSSI){
        this.userId = userId;
        this.passWord = passWord;
        this.mNetworkInfo.setSSID(apName);
        this.mNetworkInfo.setMAC(apMAC);
        this.mNetworkInfo.setRSSI(apRSSI);
    }

    public User(String userId, String passWord, NetworkInformation networkInfo){
        this.userId = userId;
        this.passWord = passWord;
        this.mNetworkInfo = networkInfo;
    }

    public String getApName() {
        return mNetworkInfo.getSSID();
    }

    public void setApName(String apName) {
        this.mNetworkInfo.setSSID(apName);
    }

    public String getMacAddr() {
        return this.mNetworkInfo.getMAC();
    }

    public void setMacAddr(String macAddr) {
        this.mNetworkInfo.setMAC(macAddr) ;
    }

    public double getLattitude() {
        return lattitude;
    }

    public void setLattitude(double lattitude) {
        this.lattitude = lattitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getRssi() {
        return this.mNetworkInfo.getRSSI();
    }

    public void setRssi(int rssi) {
        this.mNetworkInfo.setRSSI(rssi);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }
}

