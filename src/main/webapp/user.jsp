<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Document</title>
</head>
<body>
    <h1>User.jsp</h1>

    <ul>
        <% 
            Object usersObj = request.getAttribute("users");
            if (usersObj != null) {
                List<String> users = (List<String>) usersObj;
                for (String user : users) {
        %>
                    <li><%= user %></li>
        <%      
                }
            } else {
        %>
                <li>No users available</li>
        <%
            }
        %>
</body>
</html>