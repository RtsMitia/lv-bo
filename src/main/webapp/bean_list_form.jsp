<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Bean List/Array Binding Test</title>
    <style>
        fieldset { margin: 10px 0; padding: 10px; }
        legend { font-weight: bold; }
        .employee-group { background: #f0f0f0; padding: 10px; margin: 5px 0; }
    </style>
</head>
<body>
    <h2>Bean List/Array Binding Test</h2>
    
    <h3>Test 1: List&lt;Employee&gt;</h3>
    <form action="bean/list" method="post">
        <div class="employee-group">
            <strong>Employee [0]</strong><br>
            <label>Name:</label>
            <input type="text" name="employees[0].name" placeholder="Alice"><br>
            <label>Age:</label>
            <input type="number" name="employees[0].age" placeholder="25"><br>
            <label>Email:</label>
            <input type="email" name="employees[0].email" placeholder="alice@example.com"><br>
        </div>
        
        <div class="employee-group">
            <strong>Employee [1]</strong><br>
            <label>Name:</label>
            <input type="text" name="employees[1].name" placeholder="Bob"><br>
            <label>Age:</label>
            <input type="number" name="employees[1].age" placeholder="30"><br>
            <label>Email:</label>
            <input type="email" name="employees[1].email" placeholder="bob@example.com"><br>
        </div>
        
        <div class="employee-group">
            <strong>Employee [2]</strong><br>
            <label>Name:</label>
            <input type="text" name="employees[2].name" placeholder="Charlie"><br>
            <label>Age:</label>
            <input type="number" name="employees[2].age" placeholder="28"><br>
            <label>Email:</label>
            <input type="email" name="employees[2].email" placeholder="charlie@example.com"><br>
        </div>
        
        <label>Popo:</label>
        <input type="number" name="popo" placeholder="123"><br>
        
        <input type="submit" value="Submit List">
    </form>

    <hr>

    <h3>Test 2: Employee[] Array</h3>
    <form action="bean/array" method="post">
        <div class="employee-group">
            <strong>Employee [0]</strong><br>
            <label>Name:</label>
            <input type="text" name="employees[0].name" placeholder="David"><br>
            <label>Age:</label>
            <input type="number" name="employees[0].age" placeholder="35"><br>
            <label>Email:</label>
            <input type="email" name="employees[0].email" placeholder="david@example.com"><br>
        </div>
        
        <div class="employee-group">
            <strong>Employee [1]</strong><br>
            <label>Name:</label>
            <input type="text" name="employees[1].name" placeholder="Eve"><br>
            <label>Age:</label>
            <input type="number" name="employees[1].age" placeholder="27"><br>
            <label>Email:</label>
            <input type="email" name="employees[1].email" placeholder="eve@example.com"><br>
        </div>
        
        <input type="submit" value="Submit Array">
    </form>
</body>
</html>
