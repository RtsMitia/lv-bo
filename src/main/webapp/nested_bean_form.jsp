<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Nested Bean Binding Test</title>
    <style>
        fieldset { margin: 10px 0; padding: 10px; border: 2px solid #333; }
        legend { font-weight: bold; color: #333; }
        .nested { background: #f9f9f9; margin: 5px 0; padding: 10px; border: 1px solid #ccc; }
        label { display: inline-block; width: 150px; }
        input { margin: 5px 0; }
    </style>
</head>
<body>
    <h2>Nested Bean Binding Tests</h2>
    
    <!-- Test 1: Single employee with nested department -->
    <h3>Test 1: Employee with nested Department</h3>
    <form action="bean/nested" method="post">
        <fieldset>
            <legend>Employee</legend>
            <label>Name:</label><input type="text" name="employee.name" placeholder="Alice"><br>
            <label>Age:</label><input type="number" name="employee.age" placeholder="25"><br>
            <label>Email:</label><input type="email" name="employee.email" placeholder="alice@example.com"><br>
            
            <div class="nested">
                <strong>Department (nested)</strong><br>
                <label>Label:</label><input type="text" name="employee.department.label" placeholder="Engineering"><br>
                <label>Floor:</label><input type="number" name="employee.department.floor" placeholder="3"><br>
            </div>
        </fieldset>
        <input type="submit" value="Submit Nested Bean">
    </form>

    <hr>

    <!-- Test 2: List of employees with nested departments -->
    <h3>Test 2: List&lt;Employee&gt; with nested Departments</h3>
    <form action="bean/nested/list" method="post">
        <fieldset>
            <legend>Employee [0]</legend>
            <label>Name:</label><input type="text" name="employees[0].name" placeholder="Bob"><br>
            <label>Age:</label><input type="number" name="employees[0].age" placeholder="30"><br>
            <label>Email:</label><input type="email" name="employees[0].email" placeholder="bob@example.com"><br>
            
            <div class="nested">
                <strong>Department (nested)</strong><br>
                <label>Label:</label><input type="text" name="employees[0].department.label" placeholder="Sales"><br>
                <label>Floor:</label><input type="number" name="employees[0].department.floor" placeholder="2"><br>
            </div>
        </fieldset>

        <fieldset>
            <legend>Employee [1]</legend>
            <label>Name:</label><input type="text" name="employees[1].name" placeholder="Charlie"><br>
            <label>Age:</label><input type="number" name="employees[1].age" placeholder="28"><br>
            <label>Email:</label><input type="email" name="employees[1].email" placeholder="charlie@example.com"><br>
            
            <div class="nested">
                <strong>Department (nested)</strong><br>
                <label>Label:</label><input type="text" name="employees[1].department.label" placeholder="Marketing"><br>
                <label>Floor:</label><input type="number" name="employees[1].department.floor" placeholder="1"><br>
            </div>
        </fieldset>
        
        <input type="submit" value="Submit List with Nested">
    </form>

    <hr>

    <!-- Test 3: Deep nesting - Employee -> Department -> Project[] -->
    <h3>Test 3: Deep Nesting (Employee → Department → Projects[])</h3>
    <form action="bean/deep" method="post">
        <fieldset>
            <legend>Employee</legend>
            <label>Name:</label><input type="text" name="employee.name" placeholder="David"><br>
            <label>Age:</label><input type="number" name="employee.age" placeholder="35"><br>
            <label>Email:</label><input type="email" name="employee.email" placeholder="david@example.com"><br>
            
            <div class="nested">
                <strong>Department</strong><br>
                <label>Label:</label><input type="text" name="employee.department.label" placeholder="R&D"><br>
                <label>Floor:</label><input type="number" name="employee.department.floor" placeholder="5"><br>
                
                <div class="nested" style="background: #e9e9e9;">
                    <strong>Project [0]</strong><br>
                    <label>Name:</label><input type="text" name="employee.department.projects[0].name" placeholder="Project Alpha"><br>
                    <label>Budget:</label><input type="text" name="employee.department.projects[0].budget" placeholder="100000"><br>
                </div>
                
                <div class="nested" style="background: #e9e9e9;">
                    <strong>Project [1]</strong><br>
                    <label>Name:</label><input type="text" name="employee.department.projects[1].name" placeholder="Project Beta"><br>
                    <label>Budget:</label><input type="text" name="employee.department.projects[1].budget" placeholder="150000"><br>
                </div>
            </div>
        </fieldset>
        <input type="submit" value="Submit Deep Nested">
    </form>
</body>
</html>
