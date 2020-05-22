package priv.king.time.aio;


public class AioClient {
    public static void main(String[] args) {
        int port=8080;
        if(args!=null&&args.length>0){
            try {
                port=Integer.valueOf(args[0]);
            }catch (Exception e){

            }
        }
        AsyncTimeClientHandler asyncTimeClientHandler=new AsyncTimeClientHandler("127.0.0.1",port);
        new Thread(asyncTimeClientHandler).start();
    }
}
