package com.aikq.netty.demo;

import com.aikq.netty.demo.message.Message;
import com.aikq.netty.demo.message.MsgType;
import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 *  客户端
 * @author aikq
 * @date 2018年11月16日 15:58
 */
public class SimpleClient {

	private String host;
	private int port;

	public SimpleClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	private void start(){
		EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
		try{
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(eventLoopGroup)
					.channel(NioSocketChannel.class)
					.remoteAddress(host,port)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel socketChannel) throws Exception {
							ChannelPipeline pipeline = socketChannel.pipeline();
							pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
							pipeline.addLast(new StringEncoder(Charset.forName("UTF-8")));
							pipeline.addLast(new StringDecoder(Charset.forName("UTF-8")));
							pipeline.addLast(new HelloClientHandler());
						}
					});
			ChannelFuture future = bootstrap.connect(host, port).sync();
			if (!future.isSuccess()){
				start();
			}

			// 控制台输入消息
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			Channel ch = future.channel();
			for (;;) {
				String line = in.readLine();
				if (line == null) {
					continue;
				}
				/*
				 * 向服务端发送在控制台输入的文本 并用"\r\n"结尾
				 * 之所以用\r\n结尾 是因为我们在handler中添加了 DelimiterBasedFrameDecoder 帧解码。
				 * 这个解码器是一个根据\n符号位分隔符的解码器。所以每条消息的最后必须加上\n否则无法识别和解码
				 * */
				if ("mekill".equals(line)){
					ch.close();
				}
				ch.writeAndFlush(line + "\r\n");

			}

		}catch (Exception e){

		}finally {
			eventLoopGroup.shutdownGracefully();
		}

	}

	public static void main(String[] args) {
		SimpleClient client = new SimpleClient("127.0.0.1", 8888);
		client.start();

	}

	private static class ClientHandler extends ChannelInboundHandlerAdapter {

		@Override
		public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
			super.userEventTriggered(ctx, evt);

			if (evt instanceof IdleStateEvent) {
				IdleStateEvent e = (IdleStateEvent) evt;
				switch (e.state()) {
					case WRITER_IDLE:
						Message pingMsg = new Message(MsgType.PING);
						ctx.writeAndFlush(JSON.toJSON(pingMsg));
						System.out.println("send ping to server----------");
						break;
					default:
						break;
				}
			}
		}


		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			Message message = JSON.parseObject(msg+"", Message.class);
			MsgType msgType=message.getType();
			switch (msgType){
				case LOGIN:{
					//向服务器发起登录
					message = new Message(MsgType.LOGIN);
					ctx.writeAndFlush(JSON.toJSONString(message));
				}break;
				case PING:{
					System.out.println("receive ping from server----------");
				}break;
				case SEND:{
					//收到服务端消息
					System.out.println("收到服务端消息："+message.getData());
				}break;
				case NO_TARGET:{
					//收到服务端消息
					System.out.println("找不到targetId:"+message.getTargetId());
				}break;
				default:break;
			}
		}
	}




	private static class HelloClientHandler extends SimpleChannelInboundHandler<String>{


		@Override
		protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
			// 接受服务端信息
			System.out.println("服务端响应-Server say : " + s);
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			System.out.println("client connect success!");
			super.channelActive(ctx);
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			System.out.println("client close connection success!");
			super.channelInactive(ctx);
		}
	}
}
