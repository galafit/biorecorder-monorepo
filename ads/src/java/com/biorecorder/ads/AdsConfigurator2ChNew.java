package com.biorecorder.ads;

class AdsConfigurator2ChNew implements AdsConfigurator{
    public static final int NUMBER_OF_ADS_CHANNELS = 2;
    public static final int NUMBER_OF_ACCELEROMETER_CHANNELS = 3;

    private static final byte FF = (byte) 0xFF;
    private static final byte[] HELLO_COMMAND = {0x10, (byte)0xFD, FF};
    private static final byte[] PING_COMMAND = {0x10, (byte)0xFC, FF};
    private static final byte[] HARDWARE_COMMAND = {0x10, (byte)0xFA, FF};
    private static final byte[] STOP_COMMAND = {0x06, 0x11, FF, 0x06, 0x0A, FF};
    private static final byte[] START_COMMAND = {0x10, (byte)0xFB, FF};

    @Override
    public Command getHelloCommand() {
        return new CommandBase(HELLO_COMMAND);
    }

    @Override
    public Command getPingCommand() {
        return new CommandBase(PING_COMMAND);
    }

    @Override
    public Command getHardwareRequestCommand() {
        return new CommandBase(HARDWARE_COMMAND);
    }

    @Override
    public Command getStopCommand() {
        return new CommandBase(STOP_COMMAND);
    }

    @Override
    public Command[] getConfigurationCommands(AdsConfig adsConfiguration) {
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
        Command command1 = new CommandOneRegister(regNumber, regValue);
        Command command2 = new CommandOneRegister((byte)0x02, (byte)0xA0);
        Command command3 = new CommandOneRegister((byte)0x03, (byte)0x10);

        //4й регистр задаёт усиление в битах 4-6 для signal 0.
        int signalNumber = 0;
        regNumber = 0x04;
        regValue = getGainByte(adsConfiguration.getAdsChannelGain(signalNumber));
        Command command4 = new CommandOneRegister(regNumber, regValue);

        //5й регистр задаёт усиление в битах 4-6 для signal 1.
        signalNumber = 1;
        regNumber = 0x05;
        regValue = getGainByte(adsConfiguration.getAdsChannelGain(signalNumber));
        Command command5 = new CommandOneRegister(regNumber, regValue);
        Command command6 = new CommandOneRegister((byte)0x06, (byte)0x00);
        Command command7 = new CommandOneRegister((byte)0x07, (byte)0x00);
        Command command8 = new CommandOneRegister((byte)0x08, (byte)0x40);
        Command command9 = new CommandOneRegister((byte)0x09, (byte)0x02);
        Command command10 = new CommandOneRegister((byte)0x0A, (byte)0x03);

        // активируем делители по каналу 0
        signalNumber = 0;
        byte divider = (byte) adsConfiguration.getAdsChannelDivider(signalNumber);
        byte[] commandBytes0 = {0x01, 0x02, 0x02, divider, FF};
        Command command11 = new CommandBase(commandBytes0);

        // активируем делители по каналу 1
        signalNumber = 1;
        divider = (byte) adsConfiguration.getAdsChannelDivider(signalNumber);
        byte[] commandBytes1 = {0x01, 0x03, 0x02, divider, FF};
        Command command12 = new CommandBase(commandBytes1);

        Command[] commands = {
                //new CommandBase(STOP_COMMAND), //стоповая команда и так шлется в Ads перед запуском
                command1,
                command2,
                command3,
                command4,
                command5,
                command6,
                command7,
                command8,
                command9,
                command10,
                command11,
                command12,
                new CommandBase(START_COMMAND)
        };
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

