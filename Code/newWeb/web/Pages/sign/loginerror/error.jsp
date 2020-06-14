<%--
  Created by IntelliJ IDEA.
  User: adiko
  Date: 04/10/2019
  Time: 17:28
  To change this template use File | Settings | File Templates.
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<%@page import="utils.*" %>
<%@ page import="constants.Constants" %>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>MAG.i.t</title>
    <!--        Link the Bootstrap (from twitter) CSS framework in order to use its classes-->
    <link rel="stylesheet" href="../../common/bootstrap.min.css"/>
<%--    <link rel="stylesheet" type="text/css" href="css/LoginCss.css"/>--%>

    <!--        Link jQuery JavaScript library in order to use the $ (jQuery) method-->
    <!--        <script src="script/jquery-2.0.3.min.js"></script>-->
    <!--        and\or any other scripts you might need to operate the JSP file behind the scene once it arrives to the client-->
</head>
<body>
<div class="container">
    <% String usernameFromSession = SessionUtils.getUsername(request);%>
    <% String usernameFromParameter = request.getParameter(Constants.USERNAME) != null ? request.getParameter(Constants.USERNAME) : "";%>
    <% if (usernameFromSession == null) {%>
    <h1>Welcome to the MAG.i.t</h1>
    <br/>
    <h2>Please enter a unique user name:</h2>
    <form method="GET" action="login">
        <input type="text" name="<%=Constants.USERNAME%>" value="<%=usernameFromParameter%>"/>
        <input type="submit" value="Login"/>
    </form>
    <% Object errorMessage = request.getAttribute(Constants.USER_NAME_ERROR);%>
    <% if (errorMessage != null) {%>
    <span class="bg-danger" style="color:red;"><%=errorMessage%></span>
    <% } %>
    <% } else {%>
    <h1>Welcome back, <%=usernameFromSession%></h1>
    <a href="../chatroom/chatroom.html">Click here to enter Your own space</a>
    <br/>
    <a href="login?logout=true" id="logout">logout</a>
    <% }%>
</div>
</body>
</html>
