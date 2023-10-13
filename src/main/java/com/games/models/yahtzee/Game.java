package com.games.models.yahtzee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Game {
	
	private String title; // 게임 방제
	private ArrayList<Player> players; // 플레이어 목록
	private String gameID; // 게임 고유ID
	private int turn; // 턴
	private ArrayList<Integer> dice; // 주사위 결과
	
	// 생성자
	public Game(String title, String uuid) {
		this.title = title;
		this.gameID = uuid;
		this.players = new ArrayList<>();
		this.dice = new ArrayList<>();
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
	
	// 선택지 업데이트
	public void updateOptions() {
		for(Player player : this.players) {
			HashMap<String, Object> status = player.getStatus();
			HashMap<String, Boolean> options = new HashMap<>();
			options.put("aces", false);
			options.put("twos", false);
			options.put("threes", false);
			options.put("fours", false);
			options.put("fives", false);
			options.put("sixes", false);
			options.put("4ofakind", false);
			options.put("fullhouse", false);
			options.put("smallstr", false);
			options.put("largestr", false);
			options.put("yatch", false);
			options.put("choice", false);
			
			if(status.get("aces") == "") {
				options.put("aces", true);
			}
			if(status.get("twos") == "") {
				options.put("twos", true);
			}
			if(status.get("threes") == "") {
				options.put("thress", true);
			}
			if(status.get("fours") == "") {
				options.put("fours", true);
			}
			if(status.get("fives") == "") {
				options.put("fives", true);
			}
			if(status.get("sixes") == "") {
				options.put("sixes", true);
			}
			if(status.get("choice") == "") {
				options.put("choice", true);
			}
			if(status.get("3ofakind") == "") {
				for(int index=0; index<=2; index++) {
					int standard = this.dice.get(index);
					int sameNumbers = 0;
					for(int num : this.dice) {
						if(standard == num) sameNumbers ++;
					}
					if(sameNumbers >= 3) {
						options.put("3ofakind", true);
						break;
					}
				}
			}
			if(status.get("4ofakind") == "") {
				for(int index=0; index<=1; index++) {
					int standard = this.dice.get(index);
					int sameNumbers = 0;
					for(int num : this.dice) {
						if(standard == num) sameNumbers ++;
					}
					if(sameNumbers >= 4) {
						options.put("4ofakind", true);
						break;
					}
				}
			}
			if(status.get("fullhouse") == "") {
				int num = this.dice.get(0);
				int sameNumbers = 0;
				Boolean isFullHouse = true;
				ArrayList<Integer> notSameNumbers = new ArrayList<>();
				for(int target : this.dice) {
					if(num == target) {
						sameNumbers ++;
					}else {
						notSameNumbers.add(target);
					}
				}
				if(sameNumbers == 2 || sameNumbers == 3) {
					int standard = notSameNumbers.get(0);
					for(int notSameNumber : notSameNumbers) {
						if(notSameNumber != standard) {
							isFullHouse = false;
							break;
						}
					}
				}else isFullHouse = false;
				if(isFullHouse) options.put("fullhouse", true);
			}
			if(status.get("smallstr") == "") {
				boolean isStr = false;
				for(int num : this.dice) {
					if(isStr) break;
					int next = num + 1;
					for(int i=1; i<=3; i++) {
						if(this.dice.contains(next)) {
							next ++;
							if(i == 3) isStr = true;
						}else {
							break;
						}
					}
				}
				if(isStr) options.put("smallstr", true);
			}
			if(status.get("largestr") == "") {
				boolean isStr = false;
				for(int num : this.dice) {
					if(isStr) break;
					int next = num + 1;
					for(int i=1; i<=4; i++) {
						if(this.dice.contains(next)) {
							next ++;
							if(i == 4) isStr = true;
						}else {
							break;
						}
					}
				}
				if(isStr) options.put("largestr", true);
			}
			if(status.get("yatch") == "") {
				int standard = this.dice.get(0);
				boolean isYatch = true;
				for(int num : this.dice) {
					if(num != standard) {
						isYatch = false;
						break;
					}
				}
				if(isYatch) options.put("yatch", true);
			}
		}
	}
	
	// JSON 문자열로 변환
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
