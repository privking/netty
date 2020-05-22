package priv.king.time.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

public class MultiplexerTimeServer implements Runnable {

    private Selector selector;

    private ServerSocketChannel serverSocketChannel;

    private volatile boolean stop;

    public MultiplexerTimeServer(int port) throws IOException {
        selector=Selector.open();
        serverSocketChannel=ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(port),1024);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }


    public void stop(){
        stop=true;
    }

    @Override
    public void run() {
        while (!stop){
            try {
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                SelectionKey key=null;
                while (iterator.hasNext()){
                    key= iterator.next();
                     iterator.remove();
                     try {
                         handlerInput(key);
                     }catch (Exception e){
                        if(key!=null){
                            key.cancel();
                            if(key.channel()!=null){
                                key.channel().close();
                            }
                        }
                     }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void handlerInput(SelectionKey key) throws IOException {
        if(key.isValid()){
            if(key.isAcceptable()){
                ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                SocketChannel socketChannel = channel.accept();
                socketChannel.configureBlocking(false);
                socketChannel.register(selector,SelectionKey.OP_READ);
            }
            if(key.isReadable()){
                SocketChannel socketChannel = (SocketChannel) key.channel();
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                int readNumber = socketChannel.read(byteBuffer);
                if(readNumber>0){
                    byteBuffer.flip();
                    byte[] bytes=new byte[byteBuffer.remaining()];
                    byteBuffer.get(bytes);
                    String body = new String(bytes, "utf-8");
                    String returnDate="SJ".equalsIgnoreCase(body)? new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()) : "BAD ORDER";
                    doWrite(socketChannel,returnDate);
                }else if(readNumber<0){
                    key.cancel();
                    socketChannel.close();
                }
            }
        }
    }

    private void doWrite(SocketChannel socketChannel, String returnDate) throws IOException {
        if(returnDate!=null&&returnDate.trim().length()>0){
            byte[] bytes = returnDate.getBytes();
            ByteBuffer bytebuffer = ByteBuffer.allocate(bytes.length);
            bytebuffer.put(bytes);
            bytebuffer.flip();
            socketChannel.write(bytebuffer);
        }
    }
}
