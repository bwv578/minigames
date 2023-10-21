<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="main.jsp" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Yacht</title>
</head>
<!-- Jquery -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.4/jquery.min.js"></script>
<!-- font awesome -->
<script src="https://kit.fontawesome.com/b908b5678e.js" crossorigin="anonymous"></script>
<script type="text/javascript">

	window.onload = function(){
	
		const socket = new WebSocket("ws://localhost:8080/yachtWS");
		//const socket = new WebSocket("ws://52.78.178.113:8080/yachtWS");
		
		let gameID = '';
		let first = '';

		document.getElementById('mkRoom').onclick = function(){	
			socket.send('create_room@' + '임의의 방제');
		};
		
		socket.onmessage = function(event){
			console.log(event.data);
			const msg = event.data;
			const header = msg.split('@')[0];
			
			// 방 목록 정보를 수신한 경우
			if(header == 'server_status'){
				$('#rooms').empty();
				const rooms = JSON.parse(msg.split('@')[1]).rooms;
				
				$('#mainContainer').css({
					'display' : 'none'
				});
				$('#contentContainer').css({
					'display' : 'flex'
				});
				
				let roomsTable = '<table id="roomList">';
				for(let index=0; index<rooms.length; index++){
					const room = rooms[index];

					roomsTable += '<tr><td class="room" gameID="' + room.gameID + '">';
					roomsTable += '<b>' + room.title + '</b>';
					roomsTable += '&nbsp; <div id="roomInfo"><b id="small">플레이어 : </b> ';
					for(let pIndex=0; pIndex<room.players.length; pIndex++){
						if(pIndex == 1){
							roomsTable += ' vs ';
						}
						roomsTable += '<b id="small">' + room.players[pIndex] + '</b>';
					}
					roomsTable += '</div></td></tr>';
				}
				roomsTable += '</table>';

				var roomList = $(roomsTable);
				$('#rooms').append(roomList);
				
				$('.room').on('click', function () {
			        socket.send('enter@' + $(this).attr('gameID'));
			    });
			}
			
			// 클릭한 방이 이미 게임 플레이중인 경우
			if(header == 'full_room'){
				$(document).on('click', '#room', function() {
					socket.send('enter@' + $(this).attr('gameID'));
					$(document).off('click', '#room');
				});
			}
			
			// 방에 입장함
			if(header == 'gameID'){
				gameID = msg.split('@')[1];
				$('[separator="td"]').css({
					'background-color' : 'transparent'
				});
				$('#mainContainer').css({
					'display' : 'flex'
				});
				$('#contentContainer').css({
					'display' : 'none'
				});
			}
			
			// 순서정보 수신
			if(header == 'first'){
				first = msg.split('@')[1];
			}
			
			// 게임 진행정보 수신
			if(header == 'game_status'){
				const gameStatus = JSON.parse(msg.split('@')[1]);
				const dice = gameStatus.dice;
				const turn = gameStatus.turn;
				const active = gameStatus.active;
				let myName = '';
				let oppName = '';
				let myStatus = '';
				let oppStatus = '';
				let myTurn = false;
				
				if(active == 'true'){
					if(first == 'true' && (turn % 2) == 1){
						myTurn = true;
					}else if(first == 'false' && (turn % 2) == 0){
						myTurn = true;
					}

					if(first == 'true'){
						myStatus = gameStatus.players[0].status;
						oppStatus = gameStatus.players[1].status;
						myName = gameStatus.players[0].name;
						oppName = gameStatus.players[1].name;
					}else{
						myStatus = gameStatus.players[1].status;
						oppStatus = gameStatus.players[0].status;
						myName = gameStatus.players[1].name;
						oppName = gameStatus.players[0].name;
					}
				}else{
					myName = gameStatus.players[0].name;
					console.log('myName: ' + myName);
					myStatus = gameStatus.players[0].status;
					oppName = '';
					oppStatus = gameStatus.players[0].status;
				}

				// 주사위
				if(dice.length == 0){
					for(let index=1; index<=5; index++){
						const targetID = 'dice' + index;
						$('#' + targetID).html('<i class="fa-regular fa-square"></i>');
					}
				}else{
					for(let index=0; index<=4; index++){
						const targetID = 'dice' + (index + 1);
						const diceNum = dice[index];

						if(diceNum == 0){
							$('#' + targetID).html('<i class="fa-regular fa-square"></i>');
						}else if(diceNum == 1){
							$('#' + targetID).html('<i class="fa-solid fa-dice-one"></i>');
						}else if(diceNum == 2){
							$('#' + targetID).html('<i class="fa-solid fa-dice-two"></i>');
						}else if(diceNum == 3){
							$('#' + targetID).html('<i class="fa-solid fa-dice-three"></i>');
						}else if(diceNum == 4){
							$('#' + targetID).html('<i class="fa-solid fa-dice-four"></i>');
						}else if(diceNum == 5){
							$('#' + targetID).html('<i class="fa-solid fa-dice-five"></i>');
						}else if(diceNum == 6){
							$('#' + targetID).html('<i class="fa-solid fa-dice-six"></i>');
						}
					}
				}

				// 플레이어 이름
				$('#p1name').html(myName);
				$('#p2name').html(oppName);
				
				// aces
				$('#p1aces').html(myStatus.aces);
				$('#p2aces').html(oppStatus.aces);
				
				// twos
				$('#p1twos').html(myStatus.twos);
				$('#p2twos').html(oppStatus.twos);
				
				// threes
				$('#p1threes').html(myStatus.threes);
				$('#p2threes').html(oppStatus.threes);
				
				// fours
				$('#p1fours').html(myStatus.fours);
				$('#p2fours').html(oppStatus.fours);
				
				// fives
				$('#p1fives').html(myStatus.fives);
				$('#p2fives').html(oppStatus.fives);
				
				// sixes
				$('#p1sixes').html(myStatus.sixes);
				$('#p2sixes').html(oppStatus.sixes);
				
				// bonus
				$('#p1bonus').html(myStatus.bonus);
				$('#p2bonus').html(oppStatus.bonus);
				
				// 4 of a kind
				$('#p14ofakind').html(myStatus.fourofakind);
				$('#p24ofakind').html(oppStatus.fourofakind);
				
				// full house
				$('#p1fullhouse').html(myStatus.fullhouse);
				$('#p2fullhouse').html(oppStatus.fullhouse);
				
				// small straight
				$('#p1smallstr').html(myStatus.smallstr);
				$('#p2smallstr').html(oppStatus.smallstr);
				
				// large straight
				$('#p1largestr').html(myStatus.largestr);
				$('#p2largestr').html(oppStatus.largestr);
				
				// yacht
				$('#p1yacht').html(myStatus.yacht);
				$('#p2yacht').html(oppStatus.yacht);
				
				// choice
				$('#p1choice').html(myStatus.choice);
				$('#p2choice').html(oppStatus.choice);
				
				// total
				$('#p1total').html(myStatus.total);
				$('#p2total').html(oppStatus.total);
				
				// 나의 턴일때
				if(myTurn){
					// 초기화
					$('[act="act"]').off('click');
					$('[act="act"]').off('mouseenter mouseleave');
					$('#reroll').css({
						'display' : 'block'
					});
					$('[sel="true"]').css({
						'color' : 'black'
					});
					$('[sel="true"]').attr('sel', 'false');
					$('[property="dice"]').off('click');
					// 턴 표시
					$('#p1name').css({
						'background-color' : 'green'
					});
					$('#p2name').css({
						'background-color' : 'transparent'
					});
					
					// 리롤버튼
					if(gameStatus.remaining == 3){
						$('#rerollbtn').html('주사위 굴리기');
					}else{
						$('#rerollbtn').html('다시 굴리기 ' + gameStatus.remaining + '/2');
					}

					if(myStatus.aces == ''){
						$('#p1aces').hover(function(){
							$(this).css("background-color", "yellow");
						}, function(){
							$(this).css("background-color", "transparent");
						});
						$('#p1aces').click(function(){
							selectCombination('aces');
						});
					}
					if(myStatus.twos == ''){
						$('#p1twos').hover(function(){
							$(this).css("background-color", "yellow");
						}, function(){
							$(this).css("background-color", "transparent");
						});
						$('#p1twos').click(function(){
							selectCombination('twos');
						});
					}
					if(myStatus.threes == ''){
						$('#p1threes').hover(function(){
							$(this).css("background-color", "yellow");
						}, function(){
							$(this).css("background-color", "transparent");
						});
						$('#p1threes').click(function(){
							selectCombination('threes');
						});
					}
					if(myStatus.fours == ''){
						$('#p1fours').hover(function(){
							$(this).css("background-color", "yellow");
						}, function(){
							$(this).css("background-color", "transparent");
						});
						$('#p1fours').click(function(){
							selectCombination('fours');
						});
					}
					if(myStatus.fives == ''){
						$('#p1fives').hover(function(){
							$(this).css("background-color", "yellow");
						}, function(){
							$(this).css("background-color", "transparent");
						});
						$('#p1fives').click(function(){
							selectCombination('fives');
						});
					}
					if(myStatus.sixes == ''){
						$('#p1sixes').hover(function(){
							$(this).css("background-color", "yellow");
						}, function(){
							$(this).css("background-color", "transparent");
						});
						$('#p1sixes').click(function(){
							selectCombination('sixes');
						});
					}
					if(myStatus.fourofakind == ''){
						$('#p14ofakind').hover(function(){
							$(this).css("background-color", "yellow");
						}, function(){
							$(this).css("background-color", "transparent");
						});
						$('#p14ofakind').click(function(){
							selectCombination('4ofakind');
						});
					}
					if(myStatus.fullhouse == ''){
						$('#p1fullhouse').hover(function(){
							$(this).css("background-color", "yellow");
						}, function(){
							$(this).css("background-color", "transparent");
						});
						$('#p1fullhouse').click(function(){
							selectCombination('fullhouse');
						});
					}
					if(myStatus.smallstr == ''){
						$('#p1smallstr').hover(function(){
							$(this).css("background-color", "yellow");
						}, function(){
							$(this).css("background-color", "transparent");
						});
						$('#p1smallstr').click(function(){
							selectCombination('smallstr');
						});
					}
					if(myStatus.largestr == ''){
						$('#p1largestr').hover(function(){
							$(this).css("background-color", "yellow");
						}, function(){
							$(this).css("background-color", "transparent");
						});
						$('#p1largestr').click(function(){
							selectCombination('largestr');
						});
					}
					if(myStatus.yacht == ''){
						$('#p1yacht').hover(function(){
							$(this).css("background-color", "yellow");
						}, function(){
							$(this).css("background-color", "transparent");
						});
						$('#p1yacht').click(function(){
							selectCombination('yacht');
						});
					}
					if(myStatus.choice == ''){
						$('#p1choice').hover(function(){
							$(this).css("background-color", "yellow");
						}, function(){
							$(this).css("background-color", "transparent");
						});
						$('#p1choice').click(function(){
							selectCombination('choice');
						});
					}
					// 주사위 클릭이벤트
					$('[property="dice"]').click(function(){
						if($(this).attr('sel') == 'false'){
							$(this).css({
								'color' : 'grey'
							});
							$(this).attr('sel', 'true');
						}else{
							$(this).css({
								'color' : 'black'
							});
							$(this).attr('sel', 'false');
						}
					});
				}else{
					$('[act="act"]').off('click');
					$('[act="act"]').off('mouseenter mouseleave');
					$('[property="dice"]').off('click');	
					$('[property="dice"]').css({
						'color' : 'black'
					});
					$('[property="dice"]').attr('sel', 'false');
					$('#reroll').css({
						'display' : 'none'
					});
					// 턴 표시
					if(active == 'true'){
						$('#p1name').css({
							'background-color' : 'transparent'
						});
						$('#p2name').css({
							'background-color' : 'green'
						});
					}
				}
			}
			// 상대방 연결 끊김 
			if(header == 'opp_disconnected'){
				alert('상대방이 게임에서 나갔습니다');
			}
		};
		
		// 득점옵션 선택
		function selectCombination(option){
			socket.send('gameID@' + gameID + '@select@' + option);
		}
		
		// 주사위 리롤
		$('#rerollbtn').click(function(){
			let indexes = '';
			for(let i=0; i<5; i++){
				const targetID = 'dice' + (i + 1);
				if($('#' + targetID).attr('sel') == 'true'){
					indexes += i + '/';
				}
			}

			socket.send('gameID@' + gameID + '@reroll@' + indexes);
		});

		// 방 퇴장(기권)
		$(document).on('click', '#exit', function(){
			socket.send('exit@');
		});
		
		// 웹소켓 테스트
		$(document).on('click', '#wsTest', function(){
			socket.send('echo@');
		});
		
		// 서버 상태정보
		$(document).on('click', '#serverStatus', function(){
			socket.send('server_status@');
		});
		
		// 새로고침
		$(document).on('click', '#refresh', function(){
			socket.send('server_status@');
		});
		
	}

