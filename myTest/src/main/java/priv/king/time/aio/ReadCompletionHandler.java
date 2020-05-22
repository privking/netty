package priv.king.time.aio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReadCompletionHandler implements CompletionHandler<Integer,ByteBuffer> {

    private AsynchronousSocketChannel asynchronousSocketChannel;

    public ReadCompletionHandler(AsynchronousSocketChannel asynchronousSocketChannel) {
        if(this.asynchronousSocketChannel==null){
            this.asynchronousSocketChannel=asynchronousSocketChannel;
        }
    }

    @Override
    public void completed(Integer result, ByteBuffer attachment) {
        attachment.flip();
        byte[] bytes=new byte[attachment.remaining()];
        attachment.get(bytes);
        try {
            String str=new String(bytes,"utf-8");
            String returnDate="SJ".equalsIgnoreCase(str)? new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()) : "BAD ORDER";
            doWrite(returnDate);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void doWrite(String returnDate) {
        if(returnDate!=null&&returnDate.trim().length()>0){
            byte[] bytes = returnDate.getBytes();
            ByteBuffer bytebuffer = ByteBuffer.allocate(bytes.length);
            bytebuffer.put(bytes);
            bytebuffer.flip();
            asynchronousSocketChannel.write(bytebuffer, bytebuffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer byteBuffer) {
                    if(byteBuffer.hasRemaining()){
                        asynchronousSocketChannel.write(byteBuffer,byteBuffer,this);
                    }
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    try {
                        asynchronousSocketChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        try {
            this.asynchronousSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
