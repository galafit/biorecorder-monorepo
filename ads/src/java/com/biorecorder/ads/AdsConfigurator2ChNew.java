package com.biorecorder.ads;

import java.util.ArrayList;
import java.util.List;

class AdsConfigurator2ChNew implements AdsConfigurator{

    @Override
    public byte[] getHelloCommand() {
        return AdsCommands.helloRequestCommand();
    }

    @Override
    public byte[] getPingCommand() {
        return AdsCommands.pingCommand();
    }

    @Override
    public byte[] getHardwareRequestCommand() {
        return AdsCommands.hardwareRequestCommand();
    }

    @Override
    public byte[] getStopCommand() {
        return AdsCommands.stopRecordingCommand();
    }

    @Override
    public List<byte[]> getConfigurationCommands(AdsConfig adsConfiguration) {
        List<byte[]> commands = new ArrayList<>(11);
        //первый регистр 0-5 частота сэмплирования от 125 сэмплов до 4к
        // 0 => 125, 1 => 250, 2 => 500, 3 => 1000, 4 => 2000, 5 => 4000
        byte regNumber = 0x01;
        byte regValue = 0x02;
        switch (adsConfiguration.getSampleRate()) {
            case S500: regValue = 0x02; break;
            case S1000: regValue = 0x03; break;
            case S2000: regValue = 0x04; break;
            case S4000: regValue = 0x05; break;
        }
        commands.add(AdsCommands.writeAdsRegisterCommand(regNumber, regValue));
        regValue = (byte) 0xA0;
        regValue = (byte) 0xA3;//test enabled 
        commands.add(AdsCommands.writeAdsRegisterCommand((byte)0x02, regValue));
        commands.add(AdsCommands.writeAdsRegisterCommand((byte)0x03, (byte)0x10));

        //4й регистр задаёт усиление в битах 4-6 для signal 0.
        int signalNumber = 0;
        regNumber = 0x04;
        regValue = getGainByte(adsConfiguration.getAdsChannelGain(signalNumber));
        regValue = 0x05;  // Set Channel 1 to test
        commands.add(AdsCommands.writeAdsRegisterCommand(regNumber, regValue));


        //5й регистр задаёт усиление в битах 4-6 для signal 1.
        signalNumber = 1;
        regNumber = 0x05;
        regValue = getGainByte(adsConfiguration.getAdsChannelGain(signalNumber));
        commands.add(AdsCommands.writeAdsRegisterCommand(regNumber, regValue));
        commands.add(AdsCommands.writeAdsRegisterCommand((byte)0x06, (byte)0x00));
        commands.add(AdsCommands.writeAdsRegisterCommand((byte)0x07, (byte)0x00));
        commands.add(AdsCommands.writeAdsRegisterCommand((byte)0x08, (byte)0x40));
        commands.add(AdsCommands.writeAdsRegisterCommand((byte)0x09, (byte)0x02));
        commands.add(AdsCommands.writeAdsRegisterCommand((byte)0x0A, (byte)0x03));


        byte divider0 = (byte) adsConfiguration.getAdsChannelDivider(0);
        byte divider1 = (byte) adsConfiguration.getAdsChannelDivider(1);
        commands.add(AdsCommands.startRecordingCommand(divider0, divider1));
        return commands;
    }

    private byte getGainByte(Gain gain) {
        // 0х10 - усиление 1, 0х20 - 2, 0х30 - 3, 0х40 - 4, 0x00 - 6 (default value), 0х50 - 8(!!), 0х60 - 12, 0х70 - не используется
        byte regByte = 0x10;
        switch (gain) {
            case G1: regByte = 0x10; break;
            case G2: regByte = 0x20; break;
            case G3: regByte = 0x30; break;
            case G4: regByte = 0x40; break;
            case G6: regByte = 0x00; break;
            case G8: regByte = 0x50; break;
            case G12: regByte = 0x60; break;
        }
        return regByte;
    }
}

