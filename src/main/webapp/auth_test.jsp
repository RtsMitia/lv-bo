<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Authorization & Role Test</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 1200px;
            margin: 20px auto;
            padding: 20px;
        }
        .container {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
        }
        .section {
            border: 1px solid #ccc;
            padding: 15px;
            border-radius: 5px;
            background: #f9f9f9;
        }
        h2 {
            color: #333;
            border-bottom: 2px solid #007bff;
            padding-bottom: 5px;
        }
        h3 {
            color: #555;
            margin-top: 0;
        }
        .form-group {
            margin-bottom: 15px;
        }
        label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
        }
        input, select {
            width: 100%;
            padding: 8px;
            border: 1px solid #ddd;
            border-radius: 3px;
            box-sizing: border-box;
        }
        button {
            background: #007bff;
            color: white;
            border: none;
            padding: 10px 20px;
            border-radius: 3px;
            cursor: pointer;
            margin-right: 5px;
        }
        button:hover {
            background: #0056b3;
        }
        button.danger {
            background: #dc3545;
        }
        button.danger:hover {
            background: #c82333;
        }
        button.success {
            background: #28a745;
        }
        button.success:hover {
            background: #218838;
        }
        .output {
            margin-top: 15px;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 3px;
            background: white;
            min-height: 60px;
            font-family: monospace;
            white-space: pre-wrap;
        }
        .status {
            padding: 10px;
            border-radius: 3px;
            margin-bottom: 15px;
        }
        .status.success {
            background: #d4edda;
            border: 1px solid #c3e6cb;
            color: #155724;
        }
        .status.error {
            background: #f8d7da;
            border: 1px solid #f5c6cb;
            color: #721c24;
        }
        .endpoint-list {
            list-style: none;
            padding: 0;
        }
        .endpoint-list li {
            margin-bottom: 10px;
        }
        .info {
            background: #d1ecf1;
            border: 1px solid #bee5eb;
            color: #0c5460;
            padding: 10px;
            border-radius: 3px;
            margin-bottom: 15px;
        }
    </style>
