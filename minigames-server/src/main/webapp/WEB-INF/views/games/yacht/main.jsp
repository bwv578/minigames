<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<script type="text/javascript">
</script>
<style type="text/css">

	#contentContainer{
		display: flex;
		justify-content: center;
  		align-items: center; 
  		margin-top: 50px;
	}
	
	#roomsContainer{
		width: 800px;
	}
	
	#header{
		font-size: 48px; /* 원하는 글자 크기 설정 */
  		font-weight: bold; /* 굵게 표시 */
  		font-family: "Helvetica Neue", Helvetica, Arial, sans-serif; /* 눈에 띄는 폰트 설정 */
  		color: #333; /* 텍스트 색상 설정 */
	}
	
	#rooms{
		font-size: 24px; 
	}
	
	#myInfo{
		font-size: 12px;
		display: flex;
		flex-direction: column;
		float: right;
		margin-top: 30px;
	}
	
	hr {
  		margin: 10px 0; /* 위 아래 모두 10px 여백 설정 */
	}
</style>
<body>
	<div id="contentContainer"> 
		<div id="roomsContainer">
			<b id="header">Yacht</b>
			<div id="myInfo">
				<div id="info-wrapper">
					<b>내 닉네임:</b> <a id="name"></a>&nbsp;
					<button id="changeName">변경하기</button>
				</div>
			</div>
			<hr>
			<button id="mkRoom">방 만들기</button>
			<button id="refresh">새로고침</button>
			
			<br><br>
			
			<div id="rooms"></div>
		</div>
	</div>
</body>
</html>