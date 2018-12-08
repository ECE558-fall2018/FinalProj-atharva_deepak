package com.pdx.ece558finalproject_ad.smarthome;

public class NetworkInformation {

    private String SSID;
    private String MAC;
    private int RSSI;

    public NetworkInformation(){

    }

    /**
     *
     * @param SSID
     * @param MAC
     * @param RSSI
     */

    public NetworkInformation(String SSID, String MAC, int RSSI) {
        this.SSID = SSID;
        this.MAC = MAC;
        this.RSSI = RSSI;
    }

    /**
     *
     * @return SSID
     */
    public String getSSID() {
        return SSID;
    }

    /**
     * sets SSID
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
     * sets MAC
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
     * sets RSSI
     * @param RSSI
     */

    public void setRSSI(int RSSI) {
        this.RSSI = RSSI;
    }
}

