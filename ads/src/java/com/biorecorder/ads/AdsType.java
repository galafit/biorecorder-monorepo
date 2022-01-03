package com.biorecorder.ads;


public enum AdsType {
    ADS_2(2),
    ADS_8(8);
    private int numberOfAdsChannels;
    private AdsConfigurator adsConfigurator;

    //если true работает новая прошивка Стаса, а если false то Сашина прошивка
    private boolean isAdsNew = false;

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

    Command[] adsConfigurationCommands(AdsConfig adsConfig){
       return  adsConfigurator.getAdsConfigurationCommands(adsConfig);
    }

    Command adsStopCommand() {
        return adsConfigurator.getAdsStopCommand();
    }

    public static int getMaxChannelsCount() {
        return 8;
    }

}
