package com.biorecorder.ads;


import java.util.List;

public enum AdsType {
    ADS_2(2),
    ADS_8(8);
    private int numberOfAdsChannels;
    private AdsConfigurator adsConfigurator;

    //если true работает новая прошивка Стаса, а если false то Сашина прошивка
    private boolean isAdsNew = true;

    private AdsType(int numberOfAdsChannels) {
        this.numberOfAdsChannels = numberOfAdsChannels;

        if(numberOfAdsChannels == 2){
            adsConfigurator = isAdsNew ? new AdsConfigurator2ChNew() : new AdsConfigurator2Ch();
        } else if(numberOfAdsChannels == 8) {
            adsConfigurator =  new AdsConfigurator8Ch();
        } else {
            String msg = "Invalid Ads channels count: "+numberOfAdsChannels+ ". Number of Ads channels may be 2 or 8";
            throw new IllegalArgumentException(msg);
        }
    }

    public static Divider getAccelerometerAvailableDivider() {
        return Divider.D10;
    }

    public static AdsType valueOf(int channelsCount) throws IllegalArgumentException {
        for (AdsType adsType : AdsType.values()) {
            if(adsType.adsChannelsCount() == channelsCount) {
                return adsType;
            }

        }
        String msg = "Invalid Ads channels count: "+channelsCount+ ". Number of Ads channels may be 2 or 8";
        throw new IllegalArgumentException(msg);
    }

    public int adsChannelsCount() {
        return numberOfAdsChannels;
    }

    List<byte[]> adsConfigurationCommands(AdsConfig adsConfig){
       return  adsConfigurator.getConfigurationCommands(adsConfig);
    }

    byte[] adsStopCommand() {
        return adsConfigurator.getStopCommand();
    }

    byte[] adsPingCommand() {
        return adsConfigurator.getPingCommand();
    }

    byte[] adsHelloCommand() {
        return adsConfigurator.getHelloCommand();
    }

    byte[] adsHardwareRequestCommand() {
        return adsConfigurator.getHardwareRequestCommand();
    }

    public static int getMaxChannelsCount() {
        return 8;
    }

}
