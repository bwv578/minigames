package com.games.models.yahtzee;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.web.socket.WebSocketSession;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Player {
	
	private String name; // 플레이어 이름
	private WebSocketSession wsSession; // 플레이어 웹소켓 세션
	private WebSocketSession opponent; // 게임상대 세션
	private boolean first; // 차례 선 여부
	private HashMap<String, Object> status; // 게임상태
	private int remaining;

	// 생성자
	public Player(WebSocketSession session) {
		this.name = new String();
		this.wsSession = session;
		this.status = new HashMap<>();
		this.status.put("aces", "");
		this.status.put("twos", "");
		this.status.put("threes", "");
		this.status.put("fours", "");
		this.status.put("fives", "");
		this.status.put("sixes", "");
		this.status.put("3ofakind", "");
		this.status.put("4ofakind", "");
		this.status.put("fullhouse", "");
		this.status.put("smallstr", "");
		this.status.put("largestr", "");
		this.status.put("yatch", "");
		this.status.put("choice", "");
	}
	
	// 점수계산 및 게임상태 업데이트
	public int updateStatus(String option, ArrayList<Integer> dice) {
		if(this.status.get(option).equals("")) {
			int score = 0;

			if(option.equals("aces")) {
				for(int num : dice) {
					if(num == 1) score += num;
				}
			}else if(option.equals("twos")){
				for(int num : dice) {
					if(num == 2) score += num;
				}
			}else if(option.equals("threes")){
				for(int num : dice) {
					if(num == 3) score += num;
				}
			}else if(option.equals("fours")){
				for(int num : dice) {
					if(num == 4) score += num;
				}
			}else if(option.equals("fives")){
				for(int num : dice) {
					if(num == 5) score += num;
				}
			}else if(option.equals("sixes")){
				for(int num : dice) {
					if(num == 6) score += num;
				}
			}else if(option.equals("3ofakind")){
				for(int index=0; index<=2; index++) {
					int standard = dice.get(index);
					int sameNumbers = 0;
					for(int num : dice) {
						if(standard == num) sameNumbers ++;
					}
					if(sameNumbers >= 3) {
						for(int num : dice) {
							score += num;
						}
						break;
					}
				}
			}else if(option.equals("4ofakind")){
				for(int index=0; index<=1; index++) {
					int standard = dice.get(index);
					int sameNumbers = 0;
					for(int num : dice) {
						if(standard == num) sameNumbers ++;
					}
					if(sameNumbers >= 4) {
						for(int num : dice) {
							score += num;
						}
						break;
					}
				}
			}else if(option.equals("fullhouse")){
				int num = dice.get(0);
				int sameNumbers = 0;
				Boolean isFullHouse = true;
				ArrayList<Integer> notSameNumbers = new ArrayList<>();
				for(int target : dice) {
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
				if(isFullHouse) score = 25;
			}else if(option.equals("smallstr")){
				boolean isStr = false;
				for(int num : dice) {
					if(isStr) break;
					int next = num + 1;
					for(int i=1; i<=3; i++) {
						if(dice.contains(next)) {
							next ++;
							if(i == 3) isStr = true;
						}else {
							break;
						}
					}
				}
				if(isStr) score = 30;
			}else if(option.equals("largestr")){
				boolean isStr = false;
				for(int num : dice) {
					if(isStr) break;
					int next = num + 1;
					for(int i=1; i<=4; i++) {
						if(dice.contains(next)) {
							next ++;
							if(i == 4) isStr = true;
						}else {
							break;
						}
					}
				}
				if(isStr) score = 40;
			}else if(option.equals("yatch")){
				int standard = dice.get(0);
				boolean isYatch = true;
				for(int num : dice) {
					if(num != standard) {
						isYatch = false;
						break;
					}
				}
				if(isYatch) score = 50;
			}else if(option.equals("choice")){
				for(int num : dice) {
					score += num;
				}
			}

			this.status.put(option, score);
			return 1; 
		}else {
			return 0;
		}
	}
}