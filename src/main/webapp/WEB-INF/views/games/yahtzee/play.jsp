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
		let myStatus = '';
		let oppStatus = '';

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
				let myName = '';
				let oppName = '';
				
				if(first == 'true'){
					myStatus = gameStatus.players[0].status;
					oppStatus = gameStatus.players[1].status;
					myName = gameStatus.players[0].name;
					oppName = gameStatus.players[1].name;
				}else{
					myStatus = gameStatus.players[1].status;
					oppStatus = gameStatus.players[0].status;
					myName = gameStatus.players[1].name;
					oppName = gameStatus.players[2].name;
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
				$('#p1sstraight').html(myStatus.smallstr);
				$('#p2sstraight').html(oppStatus.smallstr);
				
				// large straight
				$('#p1lstraight').html(myStatus.largestr);
				$('#p2lstraight').html(oppStatus.largestr);
				
				// yatch
				$('#p1yatch').html(myStatus.yatch);
				$('#p2yatch').html(oppStatus.yatch);
				
				// choice
				$('#p1choice').html(myStatus.choice);
				$('#p2choice').html(oppStatus.choice);
			}
			
		};
		
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
							<td><i class="fa-regular fa-square"></i></td>
							<td><i class="fa-regular fa-square"></i></td>
							<td><i class="fa-regular fa-square"></i></td>
							<td><i class="fa-regular fa-square"></i></td>
							<td><i class="fa-regular fa-square"></i></td>
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
						<td id="p1sstraight"></td>
						<td id="p2sstraight"></td>
					</tr>
					
					<tr>
						<td>Large straight</td>
						<td id="p1lstraight"></td>
						<td id="p2lstraight"></td>
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