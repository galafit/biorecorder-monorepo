package com.biorecorder.comport;

import jssc.SerialPortList;

import java.util.Scanner;

public class ComportToolkit {
    private static final int SLEEP_TIME_MS = 2000;
    private static final int COMPORT_SPEED = 38400;
    private static final byte[] comportCommand = {(byte)0x41, (byte) 0x54, (byte) 0x0D, (byte) 0x0A};

    public ComportToolkit() {
        String port = chooseComport();
        Comport comport = null;
        if(port != null) {
            comport = connectToComport(port, COMPORT_SPEED);
            sendBytes(comport, comportCommand);
        }
        try {
            Thread.sleep(SLEEP_TIME_MS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Чтобы выйти введите - close");
        System.out.println("Или байты для отправки: ");
        Scanner scan = new Scanner( System.in );
        while(true) {
            String inData = scan.nextLine();
            if(inData.equals("close")) {
                if(comport != null) {
                    comport.close();
                }
                System.exit(0);
            } else {
                byte[] bytes = hexStringWithSpacesToBytes(inData);
                bytes = stringToBytes(inData);
                sendBytes(comport, bytes);
            }

        }
    }

    public static void sendBytes(Comport comport, byte[] data) {
        comport.writeBytes(data);
        System.out.println("Отправлены байты: " + bytesToHex(data));
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
                char ch = (char) (inByte & 0xFF);
                System.out.println("Получен байт: " + bytesToHex(inByte) + "  " + ch);
            }
        });
        return comport;
    }

    public static String bytesToHex(byte... bytes) {
        char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 3];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = HEX_ARRAY[v >>> 4];
            hexChars[j * 3 + 1] = HEX_ARRAY[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';

        }
        return new String(hexChars);
    }

    public static byte[] stringToBytes(String s) {
        // для строки типа "AT\r\n"
        return  s.getBytes();
    }

    public static byte[] hexStringWithSpacesToBytes(String s) {
        // для строки типа "00 A0 BF"
        int len = s.length();
        byte[] data = new byte[(len + 1) / 3];
        for (int i = 0; i < len; i += 3) {
            data[i / 3] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static byte[] hexStringToBytes(String s) {
        // для строки типа "00A0BF"
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static void main(String[] args) {
        ComportToolkit comportToolkit = new ComportToolkit();
    }
}
