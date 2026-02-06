<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Etudiant Insert Form</title>
</head>
<body>
    <h1>Insert Etudiant</h1>
    <form action="${pageContext.request.contextPath}/test/etudiant/insert" method="post">
        <label for="name">Name:</label>
        <input id="name" name="name" type="text" />
        <br/>
        <label for="age">Age:</label>
        <input id="age" name="age" type="number" />
        <br/>
        <button type="submit">Submit</button>
    </form>
    <p>After submission you should see a plain-text response with the submitted values.</p>
</body>
</html>
