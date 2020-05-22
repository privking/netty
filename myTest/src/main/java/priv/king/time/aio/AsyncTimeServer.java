package priv.king.time.aio;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;

public class AsyncTimeServer implements Runnable{

     int port;
     CountDownLatch countDownLatch;
     AsynchronousServerSocketChannel asynchronousServerSocketChannel;

    public AsyncTimeServer(int port) {
        this.port=port;
        try {
            asynchronousServerSocketChannel=AsynchronousServerSocketChannel.open();
            asynchronousServerSocketChannel.bind(new InetSocketAddress(port));
        }catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void run() {
        countDownLatch=new CountDownLatch(1);
        doAccept();
        try {
            countDownLatch.await();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void doAccept() {
        asynchronousServerSocketChannel.accept(this,new AcceptCompletionHandler());
    }
}
