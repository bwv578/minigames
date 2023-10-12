package com.games.models.yahtzee;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Game {
	
	private String title; // 게임 방제
	private ArrayList<Player> players; // 플레이어 목록
	private String gameID; // 게임 고유ID
	private int turn;
	
	// 생성자
	public Game(String title) {
		this.title = title;
		this.players = new ArrayList<>();
	}
	
	// JSON 문자열 변환 메소드
	public String toJSON() {
		StringBuilder sbResult = new StringBuilder();
		
		sbResult.append("{");
		sbResult.append("\"gameID\": \"" + this.gameID + "\",");
		sbResult.append("\"title\": \"" + this.title + "\",");
		sbResult.append("\"turn\": \"" + this.turn + "\",");
		sbResult.append("\"players\": [");
		for(Player player : this.players) {
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
			sbResult.append("\"4ofakind\" : \"" + player.getStatus().get("4ofakind") + "\",");
			sbResult.append("\"fullhouse\" : \"" + player.getStatus().get("fullhouse") + "\",");
			sbResult.append("\"smallstr\" : \"" + player.getStatus().get("smallstr") + "\",");
			sbResult.append("\"largestr\" : \"" + player.getStatus().get("largestr") + "\",");
			sbResult.append("\"choice\" : \"" + player.getStatus().get("choice") + "\",");
			sbResult.append("\"yacht\" : \"" + player.getStatus().get("yacht") + "\"");
			sbResult.append("}");
			sbResult.append("},");
		}
		if(this.players.size() != 0) sbResult.deleteCharAt(sbResult.lastIndexOf(","));
		sbResult.append("]");
		sbResult.append("}");
		
		return sbResult.toString();
	}
	
}
