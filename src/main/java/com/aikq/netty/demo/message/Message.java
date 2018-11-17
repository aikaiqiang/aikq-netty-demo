package com.aikq.netty.demo.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 *
 * @author aikq
 * @date 2018年11月16日 17:19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable {

	/**
	 * 发送者客户端ID
	 */
	private String clientId;
	/**
	 * 消息类型
	 */
	private MsgType type;
	/**
	 * 数据
	 */
	private String data;
	/**
	 * 目标客户端ID
	 */
	private String targetId;

	public Message(MsgType type) {
		this.type = type;
	}
}
