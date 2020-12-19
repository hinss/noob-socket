package com.hins.noobsocket.client;

import constants.UDPConstants;
import udplib.MessageCreator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author: hins
 * @created: 2020-09-12 23:24
 * @desc: udp 组播搜索方
 **/
public class UdpMulticastSearcher {

    public static void main(String[] args) throws IOException {

        // 1. 开始监听
        Listener listener = listen(UDPConstants.PORT_CLIENT_RESPONSE);

        // 2. 发送组播
        sendBrocast();

        System.in.read();

        List<Device> deviceList = listener.getDeviceList();
        for(Device device : deviceList){
            System.out.println(device);
        }

        //完成
        System.out.println("UDPSearcher Finished!");
    }

    private static void sendBrocast() {

        try {
            DatagramSocket searcher = new DatagramSocket();

            String buildWithPort = MessageCreator.buildWithPort(UDPConstants.PORT_CLIENT_RESPONSE);
            DatagramPacket datagramPacket = new DatagramPacket(
                    buildWithPort.getBytes(),
                    buildWithPort.getBytes().length,
                    // 组播只需要绑定局域网地址
                    InetAddress.getByName("255.255.255.255"),
                    UDPConstants.PORT_SERVER);

            searcher.send(datagramPacket);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static Listener listen(int port){

       CountDownLatch countDownLatch = new CountDownLatch(1);
       Listener listener = new Listener(port, countDownLatch);
       listener.start();

       return listener;
    }

    private static class Listener extends Thread{

        private final int listenPort;
        private final CountDownLatch countDownLatch;
        private final List<Device> deviceList = new ArrayList<>();
        private boolean done = false;
        private DatagramSocket datagramSocket = null;

        private Listener(int listenPort, CountDownLatch countDownLatch) {
            this.listenPort = listenPort;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            System.out.println("UDP 搜索方开始监听端口:" + listenPort);
            try {
                // 通知外部可以发送广播
                countDownLatch.countDown();

                datagramSocket = new DatagramSocket(listenPort);

                while(!done){

                    final byte[] buffer = new byte[512];
                    DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

                    datagramSocket.receive(receivePacket);

                    String ip = receivePacket.getAddress().getHostAddress();
                    int port = receivePacket.getPort();
                    byte[] data = receivePacket.getData();
                    int length = receivePacket.getLength();
                    String receiveMsg = new String(data,0,length);

                    String sn = MessageCreator.parseSN(receiveMsg);
                    if(sn != null){

                        Device device = new Device(port, ip, sn);
                        deviceList.add(device);
                    }
                }

                System.out.println("UDPSearcher listener finished.");

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close();
            }
        }

        private void close() {
            if(datagramSocket != null){
                datagramSocket.close();
                datagramSocket = null;
            }
        }

        List<Device> getDeviceList(){
            done = true;
            return deviceList;
        }
    }

    private static class Device {
        final int port;
        final String ip;
        final String sn;

        private Device(int port, String ip, String sn) {
            this.port = port;
            this.ip = ip;
            this.sn = sn;
        }

        @Override
        public String toString() {
            return "Device{" +
                    "port=" + port +
                    ", ip='" + ip + '\'' +
                    ", sn='" + sn + '\'' +
                    '}';
        }
    }

}
