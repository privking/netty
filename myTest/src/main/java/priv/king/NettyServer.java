package priv.king;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * @author king
 * TIME: 2020/4/29 - 14:42
 **/
public class NettyServer {
    public static void main(String[] args) throws InterruptedException {
        //就是一个死循环，不停地检测IO事件，处理IO事件，执行任务
        //创建一个线程组:接受客户端连接   主线程
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        //创建一个线程组:接受网络操作   工作线程
        EventLoopGroup workerGroup = new NioEventLoopGroup();  //cpu核心数*2

        //是服务端的一个启动辅助类，通过给他设置一系列参数来绑定端口启动服务
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        // ，bossGroup的作用就是不断地accept到新的连接，将新的连接丢给workerGroup来处理
        serverBootstrap.group(bossGroup, workerGroup)
                //设置使用NioServerSocketChannel作为服务器通道的实现
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128) //设置线程队列中等待连接的个数
                .childOption(ChannelOption.SO_KEEPALIVE, true)//保持活动连接状态
                //表示服务器启动过程中，需要经过哪些流程，这里NettyTestHendler最终的顶层接口为ChannelHander，
                // 是netty的一大核心概念，表示数据流经过的处理器
                .handler(new NettyTestHendler())
                //表示一条新的连接进来之后，该怎么处理，也就是上面所说的，老板如何给工人配活
                //ChannelInitializer 特殊的ChannelInboundHandler
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        ChannelPipeline pipeline = nioSocketChannel.pipeline();
                        //基础固定长度分割解码器
                        // pipeline.addLast(new FixedLengthFrameDecoder(10));
                        //基于换行符的解码器（\r\n || \n）
                         pipeline.addLast(new LineBasedFrameDecoder(Integer.MAX_VALUE));
                        //基于分隔符的解码器
                        // pipeline.addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Delimiters.lineDelimiter()));
                        //基于长度的解码器
                        //int maxFrameLength:最大长度
                        //int lengthFieldOffset:长度字段起始下标
                        //int lengthFieldLength：长度字段长度
                        //int lengthAdjustment：长度补偿
                        //int initialBytesToStrip：抛弃的长度
                        //boolean failFast:快速失败
                        //在大多数情况下，length字段只表示消息体的长度
                        // 但是，在某些协议中，长度字段表示整个消息的长度，包括消息头。
                        // 在这种情况下，我们指定一个非零长度调整。
                        //pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,2,0,2,true));
                        pipeline.addLast(new StringDecoder());
                        pipeline.addLast(new NettyServerHendler());

                    }
                });
        System.out.println(".........server  init..........");
        // 这里就是真正的启动过程了，绑定9090端口，等待服务器启动完毕，才会进入下行代码
        ChannelFuture future = serverBootstrap.bind(9090).sync();
        System.out.println(".........server start..........");
        //等待服务端关闭socket
        future.channel().closeFuture().sync();

        // 关闭两组死循环
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
