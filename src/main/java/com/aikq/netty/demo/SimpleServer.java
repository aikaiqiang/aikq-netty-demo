package com.aikq.netty.demo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import java.net.InetAddress;
import java.nio.charset.Charset;

/**
 *
 * @author aikq
 * @date 2018年11月16日 14:38
 */
public final class SimpleServer {

	public static void main(String[] args) {
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup wokerGroup = new NioEventLoopGroup();

		try{
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, wokerGroup)
					.channel(NioServerSocketChannel.class)
//					.handler(new SmipleServerHandler())
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel socketChannel) throws Exception {
							ChannelPipeline pipeline = socketChannel.pipeline();
							// 以("\n")为结尾分割的 解码器
							pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));

							pipeline.addLast(new StringDecoder(Charset.forName("UTF-8")));
							pipeline.addLast(new StringEncoder(Charset.forName("UTF-8")));

							// 自己的逻辑Handler
							pipeline.addLast(new HelloServerHandler());
						}
					});

			bootstrap.option(ChannelOption.SO_BACKLOG, 128);
			bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
			ChannelFuture future = bootstrap.bind(8888).sync();
			if (future.isSuccess()){
				System.out.println("服务端正常启动。。。");
			}
			future.channel().closeFuture().sync();

		}catch (Exception e){

		}finally {
			bossGroup.shutdownGracefully();
			wokerGroup.shutdownGracefully();
		}

	}


	private static class SmipleServerHandler extends ChannelInboundHandlerAdapter{

		@Override
		public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
			System.out.println("channel register");
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			System.out.println("RamoteAddress : " + ctx.channel().remoteAddress() + " active !");
			ctx.writeAndFlush( "Welcome to " + InetAddress.getLocalHost().getHostName() + " service!\n");
			super.channelActive(ctx);
		}

		@Override
		public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
			System.out.println("handler add");
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			// 处理消息
			try {
				ByteBuf in = (ByteBuf) msg;
				System.out.println("服务端接收到来至客户端的消息：" + in.toString(CharsetUtil.UTF_8));
			}catch (Exception e){

			}finally {
				ReferenceCountUtil.release(msg);
			}

//			System.out.println(msg);
//			Message message = JSON.parseObject(msg+"", Message.class);
//			System.out.println("接收到消息："+message);
//			String clientId = message.getClientId();
//			if(MsgType.LOGIN.equals(message.getType())){
//				System.out.printf("将%s添加到队列\n",clientId);
//			}

		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			// 发生异常时调用
			System.out.println("netty 发生异常：" + cause.getMessage());
			ctx.close();
		}

	}


	private static class HelloServerHandler extends SimpleChannelInboundHandler<String> {

		@Override
		protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
			// 收到消息直接打印输出
			System.out.println("客户端-" + channelHandlerContext.channel().remoteAddress() + " Say : " + s);

			if ("exit".equals(s)){
//				channelHandlerContext.writeAndFlush("Received your close command ! Have handle!");
				channelHandlerContext.close();
			}

			// 返回客户端消息 - 我已经接收到了你的消息
//			channelHandlerContext.writeAndFlush("Received your message ! you send:" + s + "\n");

		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			System.out.println("RamoteAddress : " + ctx.channel().remoteAddress() + " active !");
			ctx.writeAndFlush( "Welcome to " + InetAddress.getLocalHost().getHostName() + " service!\n");
			super.channelInactive(ctx);
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			ctx.writeAndFlush( "Thinks to " + InetAddress.getLocalHost().getHostName() + " service!\n");
			super.channelInactive(ctx);
		}

		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			ctx.writeAndFlush("Received your message ! \n");
			super.channelReadComplete(ctx);
		}
	}

}
