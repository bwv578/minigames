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
	}
	
	#roomsContainer{
		width: 800px;
	}
	
	#rooms{
		font-size: 24px; 
	}
</style>
<body>
	<div id="contentContainer"> 
		<div id="roomsContainer">
			<h1>yacht main</h1> 
			<hr>
			<button id="mkRoom">방 만들기</button>
			<button id="refresh">새로고침</button>
			
			<br><br>
			
			<div id="rooms"></div>
		</div>
	</div>
</body>
</html>