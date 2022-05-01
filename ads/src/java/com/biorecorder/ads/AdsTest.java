package com.biorecorder.ads;


public class AdsTest {
    public static void main(String[] args) {
        Ads ads = new Ads("COM3");
        AdsConfig config = new AdsConfig();
        config.setSampleRate(Sps.S500);
        ads.startRecording(config);
    }
}
