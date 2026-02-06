<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Hello JSP</title>
</head>
<body>
    <h1>Hello JSP page</h1>
    <p>This is a static JSP page. To test the framework action, visit <a href="${pageContext.request.contextPath}/hello">/hello</a></p>
    <p>When you visit <code>/hello</code>, the controller method in <code>com.test.Test</code> returns a String which should be written directly to the response.</p>
</body>
</html>
