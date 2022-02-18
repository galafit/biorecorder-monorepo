package com.biorecorder.ads;

/**
 * Create AdsConfigurationCommand (to send to Ads-device) depending on the ads-type:
 * 2 channels or 8 channels
 */
interface AdsConfigurator {
    public Command[] getConfigurationCommands(AdsConfig adsConfig);
    public Command getStopCommand();
    public Command getHelloCommand();
    public Command getPingCommand();
    public Command getHardwareRequestCommand();
}
