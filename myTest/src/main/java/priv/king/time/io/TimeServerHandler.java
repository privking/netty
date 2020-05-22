package priv.king.time.io;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeServerHandler implements Runnable {

    private Socket socket;

    public TimeServerHandler(Socket socket) {
        this.socket=socket;
    }

    @Override
    public void run() {
        BufferedReader bufferedReader=null;
        PrintWriter bufferedWriter=null;
        try {
            //典型的装饰者模式   带缓冲的字符流》字符流》字节流
            bufferedReader= new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter=new PrintWriter(socket.getOutputStream(),true);
            String line =null;
            String time=null;
            while (true){
                line=bufferedReader.readLine();
                if(line==null)
                    break;
                time="SJ".equalsIgnoreCase(line)? new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()) : "BAD ORDER";
                bufferedWriter.println(time);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(bufferedReader!=null){
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(bufferedWriter!=null){
                bufferedWriter.close();
            }
            if(socket!=null){
                try {
                    socket.close();
                    this.socket=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
