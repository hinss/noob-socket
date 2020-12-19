package com.hins.noobsocket.client;

import com.hins.libary.clink.utils.ByteUtils;
import constants.UDPConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

/**
 * @author: hins
 * @created: 2020-09-08 21:02
 * @desc: UDP单播 服务搜索方
 **/
public class UdpSearcher {


    public static ServerInfo searchServer() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);
        // 1. 首先开启监听，用于监听搜索到的服务提供方的回送消息。
        Listener listener = new Listener(countDownLatch);
        listener.start();

        // 2. 然后发送搜索。
        doSendSearch();

        // 3. 当监听到回送的 TCP Server信息后返回 ServerInfo, 否则控制台就会阻塞等待。
        countDownLatch.await();
        ServerInfo serverInfo = listener.getServerInfo();
        if(serverInfo != null){
            System.out.println(serverInfo);

            return serverInfo;
        }

        return null;
    }

    private static void doSendSearch() {

        System.out.println("$UDPSearcher.doSendSearch now");

        try {
            // 发送者无须指定端口 系统会自动分配
            DatagramSocket searcher = new DatagramSocket();

            // 构建搜索的信息体,主要用于服务端校验用
            byte[] bytes = new byte[UDPConstants.HEADER.length + 2 + 4];
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            // 塞校验头部
            byteBuffer.put(UDPConstants.HEADER);
            // 塞校验命令
            byteBuffer.putShort((short)1);
            // 塞搜索方监听的固定port
            byteBuffer.putInt(UDPConstants.PORT_CLIENT_RESPONSE);

            // 构建发送数据包
            DatagramPacket sendPacket = new DatagramPacket(bytes, 0, bytes.length);
            // 设置搜索的UDP port ip为局域网地址
            sendPacket.setSocketAddress(new InetSocketAddress(InetAddress.getByName("255.255.255.255"), UDPConstants.PORT_SERVER));
            // 广播搜索
            searcher.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }

    }


    /**
     * 搜索方监听服务提供方回送信息的线程
     */
    private static class Listener extends Thread{

        private boolean done = false;
        private DatagramSocket ds = null;
        private ServerInfo serverInfo;
        private CountDownLatch countDownLatch;
        private final int minLen = UDPConstants.HEADER.length + 2 + 4;
        final byte[] buffer = new byte[128];

        public Listener(CountDownLatch countDownLatch){
            this.countDownLatch = countDownLatch;
        }


        @Override
        public void run() {

            System.out.println("UDPSearcher 开启监听....");

            try {
                ds = new DatagramSocket(UDPConstants.PORT_CLIENT_RESPONSE);

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                while(!done){

                    // 阻塞接收消息
                    ds.receive(packet);

                    // 接收到UDP SERVER 回送的
                    ByteBuffer byteBuffer = ByteBuffer.wrap(packet.getData());
                    // 取出头部
                    byte[] header = new byte[UDPConstants.HEADER.length];
                    byteBuffer.get(header, 0, UDPConstants.HEADER.length);
                    // 校验头部信息
                    boolean isValid = packet.getData().length >= minLen
                            && ByteUtils.startsWith(header, UDPConstants.HEADER);

                    if (!isValid) {
                        // 无效继续
                        continue;
                    }

                    // command
                    short cmd = byteBuffer.getShort();
                    // 拿到回送的TCP Port
                    int tcpPort = byteBuffer.getInt();

                    if(cmd != 2 || tcpPort < 0){
                        System.out.println("UDPSearcher receive cmd:" + cmd + "\tserverPort:" + tcpPort);
                        continue;
                    }

                    // 或取sn号
                    String sn = new String(buffer, minLen, packet.getLength() - minLen);

                    System.out.println("cmd: " + cmd + " tcpPort: " + tcpPort + " sn: " + sn);

                    serverInfo = new ServerInfo(sn, tcpPort);
                    countDownLatch.countDown();
                }

            } catch (Exception e) {
                e.printStackTrace();
                countDownLatch.countDown();
            } finally {
                close();
            }
        }

        void close(){
            if(ds != null){
                ds.close();;
                ds = null;
            }
        }

        ServerInfo getServerInfo(){
            return serverInfo;
        }


    }


    /**
     * 服务器信息
     */
    static class ServerInfo {

        private String sn;
        private int tcpPort;

        public ServerInfo(String sn, int tcpPort) {
            this.sn = sn;
            this.tcpPort = tcpPort;
        }

        @Override
        public String toString() {
            return "ServerInfo{" +
                    "sn='" + sn + '\'' +
                    ", tcpPort=" + tcpPort +
                    '}';
        }
    }








    public static void main(String[] args) throws IOException {

        System.out.println("UDPSearcher Started");

        // 发送者无须指定端口 系统会自动分配
        DatagramSocket searcher = new DatagramSocket();

        // 构建键盘输入消息
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        String message = input.readLine();
        byte[] bytes = message.getBytes();

        // 构建发送数据包
        DatagramPacket sendPacket = new DatagramPacket(bytes,0,bytes.length);
        // 同一本机  所以使用getLocalHost 实现单播 只给这个ip+port 发送数据包
        sendPacket.setSocketAddress(new InetSocketAddress(InetAddress.getLocalHost(),9999));

        searcher.send(sendPacket);

        // 构建接收实体
        final byte[] receiveBuffer = new byte[512];
        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer,receiveBuffer.length);
        searcher.receive(receivePacket);

        String ip = receivePacket.getAddress().getHostAddress();
        int port = receivePacket.getPort();
        byte[] data = receivePacket.getData();
        int length = receivePacket.getLength();
        String receiveMsg = new String(data,0,length);

        System.out.println("[收到回送] 来自ip:" + ip + " P: " + port + " 内容: " + receiveMsg);
    }


}
