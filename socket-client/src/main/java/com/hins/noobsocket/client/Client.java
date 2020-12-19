package com.hins.noobsocket.client;

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

        // 客户端启动控制台
        // 1.一启动就马上开始 UDP 搜索
        try {
            UdpSearcher.ServerInfo serverInfo = UdpSearcher.searchServer();
            if(serverInfo == null){
                System.out.println("no serverInfo...");
                return ;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 2.进行TCP连接


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
