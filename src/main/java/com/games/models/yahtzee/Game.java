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
	
	// 생성자
	public Game(String title) {
		this.title = title;
		this.players = new ArrayList<>();
	}
	
}
