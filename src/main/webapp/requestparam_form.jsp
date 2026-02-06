<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Test @RequestParam Form</title>
</head>
<body>
    <h1>Test @RequestParam binding</h1>
    <form action="${pageContext.request.contextPath}/requestparam/insert" method="get">
        <label for="name">Name:</label>
        <input id="name" name="name" type="text" />
        <br/>
        <label for="age">Age:</label>
        <input id="age" name="age" type="number" />
        <br/>
        <button type="submit">Test insert (@RequestParam on both)</button>
    </form>
    <hr/>
    <form action="${pageContext.request.contextPath}/requestparam/onlyage" method="post">
        <label for="age2">Age:</label>
        <input id="age2" name="age" type="number" />
        <br/>
        <button type="submit">Test onlyAge (@RequestParam only)</button>
    </form>
    <hr/>
    <form action="${pageContext.request.contextPath}/requestparam/mixed" method="post">
        <label for="name3">Name:</label>
        <input id="name3" name="name" type="text" />
        <br/>
        <label for="age3">Age:</label>
        <input id="age3" name="age" type="number" />
        <br/>
        <button type="submit">Test mixed (name by param, age by @RequestParam)</button>
    </form>
    <hr/>
    <form action="${pageContext.request.contextPath}/requestparam/list" method="post">
        <fieldset>
            <legend>Choose colors (multiple):</legend>
            <input type="checkbox" id="color_red" name="colors" value="red">
            <label for="color_red">Red</label>
            <input type="checkbox" id="color_green" name="colors" value="green">
            <label for="color_green">Green</label>
            <input type="checkbox" id="color_blue" name="colors" value="blue">
            <label for="color_blue">Blue</label>
            <input type="checkbox" id="color_yellow" name="colors" value="yellow">
            <label for="color_yellow">Yellow</label>
        </fieldset>
        <button type="submit">Test list (checkboxes for List&lt;String&gt;)</button>
    </form>
    <p>Each form submits to a different test handler in <code>TestRequestParam</code>.</p>
</body>
</html>
