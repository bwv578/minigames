package com.games.models.yahtzee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.springframework.web.socket.WebSocketSession;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Game {
	
	private String title; // 게임 방제
	//private ArrayList<Player> players; // 플레이어 목록
	private HashMap<WebSocketSession, Player> players; // 플레이어 목록
	private String gameID; // 게임 고유ID
	private int turn; // 턴
	private ArrayList<Integer> dice; // 주사위 결과
	private int remaining; // 남은 리롤횟수
	
	// 생성자
	public Game(String title, String uuid) {
		this.title = title;
		this.gameID = uuid;
		this.players = new HashMap<>();
		this.dice = new ArrayList<>();
		this.turn = 1;
		this.remaining = 2;
	}
	
	// 주사위 굴리기
	public void roll(){
		ArrayList<Integer> result = new ArrayList<>();
		Random random = new Random();
		
		for(int i=1; i<=5; i++) {
			int randomDiceNumber = random.nextInt(6) + 1;
			result.add(randomDiceNumber);
		}
		
		this.dice = result;
	}
	
	// 턴 카운트
	public void countTurn() {
		this.turn ++;
	}
	
	// JSON 문자열로 변환
	public String toJSON() {
		StringBuilder sbResult = new StringBuilder();
		
		sbResult.append("{");
		sbResult.append("\"gameID\": \"" + this.gameID + "\",");
		sbResult.append("\"title\": \"" + this.title + "\",");
		sbResult.append("\"turn\": \"" + this.turn + "\",");
		sbResult.append("\"remaining\": \"" + this.remaining + "\",");
		sbResult.append("\"players\": [");
		for(Player player : this.players.values()) {
			sbResult.append("{");
			sbResult.append("\"name\": \"" + player.getName() + "\",");
			sbResult.append("\"wsSession\": \"" + player.getWsSession() + "\",");
			sbResult.append("\"opponent\": \"" + player.getOpponent() + "\",");
			sbResult.append("\"status\": {");
			sbResult.append("\"aces\": \"" + player.getStatus().get("aces") + "\",");
			sbResult.append("\"twos\": \"" + player.getStatus().get("twos") + "\",");
			sbResult.append("\"threes\" : \"" + player.getStatus().get("threes") + "\",");
			sbResult.append("\"fours\" : \"" + player.getStatus().get("fours") + "\",");
			sbResult.append("\"fives\" : \"" + player.getStatus().get("fives") + "\",");
			sbResult.append("\"sixes\" : \"" + player.getStatus().get("sixes") + "\",");
			sbResult.append("\"threeofakind\" : \"" + player.getStatus().get("3ofakind") + "\",");
			sbResult.append("\"fourofakind\" : \"" + player.getStatus().get("4ofakind") + "\",");
			sbResult.append("\"fullhouse\" : \"" + player.getStatus().get("fullhouse") + "\",");
			sbResult.append("\"smallstr\" : \"" + player.getStatus().get("smallstr") + "\",");
			sbResult.append("\"largestr\" : \"" + player.getStatus().get("largestr") + "\",");
			sbResult.append("\"choice\" : \"" + player.getStatus().get("choice") + "\",");
			sbResult.append("\"yatch\" : \"" + player.getStatus().get("yatch") + "\"");
			sbResult.append("}");
			sbResult.append("},");
		}
		if(this.players.size() != 0) sbResult.deleteCharAt(sbResult.lastIndexOf(","));
		sbResult.append("],");
		sbResult.append("\"dice\": [");
		for(Integer num : this.dice) {
			sbResult.append("\"" + num + "\", ");
		}
		if(this.dice.size() != 0) sbResult.deleteCharAt(sbResult.lastIndexOf(","));
		sbResult.append("]");
		sbResult.append("}");
		
		return sbResult.toString();
	}
	
}
