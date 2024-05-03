package de.htwberlin.f4.ai.ma.indoorroutefinder.wifi_scanner;

/**
 * Created by user Johann Winter
 * <p>
 * Factory for creating WifiScanner objectss
 */
public class WifiScannerFactory {

    public static WifiScanner createInstance() {
        return new WifiScannerImpl();
    }
}
