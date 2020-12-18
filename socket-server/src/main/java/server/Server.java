package server;

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

        // 启动ServerSocket 并且制定端口8888
        ServerSocket serverSocket = new ServerSocket(8888);

        System.out.println("[服务端启动] ServerAddress:" + serverSocket.getInetAddress() + " Port:" + serverSocket.getLocalPort());

        for(;;){

            // 循环 阻塞监听连接
            Socket client = serverSocket.accept();

            // 获得连接 开启线程处理连接 不阻塞主线程
            new ClientHandler(client).start();
        }
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


