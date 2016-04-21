package com.junkie.netty.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

public class WebSocketServer {
	public void bind(int port) throws InterruptedException{
		
		ServerBootstrap b = new ServerBootstrap();
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup childGroup = new NioEventLoopGroup();
		try {
			b.group(bossGroup, childGroup)
				.channel(NioServerSocketChannel.class)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
				.option(ChannelOption.TCP_NODELAY, true)
				.childHandler(new ChannelInitializer<SocketChannel>() {

					@Override
					protected void initChannel(SocketChannel ch)
							throws Exception {
						ChannelPipeline pipeLine = ch.pipeline();
						//解决粘包解码器
						pipeLine.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1500, 0, 2, 0, 2));
						//添加MessagePack的编解码器
						pipeLine.addLast("msgpack decoder", new MsgpackDecoder());
						
						//解决粘包的编码器
						pipeLine.addLast("frameEncoder", new LengthFieldPrepender(2));
						pipeLine.addLast("msgpack encoder", new MsgpackEncoder());
						//添加自己的处理逻辑
						pipeLine.addLast(new EchoServerHandler());
					}
				})
				;
			//绑定端口，同步等待成功
			ChannelFuture f = b.bind(port).sync();
			
			//等待服务器监听端口关闭
			f.channel().closeFuture().sync();
		} finally{
			bossGroup.shutdownGracefully();
			childGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws InterruptedException {
		new EchoServer().bind(8080);
	}
}
