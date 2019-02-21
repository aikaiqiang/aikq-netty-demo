package com.aikq.netty.heart;

import com.aikq.netty.demo.SimpleClient;
import com.aikq.netty.heart.handler.ClientHeartBeatHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 *  客户端实现
 * @author aikq
 * @date 2019年02月21日 14:43
 */
public class HeartClient {
	private String host;
	private int port;

	public HeartClient(String host, int port) {
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
					//					.option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel socketChannel) throws Exception {
							ChannelPipeline pipeline = socketChannel.pipeline();
//							pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
//							pipeline.addLast(new StringEncoder(Charset.forName("UTF-8")));
//							pipeline.addLast(new StringDecoder(Charset.forName("UTF-8")));
							pipeline.addLast(new IdleStateHandler(0, 4 , 0, TimeUnit.SECONDS));
							pipeline.addLast(new ClientHeartBeatHandler());
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
}
