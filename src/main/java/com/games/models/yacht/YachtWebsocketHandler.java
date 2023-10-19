package com.games.models.yacht;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

public class YachtWebsocketHandler implements WebSocketHandler{
	
	volatile private HashMap<WebSocketSession, Player> players = new HashMap<>(); // 웹소켓 세션으로 플레이어 구분
	volatile private HashMap<String, Game> games = new HashMap<>(); // 게임ID - 게임 연결 쌍	

	// 모든 게임방 조회 => JSON 문자열로 반환
	/*
	public String retrieveRooms() {
		StringBuilder sbRooms = new StringBuilder();
		Set<String> gameIDs = games.keySet();
		
		sbRooms.append("[");
		for(String ID : gameIDs) {
			Game game = games.get(ID);
			
			sbRooms.append("{");
			sbRooms.append("\"gameID\" : \"" + ID + "\", ");
			sbRooms.append("\"players\" : [");
			for(Player player : game.getPlayers().values()) {
				sbRooms.append("\"" + player.getName() + "\", ");
			}
			sbRooms.deleteCharAt(sbRooms.lastIndexOf(","));
			sbRooms.append("], ");
			sbRooms.append("\"title\" : \"" + game.getTitle() + "\"");
			sbRooms.append("}, ");
		}
		if(gameIDs.size() != 0) sbRooms.deleteCharAt(sbRooms.lastIndexOf(","));
		sbRooms.append("]");
		
		return sbRooms.toString();
	}
	*/
	
	// 서버 상태정보 => JSON
	public String retrieveServerStatus() {
		StringBuilder sbServer = new StringBuilder();
		Set<String> gameIDs = games.keySet();
		
		sbServer.append("{");
		sbServer.append("\"playerNum\": \"" + this.players.values().size() + "\", ");
		sbServer.append("\"connectedPlayers\": [");
		for(Player player : this.players.values()) {
			sbServer.append("\"" + player.getName() + "\", ");
		}
		if(this.players.values().size() != 0) sbServer.deleteCharAt(sbServer.lastIndexOf(","));
		sbServer.append("], ");
		
		sbServer.append("\"rooms\": [");
		for(String ID : gameIDs) {
			Game game = games.get(ID);
			
			sbServer.append("{");
			sbServer.append("\"gameID\" : \"" + ID + "\", ");
			sbServer.append("\"players\" : [");
			for(Player player : game.getPlayers().values()) {
				sbServer.append("\"" + player.getName() + "\", ");
			}
			sbServer.deleteCharAt(sbServer.lastIndexOf(","));
			sbServer.append("], ");
			sbServer.append("\"title\" : \"" + game.getTitle() + "\"");
			sbServer.append("}, ");
		}
		if(gameIDs.size() != 0) sbServer.deleteCharAt(sbServer.lastIndexOf(","));
		sbServer.append("]");		
		sbServer.append("}");
		
		return sbServer.toString();
	}
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		// TODO Auto-generated method stub
		
		// 새로운 플레이어 접속 시 플레이어 목록에 추가, 게임방 목록 전송
		Player newPlayer = new Player(session);
		newPlayer.setName(session.getId());
		newPlayer.setWsSession(session);

		synchronized (players) {
			players.put(session, newPlayer);
		}
		
		session.sendMessage(new TextMessage("server_status@" + this.retrieveServerStatus()));
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		// TODO Auto-generated method stub
		
		// 메시지
		String strMsg = message.getPayload().toString();
		// 요청 헤더
		String header = strMsg.split("@")[0];

		// 웹소켓 통신 테스트
		if(header.equals("echo")) {
			session.sendMessage(new TextMessage("echo@" + session.getId()));
		}
		
		// 서버 상태정보
		if(header.equals("server_status")) {
			session.sendMessage(new TextMessage("server_status@" + this.retrieveServerStatus()));
		}
		
