package priv.king.time.nio;

import java.io.IOException;

public class NioServer {
    public static void main(String[] args) throws IOException {
        int port=8080;
        if(args!=null&&args.length>0){
            try {
                port=Integer.valueOf(args[0]);
            }catch (Exception e){

            }
        }
        MultiplexerTimeServer multiplexerTimeServer=new MultiplexerTimeServer(port);
        new Thread(multiplexerTimeServer).start();
    }
}
