package com.junkie.netty.websocket;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;


public class EchoClientHandler extends ChannelHandlerAdapter{

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		ctx.close();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		UserInfo[] userInfos = UserInfo();
		for (UserInfo userInfo : userInfos) {
			ctx.write(userInfo);
		}
		ctx.flush();
		System.out.println("Client send msg");
	}
	
	private UserInfo [] UserInfo(){
		UserInfo[] userInfos = new UserInfo[10];
		UserInfo useinfo = null;
		for(int i=0; i<10; i++){
			useinfo = new UserInfo();
			useinfo.setAge(i);
			useinfo.setName("ABCDEFG ---> "+i);
			userInfos[i] = useinfo;
		}
		
		return userInfos;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		System.out.println("Client receive: "+ msg);
		ctx.write(msg);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}
	
}