package com.biorecorder.ads;

import java.util.List;

/**
 * Create AdsConfigurationCommand (to send to Ads-device) depending on the ads-type:
 * 2 channels or 8 channels
 */
interface AdsConfigurator {
    public List<byte[]> getConfigurationCommands(AdsConfig adsConfig);
    public byte[] getStopCommand();
    public byte[] getHelloCommand();
    public byte[] getPingCommand();
    public byte[] getHardwareRequestCommand();
}
