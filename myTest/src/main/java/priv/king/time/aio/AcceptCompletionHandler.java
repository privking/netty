package priv.king.time.aio;


import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel,AsyncTimeServer> {

    @Override
    public void completed(AsynchronousSocketChannel result, AsyncTimeServer attachment) {
        attachment.asynchronousServerSocketChannel.accept(attachment,this);
        ByteBuffer byteBuffer=ByteBuffer.allocate(1024);
        result.read(byteBuffer,byteBuffer,new ReadCompletionHandler(result));
    }

    @Override
    public void failed(Throwable exc, AsyncTimeServer attachment) {
        exc.printStackTrace();
        attachment.countDownLatch.countDown();
    }
}
