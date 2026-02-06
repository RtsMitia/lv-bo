<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>JSON Endpoint Test</title>
</head>
<body>
    <h1>JSON Endpoint Test</h1>
    <button id="btnObj">GET /Test/json/object</button>
    <pre id="outObj"></pre>

    <button id="btnMv">GET /Test/json/modelview</button>
    <pre id="outMv"></pre>

    <script>
        document.getElementById('btnObj').addEventListener('click', () => {
            fetch('Test/json/object')
                .then(r => r.json())
                .then(j => document.getElementById('outObj').textContent = JSON.stringify(j, null, 2))
                .catch(e => document.getElementById('outObj').textContent = e);
        });

        document.getElementById('btnMv').addEventListener('click', () => {
            fetch('Test/json/modelview')
                .then(r => r.json())
                .then(j => document.getElementById('outMv').textContent = JSON.stringify(j, null, 2))
                .catch(e => document.getElementById('outMv').textContent = e);
        });
    </script>
</body>
</html>