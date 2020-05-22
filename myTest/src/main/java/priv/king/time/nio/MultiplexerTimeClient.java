package priv.king.time.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class MultiplexerTimeClient implements Runnable{

    private String ip;
    private int port;
    private Selector selector;
    private SocketChannel socketChannel;
    private volatile boolean stop;

    public MultiplexerTimeClient(String ip, int port) throws IOException {
        this.ip = ip==null ? "127.0.0.1" : ip;
        this.port = port;
        selector=Selector.open();
        socketChannel=SocketChannel.open();
        socketChannel.configureBlocking(false);
    }

    @Override
    public void run() {
        try {
            doConnect();
        }catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        while (!stop){
            try {
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
                SelectionKey key=null;
                while (keyIterator.hasNext()){
                     key = keyIterator.next();
                     keyIterator.remove();
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
                System.exit(1);
            }
        }
        if(selector!=null){
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handlerInput(SelectionKey key) throws IOException {
        if(key.isValid()){
            if(key.isConnectable()){
                SocketChannel socketChannel = (SocketChannel) key.channel();
                if(socketChannel.finishConnect()){
                    socketChannel.register(selector,SelectionKey.OP_READ);
                    doWrite(socketChannel);
                }else{
                    System.exit(1);
                }
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
                    System.out.println(body);
                    stop=true;
                }else if(readNumber<0){
                    key.cancel();
                    socketChannel.close();
                }
            }
        }
    }

    private void doWrite(SocketChannel socketChannel) throws IOException {
        String returnDate="SJ";
        if(returnDate!=null&&returnDate.trim().length()>0){
            byte[] bytes = returnDate.getBytes();
            ByteBuffer bytebuffer = ByteBuffer.allocate(bytes.length);
            bytebuffer.put(bytes);
            bytebuffer.flip();
            socketChannel.write(bytebuffer);
        }
    }

    private void doConnect() throws IOException {
        if(socketChannel.connect(new InetSocketAddress(ip,port))){
            socketChannel.register(selector,SelectionKey.OP_READ);
            doWrite(socketChannel);
        }else{
            socketChannel.register(selector,SelectionKey.OP_CONNECT);
        }
    }
}
