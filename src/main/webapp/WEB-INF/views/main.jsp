<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<script type="text/javascript">

	window.onload = function(){
		
		const socket = new WebSocket("ws://localhost:8080/yahtzeeWS");
		socket.send("hi");
		
	}
	
</script>
<body>

		main.jsp
		
	<br>
	
	<a href="/yahtzee">야찌</a>	

</body>
</html>