</script>
<style type="text/css">
	
	#mainContainer{
		display: none;
		text-align: center;
		justify-content: center;
	}
	
	#dices{
		display: flex;
		text-align: center;
		justify-content: center;
		font-size: 60px;
	}
	
	#game td{
		border: 1px solid;
		width: 400px;
		font-size: 32px;
	}
	
	.room {
		display: relative;
		border: 1px solid;
		width: 800px;
	}
	
	#small {
		font-size: 12px;
	}
	
	#roomInfo {
		right: -10px;
	}
	
	#reroll {
		display: none;
	}
	
</style>
<body>
	<div id="mainContainer">
		<div id="content">
			<div id="dices">
				<div id="rolls">
					<table id="randomDices">
						<tr>
							<td id="dice1" property="dice" sel="false">
								<i class="fa-regular fa-square"></i>
							</td>
							<td id="dice2" property="dice" sel="false">
								<i class="fa-regular fa-square"></i>
							</td>
							<td id="dice3" property="dice" sel="false">
								<i class="fa-regular fa-square"></i>
							</td>
							<td id="dice4" property="dice" sel="false">
								<i class="fa-regular fa-square"></i>
							</td>
							<td id="dice5" property="dice" sel="false">
								<i class="fa-regular fa-square"></i>
							</td>
						</tr>
					</table>
				</div>
			</div>
			
			<br>
			
			<div id="reroll">
				<button id="rerollbtn"></button>
			</div>

			<br>
			
			<div id="gameStatus">
				<table id="game">
					
					<tr>
						<td></td>
						<td id="p1name" separator="td">player1</td>
						<td id="p2name" separator="td">player2</td>
					</tr>
				
					<tr>
						<td> <i class="fa-solid fa-dice-one"></i> Aces</td>
						<td id="p1aces" act="act" separator="td"></td>
						<td id="p2aces" separator="td"></td> 
					</tr>
					
					<tr>
						<td> <i class="fa-solid fa-dice-two"></i> Twos</td>
						<td id="p1twos" act="act" separator="td"></td>
						<td id="p2twos" separator="td"></td>
					</tr>
					
					<tr>
						<td> <i class="fa-solid fa-dice-three"></i> Threes</td>
						<td id="p1threes" act="act" separator="td"></td>
						<td id="p2threes" separator="td"></td>
					</tr>
					
					<tr>
						<td> <i class="fa-solid fa-dice-four"></i> Fours</td>
						<td id="p1fours" act="act" separator="td"></td>
						<td id="p2fours" separator="td"></td>
					</tr>
					
					<tr>
						<td> <i class="fa-solid fa-dice-five"></i> Fives</td>
						<td id="p1fives" act="act" separator="td"></td>
						<td id="p2fives" separator="td"></td>
					</tr>
					
					<tr>
						<td> <i class="fa-solid fa-dice-six"></i> Sixes</td>
						<td id="p1sixes" act="act" separator="td"></td>
						<td id="p2sixes" separator="td"></td>
					</tr>
					
					<tr id="bonus">
						<td>Bonus</td>
						<td id="p1bonus" separator="td"></td>
						<td id="p2bonus" separator="td"></td>
					</tr>
					
					<tr>
						<td>4 of a kind</td>
						<td id="p14ofakind" act="act" separator="td"></td>
						<td id="p24ofakind" separator="td"></td>
					</tr>
					
					<tr>
						<td>Full House</td>
						<td id="p1fullhouse" act="act" separator="td"></td>
						<td id="p2fullhouse" separator="td"></td>
					</tr>
					
					<tr>
						<td>Small straight</td>
						<td id="p1smallstr" act="act" separator="td"></td>
						<td id="p2smallstr" separator="td"></td>
					</tr>
					
					<tr>
						<td>Large straight</td>
						<td id="p1largestr" act="act" separator="td"></td>
						<td id="p2largestr" separator="td"></td>
					</tr>
					
					<tr>
						<td>Yacht</td>
						<td id="p1yacht" act="act" separator="td"></td>
						<td id="p2yacht" separator="td"></td>
					</tr>
					
					<tr>
						<td>Choice</td>
						<td id="p1choice" act="act" separator="td"></td>
						<td id="p2choice" separator="td"></td>
					</tr>
					
					<tr id="total">
						<td>Total</td>
						<td id="p1total" separator="td"></td>
						<td id="p2total" separator="td"></td>
					</tr>
				</table>
			</div>
			
			<button id="wsTest">웹소켓 테스트</button>
			<button id="serverStatus">서버 상태정보 테스트</button>
			<button id="exit">게임방 나가기</button>
		</div>
	</div>
</body>
</html>