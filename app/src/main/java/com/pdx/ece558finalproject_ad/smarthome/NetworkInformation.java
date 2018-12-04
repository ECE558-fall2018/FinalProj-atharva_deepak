package com.pdx.ece558finalproject_ad.smarthome;

public class NetworkInformation {

    private String SSID;
    private String MAC;
    private int RSSI;

    public NetworkInformation(){

    }

    public NetworkInformation(String SSID, String MAC, int RSSI) {
        this.SSID = SSID;
        this.MAC = MAC;
        this.RSSI = RSSI;
    }

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
}

