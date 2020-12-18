package server;

import com.hins.libary.clink.utils.ByteUtils;
import constants.UDPConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * @author: hins
 * @created: 2020-09-08 20:53
 * @desc: UDP单播 服务提供者
 **/
public class UdpProvier {

    // 单例的Provider对象 提供UDP socket相关
    private static Provider PROVIDER_INSTANCE;

    public static void start(int tcpPort){
        // 如果Server重启先停掉UDP Provier
        stop();
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn, tcpPort);
        provider.start();

        PROVIDER_INSTANCE = provider;

    }

    static void stop(){
        if(PROVIDER_INSTANCE != null){
            PROVIDER_INSTANCE.exit();
            PROVIDER_INSTANCE = null;
        }
    }


    private static class Provider extends Thread{
        // 唯一设备号
        private final byte[] sn;
        // TCP 端口号
        private int port;
        // UDP socket对象
        private DatagramSocket ds = null;

        private boolean done = false;
        // 存储消息的buffer
        final byte[] buffer = new byte[128];


        Provider(String sn, int port){
            this.sn = sn.getBytes();
            this.port = port;
        }

        @Override
        public void run() {

            System.out.println("server.UdpProvier Started");

            try {
                // 构建DatagramSocket对象 该对象会在底层调用UDP网络层协议
                ds = new DatagramSocket(UDPConstants.PORT_SERVER);

                // 正因为UDP不是基于连接而是基于数据包的，所以收发数据都是基于数据包
                // 构建数据包用于接收数据
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

                // 死循环接收消息 除非接收到停止的指令
                while(!done){

                    // 阻塞等到有数据包的到达
                    ds.receive(receivePacket);

                    // 获得数据，从数据包中解析收到的数据
                    // 搜索者的ip
                    String searcherIp = receivePacket.getAddress().getHostAddress();
                    // 搜索服务者的port
                    int searcherPort = receivePacket.getPort();
                    // 搜索方发送的data
                    byte[] clientData = receivePacket.getData();
                    // data长度
                    int clientDataLen = receivePacket.getLength();
                    // 构建成String类型的消息
                    String receiveMsg = new String(clientData,0, clientDataLen);
                    System.out.println("[收到消息] 来自IP:" + searcherIp + " P:" + searcherPort + " 内容:" + receiveMsg);

                    // 校验搜索方 头部信息 不合法者不会给他返回 TCP的 Port.
                    boolean isValid = clientDataLen >= (UDPConstants.HEADER.length + 2 + 4)
                            && ByteUtils.startsWith(clientData, UDPConstants.HEADER);

                    if(!isValid){
                        continue;
                    }

                    // 解析命令与回送端口
                    int index = UDPConstants.HEADER.length;
                    short cmd = (short) ((clientData[index++] << 8) | (clientData[index++] & 0xff));
                    int responsePort = (((clientData[index++]) << 24) |
                            ((clientData[index++] & 0xff) << 16) |
                            ((clientData[index++] & 0xff) << 8) |
                            ((clientData[index] & 0xff)));

                    // 构建一份回送数据
                    if(cmd == 1 && responsePort > 0){

                        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                        // 首先塞进去 UDP Header用于校验合法
                        byteBuffer.put(UDPConstants.HEADER);
                        // 塞确认cmd
                        byteBuffer.putShort((short)2);
                        // 塞TCP端口
                        byteBuffer.putInt(port);
                        // 塞sn
                        byteBuffer.put(sn);

                        int len = byteBuffer.position();
                        System.out.println("ByteBuffer对象的position位置: " + len);
                        System.out.println("Buffer数组的len: " + buffer.length);

                        DatagramPacket resPacket = new DatagramPacket(buffer, len, receivePacket.getAddress(), responsePort);
                        ds.send(resPacket);

                        System.out.println("UDPProvier 收到client 搜索并且成功回送!");

                    } else{
                        System.out.println("UDPProvier 不支持该指令: " + cmd + "与端口: " + responsePort);
                    }

                }

            } catch (IOException ignore) {

            } finally {
                close();
            }
        }

        private void close() {
            if(ds != null){
                ds.close();
                // 将UDP socket对象设置为null
                ds = null;
            }
        }

        /**
         * 提供退出方法 用于停掉线程内while循环
         */
        void exit(){

            done = true;
            close();

        }

    }

}
