<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Test Map<String, Object> Binding</title>
</head>
<body>
    <h2>Test Map<String, Object> Parameter Binding</h2>
    <form action="mapparam" method="post">
        <label for="name">Name:</label>
        <input type="text" id="name" name="name"><br><br>
        <label for="age">Age:</label>
        <input type="number" id="age" name="age"><br><br>
        <label for="email">Email:</label>
        <input type="email" id="email" name="email"><br><br>
        <label for="colors">Favorite Colors:</label>
        <select id="colors" name="colors" multiple>
            <option value="Red">Red</option>
            <option value="Green">Green</option>
            <option value="Blue">Blue</option>
            <option value="Yellow">Yellow</option>
        </select><br><br>

        <label>
            <input type="checkbox" name="fruits[]" value="apple"> Apple
        </label>
        <label>
            <input type="checkbox" name="fruits[]" value="banana"> Banana
        </label>
        <label>
            <input type="checkbox" name="fruits[]" value="orange"> Orange
        </label>
        <input type="submit" value="Submit">
    </form>
    <hr>
    <h3>Test with Path Variable</h3>
    <form action="mapparam/123" method="post">
        <label for="name2">Name:</label>
        <input type="text" id="name2" name="name"><br><br>
        <label for="age2">Age:</label>
        <input type="number" id="age2" name="age"><br><br>
        <input type="submit" value="Submit with Path">
    </form>
</body>
</html>