</head>
<body>
    <h1>🔐 Authorization & Role-Based Access Control Test</h1>

    <div id="currentStatus" class="info">
        <strong>Current Session:</strong> <span id="statusText">Loading...</span>
    </div>

    <div class="container">
        <!-- Login Section -->
        <div class="section">
            <h2>1. Authentication</h2>
            
            <h3>Login</h3>
            <form id="loginForm">
                <div class="form-group">
                    <label for="username">Username:</label>
                    <input type="text" id="username" name="username" placeholder="Enter username" required>
                </div>
                <div class="form-group">
                    <label for="role">Roles (comma-separated):</label>
                    <input type="text" id="role" name="role" placeholder="e.g., admin, sale-manager" required>
                </div>
                <button type="submit" class="success">Login</button>
            </form>

            <h3>Quick Login Presets</h3>
            <button onclick="quickLogin('john', 'admin')" class="success">Login as Admin</button>
            <button onclick="quickLogin('jane', 'sale-manager')" class="success">Login as Sales Manager</button>
            <button onclick="quickLogin('bob', 'manager')" class="success">Login as Manager</button>
            <button onclick="quickLogin('alice', 'user')" class="success">Login as Regular User</button>

            <h3>Logout</h3>
            <button onclick="logout()" class="danger">Logout</button>

            <div id="authOutput" class="output"></div>
        </div>

        <!-- Test Endpoints Section -->
        <div class="section">
            <h2>2. Test Endpoints</h2>
            
            <h3>Public Endpoint (No Auth Required)</h3>
            <button onclick="testEndpoint('/Test/auth/public')">Test Public</button>

            <h3>Protected Endpoint (@Authorized)</h3>
            <button onclick="testEndpoint('/Test/auth/protected')">Test Protected</button>
            <p style="font-size: 12px; color: #666;">Requires: Login only (any role)</p>

            <h3>Admin Endpoint (@Role("admin"))</h3>
            <button onclick="testEndpoint('/Test/auth/admin')">Test Admin</button>
            <p style="font-size: 12px; color: #666;">Requires: admin role</p>

            <h3>Sales Endpoint (@Role("admin, sale-manager"))</h3>
            <button onclick="testEndpoint('/Test/auth/sales')">Test Sales</button>
            <p style="font-size: 12px; color: #666;">Requires: admin OR sale-manager role</p>

            <h3>Manager Endpoint (@Role("manager") + @JSON)</h3>
            <button onclick="testEndpoint('/Test/auth/manager')">Test Manager (JSON)</button>
            <p style="font-size: 12px; color: #666;">Requires: manager role, returns JSON</p>

            <h3>Case-Insensitive Test (@Role("ADMIN"))</h3>
            <button onclick="testEndpoint('/Test/auth/case-test')">Test Case Insensitive</button>
            <p style="font-size: 12px; color: #666;">Role "admin" should match "ADMIN"</p>

            <div id="testOutput" class="output"></div>
        </div>
    </div>

    <div class="section" style="margin-top: 20px;">
        <h2>📋 Test Scenarios</h2>
        <ul class="endpoint-list">
            <li><strong>Scenario 1:</strong> Try accessing protected endpoints without logging in → Should get 403 Forbidden</li>
            <li><strong>Scenario 2:</strong> Login as regular user → Can access @Authorized but not @Role endpoints</li>
            <li><strong>Scenario 3:</strong> Login as admin → Can access everything</li>
            <li><strong>Scenario 4:</strong> Login as sale-manager → Can access sales endpoint but not admin-only</li>
            <li><strong>Scenario 5:</strong> Test case-insensitive role matching (admin vs ADMIN)</li>
            <li><strong>Scenario 6:</strong> Logout and verify access is revoked</li>
        </ul>
    </div>

    <script>
        const ctx = '${pageContext.request.contextPath}';

        // Load status on page load
        window.onload = function() {
            checkStatus();
        };

        // Check current authentication status
        function checkStatus() {
            fetch(ctx + '/Test/auth/status')
                .then(r => r.json())
                .then(data => {
                    const statusDiv = document.getElementById('statusText');
                    if (data.data.loggedIn) {
                        statusDiv.innerHTML = '✅ Logged in as <strong>' + data.data.user + '</strong> with roles: <strong>' + JSON.stringify(data.data.roles) + '</strong>';
                        document.getElementById('currentStatus').className = 'status success';
                    } else {
                        statusDiv.innerHTML = '❌ Not logged in';
                        document.getElementById('currentStatus').className = 'status error';
                    }
                })
                .catch(err => {
                    document.getElementById('statusText').innerHTML = '⚠️ Error checking status';
                });
        }

        // Handle login form
        document.getElementById('loginForm').onsubmit = function(e) {
            e.preventDefault();
            const formData = new FormData(this);
            
            fetch(ctx + '/Test/auth/login', {
                method: 'POST',
                body: formData
            })
            .then(r => r.text())
            .then(text => {
                document.getElementById('authOutput').innerHTML = '✅ ' + text;
                checkStatus();
            })
            .catch(err => {
                document.getElementById('authOutput').innerHTML = '❌ Error: ' + err;
            });
        };

        // Quick login
        function quickLogin(username, role) {
            const formData = new FormData();
            formData.append('username', username);
            formData.append('role', role);
            
            fetch(ctx + '/Test/auth/login', {
                method: 'POST',
                body: formData
            })
            .then(r => r.text())
            .then(text => {
                document.getElementById('authOutput').innerHTML = '✅ ' + text;
                checkStatus();
            })
            .catch(err => {
                document.getElementById('authOutput').innerHTML = '❌ Error: ' + err;
            });
        }

        // Logout
        function logout() {
            fetch(ctx + '/Test/auth/logout')
                .then(r => r.text())
                .then(text => {
                    document.getElementById('authOutput').innerHTML = '✅ ' + text;
                    checkStatus();
                })
                .catch(err => {
                    document.getElementById('authOutput').innerHTML = '❌ Error: ' + err;
                });
        }

        // Test endpoint
        function testEndpoint(url) {
            const output = document.getElementById('testOutput');
            output.innerHTML = '⏳ Testing ' + url + '...';
            
            fetch(ctx + url)
                .then(async response => {
                    const text = await response.text();
                    let result = 'Status: ' + response.status + ' ' + response.statusText + '\n\n';
                    
                    if (response.status === 200) {
                        result += '✅ SUCCESS\n\n';
                    } else if (response.status === 403) {
                        result += '🚫 FORBIDDEN\n\n';
                    }
                    
                    result += 'Response:\n' + text;
                    output.innerHTML = result;
                })
                .catch(err => {
                    output.innerHTML = '❌ Error: ' + err;
                });
        }
    </script>
</body>
</html>
