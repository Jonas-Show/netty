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
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

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
						//将请求或应答消息编码或解码成http消息
						pipeLine.addLast("http-codec", new HttpServerCodec());
						//将http消息的多个部分组合成一个完整的http消息
						pipeLine.addLast("aggregator", new HttpObjectAggregator(65536));
						//发送html5文件和支持websocket通信
						pipeLine.addLast("http-chunked",new ChunkedWriteHandler());
						//添加自己的处理逻辑
						pipeLine.addLast(new WebSocketServerHandler());
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
		new WebSocketServer().bind(8080);
	}
}
