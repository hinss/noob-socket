package server;

import server.handler.ClientHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author: hins
 * @created: 2020-09-29 17:32
 * @desc: Tcp服务端
 **/
public class TCPServer {

    private final int port;


    public TCPServer(int port){
        this.port = port;
    }

    public void start() throws IOException {

        // 1.TCP服务端启动客户端监听线程ClientListener
        ClientListener clientListener = new ClientListener(port);
        clientListener.start();

    }


    /**
     * TcpServer 客户端连接监听器
     */
    private class ClientListener extends Thread{

        private ServerSocket serverSocket;
        private boolean done;

        public ClientListener(int tcpPort) throws IOException {

            // 监听TCP端口
            serverSocket = new ServerSocket(tcpPort);
            System.out.println("服务端信息:" + serverSocket.getInetAddress() + " P:" + serverSocket.getLocalPort());
        }

        @Override
        public void run() {

            // 循环阻塞监听客户端连接
            while(!done){
                Socket socket = null;
                try {
                    socket = serverSocket.accept();

                    // 得到客户端连接 构建ClientHandler
                    ClientHandler clientHandler = new ClientHandler(socket);
                    clientHandler.startToRead();

                } catch (IOException e) {
                    // 客户端连接发生异常 继续监听
                    continue;
                }
            }
        }
    }









}
