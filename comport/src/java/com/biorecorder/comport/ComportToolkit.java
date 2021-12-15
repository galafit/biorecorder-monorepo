package com.biorecorder.comport;

import jssc.SerialPortList;

import java.util.Scanner;

public class ComportToolkit {
    private static final int SLEEP_TIME_MS = 2000;
    private static final int COMPORT_SPEED = 460800;
    private static final byte[] comportCommand = {(byte)0xFD, (byte) 0xFD};

    public ComportToolkit() {
        String port = chooseComport();
        Comport comport = null;
        if(port != null) {
            comport = connectToComport(port, COMPORT_SPEED);
            comport.writeBytes(comportCommand);
            System.out.println("В компорт отправлены байты:");
            for (byte b : comportCommand) {
                System.out.println(bytesToHex(b));
            }
        }
        try {
            Thread.sleep(SLEEP_TIME_MS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Чтобы выйти введите - close: ");
        Scanner scan = new Scanner( System.in );
        String inData = scan.nextLine();
        if(inData.equals("close")) {
            if(comport != null) {
                comport.close();
            }
            System.exit(0);
        }
    }

    public static String[] getAvailableComports() {
        String[] availableComports = SerialPortList.getPortNames();
        return availableComports;
    }

    public static String chooseComport() {
        String[] availableComports = getAvailableComports();
        if(availableComports == null || availableComports.length == 0) {
            System.out.println("Нет доступных компортов!");
            return null;
        }
        if(availableComports.length == 1) {
            String port = availableComports[0];
            System.out.println("Компорт: "+ port);
            return port;
        }
        System.out.println("Доступные порты:");
        for (String port : availableComports) {
            System.out.println(port);
        }
        System.out.println("Выберите порт: ");
        Scanner scan = new Scanner( System.in );
        String inData = scan.nextLine();
        return inData;
    }

    public static Comport connectToComport(String name, int speed) {
        Comport comport =  new ComportJSCC(name, speed);
        System.out.println("Соединились с компортом: " + name);
        comport.addListener(new ComportListener() {
            @Override
            public void onByteReceived(byte inByte) {
                System.out.println("Получен байт: " + bytesToHex(inByte));
            }
        });
        return comport;
    }

    public static String bytesToHex(byte... bytes) {
        char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static void main(String[] args) {
        ComportToolkit comportToolkit = new ComportToolkit();
    }
}
