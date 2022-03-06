package com.biorecorder.ads;

/**
 * Listener for messages from Ads
 */
public interface MessageListener {
    public void onMessageReceived(AdsMessage messageType, String message);
}
