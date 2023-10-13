package com.games.models.yahtzee;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

public class YahtzeeWebsocketHandler implements WebSocketHandler{
	
	volatile private HashMap<WebSocketSession, Player> players = new HashMap<>(); // 웹소켓 세션으로 플레이어 구분
	volatile private HashMap<String, Game> games = new HashMap<>(); // 게임ID - 게임 연결 쌍	

	// 모든 게임방 조회 => JSON 문자열로 반환
	public String retrieveRooms() {
		StringBuilder sbRooms = new StringBuilder();
		Set<String> gameIDs = games.keySet();
		
		sbRooms.append("[");
		for(String ID : gameIDs) {
			Game game = games.get(ID);
			
			sbRooms.append("{");
			sbRooms.append("\"gameID\" : \"" + ID + "\", ");
			sbRooms.append("\"players\" : [");
			for(Player player : game.getPlayers()) {
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
		
		session.sendMessage(new TextMessage("rooms@" + retrieveRooms()));
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		// TODO Auto-generated method stub
		
		// 새로운 게임 방 생성
		if(message.getPayload().toString().split("@")[0].equals("create_room")) {
			String title = message.getPayload().toString().split("@")[1];
			
			// 새로운 게임 고유ID 발급
			UUID uuid = UUID.randomUUID();

			// 게임객체 생성, 게임에 방 개설한 플레이어 추가
			Game newGame = new Game(title, uuid.toString());
			Player player = players.get(session);
			player.setFirst(true);
			newGame.getPlayers().add(player);

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
		if(message.getPayload().toString().split("@")[0].equals("enter")) {
			String msg[] = message.getPayload().toString().split("@");
			String gameID = msg[1];

			synchronized (games) {
				Game game = games.get(gameID);
				Player player = players.get(session);
				
				if(game.getPlayers().size() < 2) {
					// 게임 시작
					Player opponent = game.getPlayers().get(0);
					player.setOpponent(opponent.getWsSession());
					player.setFirst(false);
					game.getPlayers().add(player);
					game.roll();
					session.sendMessage(new TextMessage("gameID@" + game.getGameID()));
					session.sendMessage(new TextMessage("first@false"));
					// 양측 플레이어에게 게임방 상태 전송
					session.sendMessage(new TextMessage("game_status@" + game.toJSON()));
					opponent.getWsSession().sendMessage(new TextMessage("game_status@" + game.toJSON()));
				}else {
					// 방이 가득 찬 경우
					session.sendMessage(new TextMessage("full_room@"));
					session.sendMessage(new TextMessage("rooms@" + retrieveRooms()));
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
	}

	@Override
	public boolean supportsPartialMessages() {
		// TODO Auto-generated method stub
		return false;
	}

}
