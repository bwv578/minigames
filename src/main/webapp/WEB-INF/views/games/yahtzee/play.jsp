<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="main.jsp" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<!-- Jquery -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.4/jquery.min.js"></script>
<script src="https://kit.fontawesome.com/b908b5678e.js" crossorigin="anonymous"></script>
<script type="text/javascript">

	window.onload = function(){
	
		const socket = new WebSocket("ws://localhost:8080/yahtzeeWS");
		//const socket = new WebSocket("ws://52.78.178.113:8080/yahtzeeWS");
		
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
			if(header == 'rooms'){
				$('#rooms').empty();
				const rooms = JSON.parse(msg.split('@')[1]);
				
				let roomsTable = '<table id="roomList">';
				for(let index=0; index<rooms.length; index++){
					const room = rooms[index];

					roomsTable += '<tr><td id="room" gameID="' + room.gameID + '">';
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
				let myTurn = false;
				if(first == 'true' && (turn % 2) == 1){
					myTurn = true;
				}else if(first == 'false' && (turn % 2) == 0){
					myTurn = true;
				}
				let myName = '';
				let oppName = '';
				let myStatus = '';
				let oppStatus = '';
				
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
				
				// 주사위
				for(let index=0; index<=4; index++){
					const targetID = 'dice' + (index + 1);
					const diceNum = dice[index];

					if(diceNum == 1){
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
				
				// 3 of a kind
				$('#p13ofakind').html(myStatus.threeofakind);
				$('#p23ofakind').html(oppStatus.threeofakind);
				
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
				
				// yatch
				$('#p1yatch').html(myStatus.yatch);
				$('#p2yatch').html(oppStatus.yatch);
				
				// choice
				$('#p1choice').html(myStatus.choice);
				$('#p2choice').html(oppStatus.choice);
				
				// 나의 턴일때
				if(myTurn){
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
					if(myStatus.threeofakind == ''){
						$('#p13ofakind').hover(function(){
							$(this).css("background-color", "yellow");
						}, function(){
							$(this).css("background-color", "transparent");
						});
						$('#p13ofakind').click(function(){
							selectCombination('3ofakind');
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
					if(myStatus.yatch == ''){
						$('#p1yatch').hover(function(){
							$(this).css("background-color", "yellow");
						}, function(){
							$(this).css("background-color", "transparent");
						});
						$('#p1yatch').click(function(){
							selectCombination('yatch');
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
				}
			}
			
		};
		
		// 득점옵션 선택
		function selectCombination(option){
			socket.send('gameID@' + gameID + '@select@' + option);
		}
		
		// 방 입장
		$(document).on('click', '#room', function() {
			socket.send('enter@' + $(this).attr('gameID'));
			$(document).off('click', '#room');
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
		font-size: 52px;
	}
	
	#game td{
		border: 1px solid;
		width: 400px;
		font-size: 32px;
	}
	
	#room {
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
	
</style>
<body>
	<div id="mainContainer">
		<div id="content">
			<div id="dices">
				<div id="rolls">
					<table id="randomDices">
						<tr>
							<td id="dice1"><i class="fa-regular fa-square"></i></td>
							<td id="dice2"><i class="fa-regular fa-square"></i></td>
							<td id="dice3"><i class="fa-regular fa-square"></i></td>
							<td id="dice4"><i class="fa-regular fa-square"></i></td>
							<td id="dice5"><i class="fa-regular fa-square"></i></td>
						</tr>
					</table>
				</div>
			</div>
			
			<br>
			
			<div id="reroll">
				<button>다시굴리기</button>
			</div>

			<br>
			
			<div id="gameStatus">
				<table id="game">
					
					<tr>
						<td></td>
						<td id="p1name">player1</td>
						<td id="p2name">player2</td>
					</tr>
				
					<tr>
						<td> <i class="fa-solid fa-dice-one"></i> Aces</td>
						<td id="p1aces"></td>
						<td id="p2aces"></td> 
					</tr>
					
					<tr>
						<td> <i class="fa-solid fa-dice-two"></i> Tows</td>
						<td id="p1twos"></td>
						<td id="p2twos"></td>
					</tr>
					
					<tr>
						<td> <i class="fa-solid fa-dice-three"></i> Threes</td>
						<td id="p1threes"></td>
						<td id="p2threes"></td>
					</tr>
					
					<tr>
						<td> <i class="fa-solid fa-dice-four"></i> Fours</td>
						<td id="p1fours"></td>
						<td id="p2fours"></td>
					</tr>
					
					<tr>
						<td> <i class="fa-solid fa-dice-five"></i> Fives</td>
						<td id="p1fives"></td>
						<td id="p2fives"></td>
					</tr>
					
					<tr>
						<td> <i class="fa-solid fa-dice-six"></i> Sixes</td>
						<td id="p1sixes"></td>
						<td id="p2sixes"></td>
					</tr>
					
					<tr>
						<td colspan="3">&nbsp;</td>
					</tr>
					
					<tr>
						<td>3 of a kind</td>
						<td id="p13ofakind"></td>
						<td id="p23ofakind"></td>
					</tr>
					
					<tr>
						<td>4 of a kind</td>
						<td id="p14ofakind"></td>
						<td id="p24ofakind"></td>
					</tr>
					
					<tr>
						<td>Full House</td>
						<td id="p1fullhouse"></td>
						<td id="p2fullhouse"></td>
					</tr>
					
					<tr>
						<td>Small straight</td>
						<td id="p1smallstr"></td>
						<td id="p2smallstr"></td>
					</tr>
					
					<tr>
						<td>Large straight</td>
						<td id="p1largestr"></td>
						<td id="p2largestr"></td>
					</tr>
					
					<tr>
						<td>Yacht</td>
						<td id="p1yatch"></td>
						<td id="p2yatch"></td>
					</tr>
					
					<tr>
						<td>Choice</td>
						<td id="p1choice"></td>
						<td id="p2choice"></td>
					</tr>
				</table>
			</div>
		</div>
	</div>
</body>
</html>