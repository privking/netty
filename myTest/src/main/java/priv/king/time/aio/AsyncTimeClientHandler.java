package priv.king.time.aio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

public class AsyncTimeClientHandler implements CompletionHandler<Void, AsyncTimeClientHandler>, Runnable {

    private AsynchronousSocketChannel asynchronousSocketChannel;
    private CountDownLatch countDownLatch;
    private String ip;
    private int port;

    public AsyncTimeClientHandler(String ip, int port) {
        this.ip = ip;
        this.port = port;
        try {
            asynchronousSocketChannel = AsynchronousSocketChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        countDownLatch = new CountDownLatch(1);
        asynchronousSocketChannel.connect(new InetSocketAddress(ip, port), this, this);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            asynchronousSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void completed(Void result, AsyncTimeClientHandler attachment) {
        String returnDate = "SJ";
        if (returnDate != null && returnDate.trim().length() > 0) {
            byte[] bytes = returnDate.getBytes();
            ByteBuffer bytebuffer = ByteBuffer.allocate(bytes.length);
            bytebuffer.put(bytes);
            bytebuffer.flip();
            asynchronousSocketChannel.write(bytebuffer, bytebuffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer byteBuffer) {
                    if (byteBuffer.hasRemaining()) {
                        asynchronousSocketChannel.write(byteBuffer, byteBuffer, this);
                    } else {
                        ByteBuffer byteBuffer1 = ByteBuffer.allocate(1024);
                        asynchronousSocketChannel.read(byteBuffer1, byteBuffer1, new CompletionHandler<Integer, ByteBuffer>() {
                            @Override
                            public void completed(Integer result, ByteBuffer attachment) {
                                attachment.flip();
                                byte[] bytes1 = new byte[attachment.remaining()];
                                attachment.get(bytes1);
                                String str = null;
                                try {
                                    str = new String(bytes1, "utf-8");
                                    System.out.println(str);
                                    countDownLatch.countDown();
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void failed(Throwable exc, ByteBuffer attachment) {
                                try {
                                    asynchronousSocketChannel.close();
                                    countDownLatch.countDown();
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
                        asynchronousSocketChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void failed(Throwable exc, AsyncTimeClientHandler attachment) {
        exc.printStackTrace();
        countDownLatch.countDown();
        try {
            asynchronousSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
