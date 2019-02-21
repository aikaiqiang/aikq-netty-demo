package com.aikq.netty.heart.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 *  客户端心跳处理
 * @author aikq
 * @date 2019年02月21日 14:45
 */
public class ClientHeartBeatHandler extends ChannelInboundHandlerAdapter {
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

		if(evt instanceof IdleStateEvent){
			IdleStateEvent event = (IdleStateEvent)evt;
			if(event.state().equals(IdleState.WRITER_IDLE)){
			}
		}

		super.userEventTriggered(ctx, evt);
	}
}
