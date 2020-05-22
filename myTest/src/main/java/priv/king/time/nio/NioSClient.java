package priv.king.time.nio;

import java.io.IOException;

public class NioSClient {
    public static void main(String[] args) throws IOException {
        int port=8080;
        if(args!=null&&args.length>0){
            try {
                port=Integer.valueOf(args[0]);
            }catch (Exception e){

            }
        }
        MultiplexerTimeClient multiplexerTimeCient=new MultiplexerTimeClient("127.0.0.1",port);
        new Thread(multiplexerTimeCient).start();
    }
}
