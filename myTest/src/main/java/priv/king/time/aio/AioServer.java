package priv.king.time.aio;


public class AioServer {
    public static void main(String[] args) {
        int port=8080;
        if(args!=null&&args.length>0){
            try {
                port=Integer.valueOf(args[0]);
            }catch (Exception e){

            }
        }
        AsyncTimeServer asyncTimeServer=new AsyncTimeServer(port);
        new Thread(asyncTimeServer).start();
    }
}
