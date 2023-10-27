package com.games.models.yacht;

import java.util.HashMap;
import java.util.Random;
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
	
	// 서버 상태정보 => JSON
	public String retrieveServerStatus(WebSocketSession session) {
		StringBuilder sbServer = new StringBuilder();
		Set<String> gameIDs = games.keySet();
		Player player = this.players.get(session);
		String myName = player.getName();
		
		sbServer.append("{");
		sbServer.append("\"myName\": \"" + myName + "\", ");
		sbServer.append("\"playerNum\": \"" + this.players.values().size() + "\", ");
		sbServer.append("\"connectedPlayers\": [");
		for(Player p : this.players.values()) {
			sbServer.append("\"" + p.getName() + "\", ");
		}
		if(this.players.values().size() != 0) sbServer.deleteCharAt(sbServer.lastIndexOf(","));
		sbServer.append("], ");
		
		sbServer.append("\"rooms\": [");
		for(String ID : gameIDs) {
			Game game = games.get(ID);
			
			sbServer.append("{");
			sbServer.append("\"gameID\" : \"" + ID + "\", ");
			sbServer.append("\"players\" : [");
			for(Player p : game.getPlayers().values()) {
				sbServer.append("\"" + p.getName() + "\", ");
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
		session.sendMessage(new TextMessage("server_status@" + this.retrieveServerStatus(session)));
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		// TODO Auto-generated method stub
		
		// 메시지
		String strMsg = message.getPayload().toString();
		System.out.println(strMsg);
		// 요청 헤더
		String header = strMsg.split("@")[0];

		// 웹소켓 통신 테스트
		if(header.equals("echo")) {
			session.sendMessage(new TextMessage("echo@" + session.getId()));
		}
		
		// 서버 상태정보
		if(header.equals("server_status")) {
			session.sendMessage(new TextMessage("server_status@" + this.retrieveServerStatus(session)));
		}
		
		// 새로운 게임방 생성
		if(header.equals("create_room")) {
			String title = message.getPayload().toString().split("@")[1];

			// 플레이어 정의
			Player player = players.get(session);
			if(player.getGameID() != null) {
				// 이미 특정 게임방에 소속된 플레이어가 중복으로 게임방 개설하는경우 차단
				session.sendMessage(new TextMessage("invalid_request@"));
				return;
			}
				
			// 새로운 게임 고유ID 발급
			UUID uuid = UUID.randomUUID();
			// 게임객체 생성
			Game newGame = new Game(title, uuid.toString());
			// 플레이어 및 게임 설정
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
			// 클라이언트에게 대기중인 게임방 상태 전송
			session.sendMessage(new TextMessage("game_status@" + newGame.toJSON()));
		}
		
		// 닉네임 변경요청
		if(header.equals("set_name")) {
			String altName = strMsg.split("@")[1];
			Player player = this.players.get(session);
			
			synchronized (players) {
				player.setName(altName);
			}
			// 닉네임 변경 후 갱신된 상태정보 전송
			session.sendMessage(new TextMessage("server_status@" + retrieveServerStatus(session)));
		}
		
		// 게임방에 참가요청
		if(header.equals("enter")) {
			String msg[] = strMsg.split("@");
			String gameID = null;
			
			if(msg.length > 1) gameID = msg[1]; // 특정 방 선택에 의한 참가
			else { 
				// 빠른참가할 gameID 획득 시도	
				for(Game candidate : this.games.values()) {
					if(!candidate.isActive()) {
						gameID = candidate.getGameID();
						break;
					}
				}
				if(gameID == null) { // 빠른참가 가능한 방이 존재하지 않는 경우
					session.sendMessage(new TextMessage("no_room@"));
					session.sendMessage(new TextMessage("server_status@" + retrieveServerStatus(session)));
					return;
				}
			}

			synchronized (games) {
				Game game = games.get(gameID);
				Player player = players.get(session);
				
				if(game != null && game.getPlayers().size() < 2) {
					// 게임 시작
					for(Player opponent : game.getPlayers().values()) {
						player.setOpponent(opponent.getWsSession());
						opponent.setOpponent(session);
					}
					
					// 기본 배정요소 설정
					player.setFirst(false);
					player.setGameID(gameID);
					game.getPlayers().put(session, player);
					game.setActive(true);
					
					// 참가한 플레이어에게 배정된 정보 전송
					session.sendMessage(new TextMessage("gameID@" + game.getGameID()));
					session.sendMessage(new TextMessage("first@false"));
					
					// 양측 플레이어에게 게임방 상태 전송
					session.sendMessage(new TextMessage("game_status@" + game.toJSON()));
					player.getOpponent().sendMessage(new TextMessage("game_status@" + game.toJSON()));
				
				}else if(game != null){ 
					// 방이 가득 찬 경우
					if(msg.length > 1) { // 특정 방 선택 입장요청 후 방이 가득찬 경우
						session.sendMessage(new TextMessage("full_room@"));
						session.sendMessage(new TextMessage("server_status@" + retrieveServerStatus(session)));
					}else { // 빠른참가 요청후 랜덤한 방 입장 시도가 실패한 경우
						handleMessage(session, message);
					}
				}else {
					// 방이 존재하지 않는 경우
					session.sendMessage(new TextMessage("no_room@"));
					session.sendMessage(new TextMessage("server_status@" + retrieveServerStatus(session)));
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
			boolean isValidDice = false;
			boolean isValidSel = false;
			
			// 옳은 턴인지 판별
			if(player.isFirst() && turn % 2 == 1 && game.isActive()) isValidTurn = true;
			if(!player.isFirst() && turn % 2 == 0 && game.isActive()) isValidTurn = true;	
			// 옳은 주사위 배열을 갖고있는지 판별
			if(game.getDice().size() == 5) isValidDice = true;
			// 옳은 득점옵션 선택 요청인지 판별
			if(isValidTurn && isValidDice) isValidSel = true;
			
			// 득점옵션 선택 요청인 경우
			if(isValidSel && request.equals("select")) {
				String option = strMsg.split("@")[3];
				int result = player.updateStatus(option, game.getDice());

				if(result == 0) {
					// 잘못된(조작된) 요청일 경우
					session.sendMessage(new TextMessage("invalid_request@"));
				}else if(result == 1) {
					// 게임상태 업데이트 성공
					synchronized (games) {
						game.countTurn();
						game.setRemaining(3);
						game.initDice();
					}

					for(Player p : game.getPlayers().values()) {
						WebSocketSession pSession = p.getWsSession();
						pSession.sendMessage(new TextMessage("game_status@" + game.toJSON()));
					}
					
					// 게임이 완료된 경우
					if(game.getTurn() == 25) {
						Thread.sleep(3000);

						Player opponent = game.getPlayers().get(player.getOpponent());
						// 각자 총점
						int pTotal = (Integer)player.getStatus().get("total");
						int oTotal = (Integer)opponent.getStatus().get("total");
						
						// 점수 비교
						if(pTotal > oTotal) {
							session.sendMessage(new TextMessage("result@win"));
							player.getOpponent().sendMessage(new TextMessage("result@defeat"));
						}else if(pTotal < oTotal) {
							session.sendMessage(new TextMessage("result@defeat"));
							player.getOpponent().sendMessage(new TextMessage("result@win"));
						}else {
							// 무승부
							session.sendMessage(new TextMessage("result@draw"));
							player.getOpponent().sendMessage(new TextMessage("result@draw"));
						}
						
						// 플레이어들의 status 초기화
						synchronized (players) {
							player.init();
							opponent.init();
						}
						// 완료된 게임 제거
						synchronized (games) {
							this.games.remove(gameID);
						}
					}
				}
			}
			
			// 주사위 롤/리롤 요청인 경우
			if(isValidTurn && request.equals("reroll") && game.getRemaining() > 0) {
				String[] strRerollIndexes;
				boolean success = false;
				
				if(strMsg.split("@").length == 4) {
					strRerollIndexes = strMsg.split("@")[3].split("/");
				}else {
					strRerollIndexes = new String[0];
				}
				
				int[] rerollIndexes = new int[strRerollIndexes.length];
				
				if(strRerollIndexes.length == 0) {
					synchronized (games) {
						game.roll();
					}
					success = true;
				}else {
					for(int i=0; i<strRerollIndexes.length; i++) {
						rerollIndexes[i] = Integer.parseInt(strRerollIndexes[i]);
					}
					if(game.getDice().size() != 0) {
						synchronized (games) {
							game.reroll(rerollIndexes);
						}
						success = true;
					}
				}
				if(success) game.subtract();
				
				for(Player p : game.getPlayers().values()) {
					WebSocketSession pSession = p.getWsSession();
					pSession.sendMessage(new TextMessage("game_status@" + game.toJSON()));
				}	
			}
		}
		
		// 게임방에서 나가기 요청
		if(header.equals("exit")) {
			Player player = this.players.get(session);
			Player opponent = this.players.get(player.getOpponent());
			
			// 플레이어가 접속해있던 방 제거
			String gameId = player.getGameID();
			synchronized (games) {
				this.games.remove(gameId);
			}
			synchronized (players) {
				player.init();
			}
			// 상대 플레이어에게 플레이어 퇴장 알림
			if(opponent != null) {
				opponent.getWsSession().sendMessage(new TextMessage("opp_disconnected@"));
				synchronized (players) {
					opponent.init();
				}
			}

			session.sendMessage(new TextMessage("server_status@" + this.retrieveServerStatus(session)));
		}
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		// TODO Auto-generated method stub	
		Player player = this.players.get(session);
		Player opponent = this.players.get(player.getOpponent());
		
		// 플레이어가 접속해있던 방 제거
		String gameId = player.getGameID();
		synchronized (games) {
			this.games.remove(gameId);
		}
		// 같이 게임하던 상대에게 연결끊김 알림
		if(opponent != null) {
			opponent.getWsSession().sendMessage(new TextMessage("opp_disconnected@"));
			synchronized (players) {
				opponent.init();
			}
		}
		// 접속중인 플레이어 목록에서 사용자 제거
		synchronized (players) {
			players.remove(session);
		}
	}

	@Override
	public boolean supportsPartialMessages() {
		// TODO Auto-generated method stub
		return false;
	}

}
