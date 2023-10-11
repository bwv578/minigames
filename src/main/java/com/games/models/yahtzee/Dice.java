package com.games.models.yahtzee;

import java.util.ArrayList;
import java.util.Random;

public class Dice {
	
	// 주사위 굴리기 => 무작위로 굴린 5개 주사위 결과 반환
	public ArrayList<Integer> roll(){
		ArrayList<Integer> result = new ArrayList<>();
		Random random = new Random();
		
		for(int i=1; i<=5; i++) {
			int randomDiceNumber = random.nextInt(6) + 1;
			result.add(randomDiceNumber);
		}
		
		return result;
	}
	
}
