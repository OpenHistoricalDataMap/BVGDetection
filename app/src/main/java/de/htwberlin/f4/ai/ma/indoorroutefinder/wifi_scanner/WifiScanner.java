package de.htwberlin.f4.ai.ma.indoorroutefinder.wifi_scanner;

import android.net.wifi.WifiManager;

import java.util.List;

/**
 * Created by Johann Winter
 * <p>
 * The WifiScanner scans for available WiFi networks und returns a list of them.
 * It needs to be passed a WifiManager object and a boolean. If the boolean "onlyNetworksWithTwoOrMoreAPs"
 * is set true, the WifiScanner counts the occurrences of the SSIDs and returns a list which contains
 * only the SSIDs which have two or more BSSIDs (MAC-Addresses, Accesspoints).
 */
public interface WifiScanner {

    /**
     * @param wifiManager                  the WifiManager object which is used to scan the WiFi
     * @param onlyNetworksWithTwoOrMoreAPs true, if only networks with more than two access points
     *                                     should be returned
     * @return a list of SSIDs (network names)
     */
    List<String> getAvailableNetworks(WifiManager wifiManager, boolean onlyNetworksWithTwoOrMoreAPs);
}
