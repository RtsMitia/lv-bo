<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Session Management Test</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .test-section { 
            border: 1px solid #ccc; 
            padding: 15px; 
            margin-bottom: 15px; 
            border-radius: 5px;
        }
        h2 { color: #333; }
        form { margin: 10px 0; }
        input, button { 
            padding: 8px; 
            margin: 5px; 
            font-size: 14px;
        }
        .result { 
            background: #f0f0f0; 
            padding: 10px; 
            margin: 10px 0; 
            border-radius: 3px;
            white-space: pre-wrap;
        }
    </style>
</head>
<body>
    <h1>Session Management Test</h1>

    <div class="test-section">
        <h2>1. Login (Store in Session)</h2>
        <form action="${pageContext.request.contextPath}/Test/session/login" method="post">
            <label>Username: <input name="username" type="text" value="john_doe" /></label><br/>
            <label>Role: <input name="role" type="text" value="admin" /></label><br/>
            <button type="submit">Login</button>
        </form>
    </div>

    <div class="test-section">
        <h2>2. View Session Info</h2>
        <form action="${pageContext.request.contextPath}/Test/session/info" method="get">
            <button type="submit">Show Session Contents</button>
        </form>
    </div>

    <div class="test-section">
        <h2>3. Update Session Value</h2>
        <form action="${pageContext.request.contextPath}/Test/session/update" method="post">
            <label>Key: <input name="key" type="text" value="status" /></label><br/>
            <label>Value: <input name="value" type="text" value="active" /></label><br/>
            <button type="submit">Update Session</button>
        </form>
    </div>

    <div class="test-section">
        <h2>4. Remove from Session</h2>
        <form action="${pageContext.request.contextPath}/Test/session/remove" method="post">
            <label>Key: <input name="key" type="text" value="status" /></label><br/>
            <button type="submit">Remove</button>
        </form>
    </div>

    <div class="test-section">
        <h2>5. Page Counter</h2>
        <form action="${pageContext.request.contextPath}/Test/session/counter" method="get">
            <button type="submit">Increment Counter</button>
        </form>
        <p><em>Click multiple times to see counter increase</em></p>
    </div>

    <div class="test-section">
        <h2>6. Session as JSON</h2>
        <button onclick="fetchSessionJson()">Get Session JSON</button>
        <div class="result" id="json-result">Click button to fetch JSON...</div>
    </div>

    <div class="test-section">
        <h2>7. Clear Session</h2>
        <form action="${pageContext.request.contextPath}/Test/session/clear" method="post">
            <button type="submit" style="background: #d9534f; color: white;">Clear All Session Data</button>
        </form>
    </div>

    <script>
        async function fetchSessionJson() {
            try {
                const response = await fetch('${pageContext.request.contextPath}/Test/session/json');
                const data = await response.json();
                document.getElementById('json-result').textContent = JSON.stringify(data, null, 2);
            } catch (error) {
                document.getElementById('json-result').textContent = 'Error: ' + error.message;
            }
        }
    </script>
</body>
</html>
