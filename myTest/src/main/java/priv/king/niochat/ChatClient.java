package priv.king.niochat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class ChatClient implements  Runnable{

    private SocketChannel socketChannel;

    private Selector selector;

    public ChatClient(){
        try {
            //得到一个网络通道
            //SelectorProvider.provider().openSocketChannel();
            socketChannel=SocketChannel.open();
            //SelectorProvider.provider().openSelector();
            selector=Selector.open();
            //设置非阻塞式
            socketChannel.configureBlocking(false);
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    public void doCon(){
        //提供服务器ip与端口
        InetSocketAddress inetSocketAddress=new InetSocketAddress("127.0.0.1",9090);
        //连接服务器端
        try {
            /*
            如果此通道处于非阻塞模式，则此方法的调用将启动非阻塞连接操作。
            如果立即建立连接，就像本地连接一样，那么此方法将返回true。
            否则，此方法将返回false，并且连接操作稍后必须通过调用finishConnect方法来完成。
            如果此通道处于阻塞模式，则此方法的调用将阻塞，直到建立连接或发生I/O错误。
             */
            if(socketChannel.connect(inetSocketAddress)){
                socketChannel.register(selector,SelectionKey.OP_READ);
                //写数据
                writeData(socketChannel);
            }else{
                socketChannel.register(selector, SelectionKey.OP_CONNECT);//如果连接不上
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 我们给匿名内部类传递参数的时候，若该形参在内部类中需要被使用，那么该形参必须要为final。
     * 也就是说：当所在的方法的形参需要被内部类里面使用时，该形参必须为final。
     *
     * @param socketChannel
     * @throws IOException
     */
    public void writeData(final SocketChannel socketChannel) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true){
                        Scanner scanner=new Scanner(System.in);
                        String str = scanner.nextLine();
                        if(str.equals("by")){
                            socketChannel.close();
                            return;
                        }
                        ByteBuffer byteBuffer=ByteBuffer.wrap((socketChannel.getLocalAddress().toString()+"说："+str).getBytes());
                        socketChannel.write(byteBuffer);

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void readData() throws IOException {
        ByteBuffer byteBuffer=ByteBuffer.allocate(1024);
        int read = socketChannel.read(byteBuffer);
        if(read>0){
            byte[] array = byteBuffer.array();
            System.out.println(new String(array,"utf-8"));
        }
    }


    public static void main(String[] args) throws IOException {
        new Thread(new ChatClient()).start();
    }

    @Override
    public void run() {
        doCon();
        try {
            while (true){
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()){
                    SelectionKey selectionKey = iterator.next();
                    if(selectionKey.isValid()){
                        if(selectionKey.isConnectable()){
                            SocketChannel channel = (SocketChannel) selectionKey.channel();
                            if (channel.finishConnect()){
                                channel.register(selector,SelectionKey.OP_READ);
                                System.out.println("bbbbbbbbbbbbb");
                                //写数据
                                writeData(channel);
                            }else{
                                System.exit(1);
                            }
                        }
                        if(selectionKey.isReadable()){
                            readData();
                        }
                    }
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
