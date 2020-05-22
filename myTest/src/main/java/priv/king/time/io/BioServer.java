package priv.king.time.io;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BioServer {
    public static void main(String[] args) throws IOException {
        int port=8080;
        if(args!=null&&args.length>0){
            try {
                port=Integer.valueOf(args[0]);
            }catch (Exception e){

            }
        }
        ServerSocket serverSocket=null;
        try {
            serverSocket=new ServerSocket(port);
            TimeServerHandlerExecutorPool pool = new TimeServerHandlerExecutorPool(50, 1000);
            while (true){
                Socket socket = serverSocket.accept();
//                new Thread(new TimeServerHandler(socket)).start();
                pool.execute(new TimeServerHandler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(serverSocket!=null){
                serverSocket.close();
            }
        }

    }
}
