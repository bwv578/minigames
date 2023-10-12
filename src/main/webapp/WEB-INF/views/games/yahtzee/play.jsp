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
			
			// 게임 진행정보 수신
			if(header == 'game_status'){
				
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
						<td id="p1">player1</td>
						<td id="p2">player2</td>
					</tr>
				
					<tr>
						<td> <i class="fa-solid fa-dice-one"></i> Aces</td>
						<td id="p1"></td>
						<td id="p2"></td> 
					</tr>
					
					<tr>
						<td> <i class="fa-solid fa-dice-two"></i> Tows</td>
						<td id="p1"></td>
						<td id="p2"></td>
					</tr>
					
					<tr>
						<td> <i class="fa-solid fa-dice-three"></i> Threes</td>
						<td id="p1"></td>
						<td id="p2"></td>
					</tr>
					
					<tr>
						<td> <i class="fa-solid fa-dice-four"></i> Fours</td>
						<td id="p1"></td>
						<td id="p2"></td>
					</tr>
					
					<tr>
						<td> <i class="fa-solid fa-dice-five"></i> Fives</td>
						<td id="p1"></td>
						<td id="p2"></td>
					</tr>
					
					<tr>
						<td> <i class="fa-solid fa-dice-six"></i> Sixes</td>
						<td id="p1"></td>
						<td id="p2"></td>
					</tr>
					
					<tr>
						<td colspan="3">&nbsp;</td>
					</tr>
					
					<tr>
						<td>4 of a kind</td>
						<td id="p1"></td>
						<td id="p2"></td>
					</tr>
					
					<tr>
						<td>Full House</td>
						<td id="p1"></td>
						<td id="p2"></td>
					</tr>
					
					<tr>
						<td>Small straight</td>
						<td id="p1"></td>
						<td id="p2"></td>
					</tr>
					
					<tr>
						<td>Large straight</td>
						<td id="p1"></td>
						<td id="p2"></td>
					</tr>
					
					<tr>
						<td>Yacht</td>
						<td id="p1"></td>
						<td id="p2"></td>
					</tr>
					
					<tr>
						<td>Choice</td>
						<td id="p1"></td>
						<td id="p2"></td>
					</tr>
				</table>
			</div>
		</div>
	</div>
</body>
</html>