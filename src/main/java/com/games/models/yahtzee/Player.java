package com.games.models.yahtzee;

import java.util.HashMap;

import org.springframework.web.socket.WebSocketSession;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Player {
	
	private String name; // 플레이어 이름
	private WebSocketSession wsSession; // 플레이어 웹소켓 세션
	private WebSocketSession opponent; // 게임상대
	private boolean first; // 차례 선 여부
	private HashMap<String, Object> status; // 게임상태
	private HashMap<String, Boolean> possibleChoices; // 가능한 선택지
	
	// 생성자
	public Player(WebSocketSession session) {
		this.name = new String();
		this.wsSession = session;
		this.status = new HashMap<>();
		this.possibleChoices = new HashMap<>();
		this.status.put("aces", "");
		this.status.put("twos", "");
		this.status.put("threes", "");
		this.status.put("fours", "");
		this.status.put("fives", "");
		this.status.put("sixes", "");
		this.status.put("4ofakind", "");
		this.status.put("fullhouse", "");
		this.status.put("smallstr", "");
		this.status.put("largestr", "");
		this.status.put("yatch", "");
		this.status.put("choice", "");
	}
	
}
