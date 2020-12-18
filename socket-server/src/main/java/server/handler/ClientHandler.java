package server.handler;

import java.io.*;
import java.net.Socket;

/**
 * @author: hins
 * @created: 2020-09-29 18:08
 * @desc: 客户端连接handler
 **/
public class ClientHandler {

    private final Socket socket;
    private final ClientReadHandler clientReadHandler;
    private final ClientWriteHandler clientWriteHandler;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.clientReadHandler = new ClientReadHandler(socket);
        this.clientWriteHandler = new ClientWriteHandler(socket);

        System.out.println("新客户端连接：" + socket.getInetAddress() +
                " P:" + socket.getPort());
    }

    /**
     * 启动客户端读取数据线程
     */
    public void startToRead(){
        clientReadHandler.start();
    }

    /**
     * 读取线程
     */
    public class ClientReadHandler extends Thread{

        /** 客户端的输入流 */
        private final InputStream inputStream;
        private boolean done = false;

        public ClientReadHandler(Socket client) throws IOException {
            this.inputStream = client.getInputStream();
        }

        @Override
        public void run() {

            // TODO 处理客户端连接 读取数据逻辑

            // BufferedReader 用于从客户端中读取数据
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            while(!done){

                try {
                    String msg = bufferedReader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }


        }
    }

    /**
     * 输出线程
     */
    public class ClientWriteHandler extends Thread{

        private final OutputStream outputStream;
        private boolean done = false;

        public ClientWriteHandler(Socket client) throws IOException {
            this.outputStream = client.getOutputStream();
        }

        @Override
        public void run() {
            // TODO 处理客户端连接 发送数据逻辑
        }
    }


}
