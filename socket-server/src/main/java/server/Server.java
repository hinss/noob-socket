package server;

import constants.TCPConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author: hins
 * @created: 2020-09-03 23:46
 * @desc:
 **/
public class Server {


    public static void main(String[] args) throws IOException {

        // 1.启动台启动 udp server
        UdpProvier.start(TCPConstants.PORT_SERVER);



    }

    private static class ClientHandler extends Thread{
        private Socket client;
        private boolean isClosed;
        private BufferedReader bufferedReader;
        private PrintStream printStream;

        private ClientHandler(Socket client) throws IOException {
            this.client = client;
            this.bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            this.printStream = new PrintStream(client.getOutputStream());
        }

            @Override
            public void run() {
                System.out.println("[获得客户端连接] IP:" + client.getInetAddress().getHostAddress() + " PORT:" + client.getPort());

                try {
                    do {

                        String message = bufferedReader.readLine();
                        if(message.equalsIgnoreCase("bye")){
                            // 退出
                            printStream.println("bye");
                            isClosed = true;
                        }else{
                            System.out.println("[收到消息] 客户端IP: " + client.getInetAddress().getHostAddress() + "P:"+client.getPort()+ "的消息:" + message);
                            // 回送给客户端消息
                            printStream.println(message);
                        }
                    }while(!isClosed);

                    bufferedReader.close();
                    printStream.close();

                }catch (Exception e){
                    e.printStackTrace();
                    System.out.println("[异常关闭]");
                } finally {

                    try {
                        client.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                System.out.println("[客户端退出] IP: " + client.getInetAddress() + " P:" + client.getPort());

            }
        }
}


