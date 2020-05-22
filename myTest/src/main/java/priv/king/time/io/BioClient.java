package priv.king.time.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class BioClient {
    public static void main(String[] args) throws IOException {
        int port=8080;
        if(args!=null&&args.length>0){
            try {
                port=Integer.valueOf(args[0]);
            }catch (Exception e){
                
            }
        }
        Socket socket=null;
        BufferedReader bufferedReader=null;
        PrintWriter printWriter=null;
        try {
            socket=new Socket("127.0.0.1",port);
            while (true){
                bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                printWriter=new PrintWriter(socket.getOutputStream(),true);
                printWriter.println("SJ");
                String readLine = bufferedReader.readLine();
                System.out.println(readLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(socket!=null){
                socket.close();
            }
            if(bufferedReader!=null){
                bufferedReader.close();
            }
            if(printWriter!=null){
                printWriter.close();
            }
        }

    }
}
