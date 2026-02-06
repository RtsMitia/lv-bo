<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Bean Binding Test</title>
</head>
<body>
    <h2>Bean Binding: Employee and Department</h2>
    <form action="bean" method="post">
        <fieldset>
            <legend>Employee (prefix `e.`)</legend>
            <label for="ename">Name:</label>
            <input type="text" id="ename" name="e.name"><br><br>
            <label for="eage">Age:</label>
            <input type="number" id="eage" name="e.age"><br><br>
            <label for="eemail">Email:</label>
            <input type="email" id="eemail" name="e.email"><br><br>
        </fieldset>
        <fieldset>
            <legend>Department (prefix `d.`)</legend>
            <label for="dlabel">Label:</label>
            <input type="text" id="dlabel" name="d.label"><br><br>
            <label for="dfloor">Floor:</label>
            <input type="number" id="dfloor" name="d.floor"><br><br>
        </fieldset>
        <input type="submit" value="Submit">
    </form>

    <hr>
    <h3>Single bean test (Employee)</h3>
    <form action="bean/single" method="post">
        <label for="sname">Name:</label>
        <input type="text" id="sname" name="e.name"><br><br>
        <label for="sage">Age:</label>
        <input type="number" id="sage" name="e.age"><br><br>
        <input type="submit" value="Submit Employee">
    </form>
</body>
</html>
