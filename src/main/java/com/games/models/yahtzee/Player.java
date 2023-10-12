package com.games.models.yahtzee;

import java.util.HashMap;

import org.springframework.web.socket.WebSocketSession;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Player {
	
	private String name;
	private WebSocketSession wsSession;
	private WebSocketSession opponent;
	private HashMap<String, String> status;
	
	// 생성자
	public Player(WebSocketSession session) {
		this.name = new String();
		this.wsSession = session;
		this.status = new HashMap<>();
	}
	
}