		// 새로운 게임방 생성
		if(header.equals("create_room")) {
			String title = message.getPayload().toString().split("@")[1];
			
			// 새로운 게임 고유ID 발급
			UUID uuid = UUID.randomUUID();

			// 게임객체 생성, 게임에 방 개설한 플레이어 추가
			Game newGame = new Game(title, uuid.toString());
			Player player = players.get(session);
			player.setFirst(true);
			player.setGameID(uuid.toString());
			newGame.getPlayers().put(session, player);

			// 게임방 목록에 새 게임 추가
			synchronized (games) {
				games.put(uuid.toString(), newGame);
			}
			
			// 클라이언트에게 게임ID 전송
			session.sendMessage(new TextMessage("gameID@" + uuid.toString()));
			// 클라이언트에게 차례 전송
			session.sendMessage(new TextMessage("first@true"));
		}
		
		// 게임방에 참가요청
		if(header.equals("enter")) {
			String msg[] = message.getPayload().toString().split("@");
			String gameID = msg[1];

			synchronized (games) {
				Game game = games.get(gameID);
				Player player = players.get(session);
				
				if(game.getPlayers().size() < 2) {
					// 게임 시작
					for(Player opponent : game.getPlayers().values()) {
						player.setOpponent(opponent.getWsSession());
						opponent.setOpponent(session);
					}			
					player.setFirst(false);
					player.setGameID(gameID);
					game.getPlayers().put(session, player);
					game.roll();
					session.sendMessage(new TextMessage("gameID@" + game.getGameID()));
					session.sendMessage(new TextMessage("first@false"));
					// 양측 플레이어에게 게임방 상태 전송
					session.sendMessage(new TextMessage("game_status@" + game.toJSON()));
					player.getOpponent().sendMessage(new TextMessage("game_status@" + game.toJSON()));
				}else {
					// 방이 가득 찬 경우
					session.sendMessage(new TextMessage("full_room@"));
					session.sendMessage(new TextMessage("server_status@" + retrieveServerStatus()));
				}
			}
		}
		
		// 플레이어의 게임 조작정보 수신
		if(header.equals("gameID")) {
			String gameID = strMsg.split("@")[1];
			Game game = games.get(gameID);
			Player player = game.getPlayers().get(session);		
			String request = strMsg.split("@")[2];
			int turn = game.getTurn();
			boolean isValidTurn = false;
			
			// 옳은 턴인지 판별
			if(player.isFirst() && turn % 2 == 1) isValidTurn = true;
			if(!player.isFirst() && turn % 2 == 0) isValidTurn = true;
			
			// 득점옵션 선택 요청인 경우
			if(isValidTurn && request.equals("select")) {
				String option = strMsg.split("@")[3];
				int result = player.updateStatus(option, game.getDice());

				if(result == 0) {
					// 잘못된(조작된) 요청일 경우
					session.sendMessage(new TextMessage("invalid_request@"));
				}else if(result == 1) {
					// 게임상태 업데이트 성공
					game.countTurn();
					game.setRemaining(2);
					game.roll();

					for(Player p : game.getPlayers().values()) {
						WebSocketSession pSession = p.getWsSession();
						pSession.sendMessage(new TextMessage("game_status@" + game.toJSON()));
					}						
				}
			}
			
			// 주사위 리롤 요청인 경우
			if(isValidTurn && request.equals("reroll") && game.getRemaining() > 0) {
				String[] strRerollIndexes = strMsg.split("@")[3].split("/");
				int[] rerollIndexes = new int[strRerollIndexes.length];

				for(int i=0; i<strRerollIndexes.length; i++) {
					rerollIndexes[i] = Integer.parseInt(strRerollIndexes[i]);
				}
				
				game.reroll(rerollIndexes);
				game.subtract();
				
				for(Player p : game.getPlayers().values()) {
					WebSocketSession pSession = p.getWsSession();
					pSession.sendMessage(new TextMessage("game_status@" + game.toJSON()));
				}	
			}
			
		}
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		// TODO Auto-generated method stub	
		System.out.println("ws세션종료");
		
		Player player = this.players.get(session);
		
		// 플레이어가 접속해있던 방 제거
		String gameId = player.getGameID();
		this.games.remove(gameId);
		
		// 접속중인 플레이어 목록에서 사용자 제거
		players.remove(session);
		
	}

	@Override
	public boolean supportsPartialMessages() {
		// TODO Auto-generated method stub
		return false;
	}

}
