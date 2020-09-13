import constants.UDPConstants;
import udplib.MessageCreator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.UUID;

/**
 * @author: hins
 * @created: 2020-09-12 23:01
 * @desc: Udp组播服务提供方
 **/
public class UdpMulticastProvider {

    public static void main(String[] args) throws IOException {

        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn);
        provider.start();

        System.in.read();
        provider.exit();

    }

    private static class Provider extends Thread{
        // 设备号
        private final String sn;
        private boolean flag = false;
        private DatagramSocket datagramSocket = null;

        private Provider(String sn) {
            this.sn = sn;
        }

        @Override
        public void run() {

            System.out.println("UDPProvider Started");

            // 每一个能够提供服务的提供方必须都有约定好的端口
            try {
                datagramSocket = new DatagramSocket(UDPConstants.PORT_SERVER);


                while(!flag){

                    final byte[] buffer = new byte[512];
                    DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                    datagramSocket.receive(receivePacket);

                    // 获得数据，从数据包中解析收到的数据
                    String ip = receivePacket.getAddress().getHostAddress();
                    int port = receivePacket.getPort();
                    byte[] data = receivePacket.getData();
                    int length = receivePacket.getLength();
                    String receiveMsg = new String(data,0,length);

                    // 从消息中解析搜索方监听的服务端口
                    int listenPort = MessageCreator.parsePort(receiveMsg);
                    String buildWithSN = MessageCreator.buildWithSN(sn);

                    //回送消息
                    DatagramPacket sendPacket = new DatagramPacket(buildWithSN.getBytes(),
                            0,
                            buildWithSN.length(),
                            receivePacket.getAddress(),
                            listenPort);

                    datagramSocket.send(sendPacket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close();
            }

            //完成
            System.out.println("UDPProvider Finished.");
            close();
        }

        public void close(){
            if(datagramSocket != null){
                datagramSocket.close();
                datagramSocket = null;
            }
        }

        public void exit(){
            flag = true;
            close();
        }
    }

}
