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

    /**
     *
     * @return SSID
     */
    public String getSSID() {
        return SSID;
    }

    /**
     * set SSID
     * @param SSID
     */
    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    /**
     *
     * @return MAC
     */
    public String getMAC() {
        return MAC;
    }

    /**
     * Set MAC
     * @param MAC
     */

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }

    /**
     *
     * @return RSSI
     */
    public int getRSSI() {
        return RSSI;
    }

    /**
     * set RSSI
     * @param RSSI
     */
    public void setRSSI(int RSSI) {
        this.RSSI = RSSI;
    }



    public User(){

    }

    /**
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * set email
     * @param email
     */

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     *
     * @return mnetworkInfor
     */
    public NetworkInformation getNetworkInfo() {
        return mNetworkInfo;
    }

    /**
     * sets networkInfor
     * @param networkInfo
     * @return object
     */
    public User setNetworkInfo(NetworkInformation networkInfo) {
        this.mNetworkInfo = networkInfo;
        return this;
    }

    /**
     * sets userid and password
     * @param userId
     * @param passWord
     */

    public User(String userId, String passWord) {
        this.userId = userId;
        this.mPassword = passWord;
    }

    /**
     *
     * @param userId
     * @param passWord
     * @param networkInfo
     */
    public User(String userId, String passWord, NetworkInformation networkInfo){
        this.userId = userId;
        this.mPassword = passWord;
        this.mNetworkInfo = networkInfo;
    }

    /**
     *
     * @return latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * set latitude
     * @param latitude
     */

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }


    /**
     *
     * @return longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * sets longitude
     * @param longitude
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     *
     * @return userid
     */
    public String getUserId() {
        return userId;
    }

    /**
     * sets userid
     * @param userId
     */

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     *
     * @return password
     */

    public String getPassword() {
        return mPassword;
    }

    /**
     * sets password
     * @param password
     */
    public void setPassword(String password) {
        this.mPassword = password;
    }
}

