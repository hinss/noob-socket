package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * @author: hins
 * @created: 2020-09-08 20:53
 * @desc: UDP单播 服务提供者
 **/
public class UdpProvier {

    public static void main(String[] args) throws IOException {

        System.out.println("server.UdpProvier Started");

        // 构建DatagramSocket对象 该对象会在底层调用UDP网络层协议
        DatagramSocket udpProvier = new DatagramSocket(9999);

        // 正因为UDP不是基于连接而是基于数据包的，所以收发数据都是基于数据包
        // 构建数据包用于接收数据
        final byte[] buffer = new byte[512];
        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

        // 阻塞等到有数据包的到达
        udpProvier.receive(receivePacket);

        // 获得数据，从数据包中解析收到的数据
        String ip = receivePacket.getAddress().getHostAddress();
        int port = receivePacket.getPort();
        byte[] data = receivePacket.getData();
        int length = receivePacket.getLength();
        String receiveMsg = new String(data,0,length);
        System.out.println("[收到消息] 来自IP:" + ip + " P:" + port + " 内容:" + receiveMsg);


        //回送消息
        DatagramPacket sendPacket = new DatagramPacket(receiveMsg.getBytes(),0,receiveMsg.length());
        // 设置回送的地址
        sendPacket.setSocketAddress(new InetSocketAddress(ip,port));
        udpProvier.send(sendPacket);







    }


}
