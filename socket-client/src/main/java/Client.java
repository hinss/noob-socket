import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author: hins
 * @created: 2020-09-03 23:46
 * @desc:
 **/
public class Client {

    private static boolean flag = false;

    public static void main(String[] args) throws IOException {

        // 构建客户端连接socket对象
        Socket client = new Socket();
        // 设置连接超时时间
        client.setSoTimeout(3000);

        // 绑定连接服务端的ip 端口
        client.connect(new InetSocketAddress("127.0.0.1",8888));

        requestAndResponse(client);

        client.close();
    }

    private static void requestAndResponse(Socket client) throws IOException {

        // 模拟键盘输入发送消息
        InputStream inputStream = System.in;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        OutputStream outputStream;
        BufferedReader input;
        try {
            // 从socket获取输入输出流
            outputStream = client.getOutputStream();
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));

            PrintStream output = new PrintStream(outputStream);

            do{
                // 阻塞获得键盘输入的消息
                String typeInMsg = bufferedReader.readLine();
                // 发送消息到服务端
                output.println(typeInMsg);

                //从服务端接收一条消息
                String receiveMsg = input.readLine();
                if(!receiveMsg.equalsIgnoreCase("bye")){
                    System.out.println("[服务端回送消息]: " + receiveMsg);
                }else{
                    flag = true;
                }
            }while(!flag);

            inputStream.close();
            input.close();
            outputStream.close();
        } catch (IOException e) {
            System.out.println("异常关闭");
        }
    }


}
