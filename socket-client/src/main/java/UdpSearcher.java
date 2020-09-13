import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;

/**
 * @author: hins
 * @created: 2020-09-08 21:02
 * @desc: UDP单播 服务搜索方
 **/
public class UdpSearcher {

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
