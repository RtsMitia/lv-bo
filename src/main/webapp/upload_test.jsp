<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>File Upload Test</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 50px auto;
            padding: 20px;
        }
        .test-section {
            border: 1px solid #ddd;
            padding: 20px;
            margin-bottom: 20px;
            border-radius: 5px;
        }
        h2 {
            color: #333;
            margin-top: 0;
        }
        input[type="file"], input[type="text"] {
            margin: 10px 0;
            padding: 5px;
        }
        button {
            background-color: #4CAF50;
            color: white;
            padding: 10px 20px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            margin-top: 10px;
        }
        button:hover {
            background-color: #45a049;
        }
        .result {
            margin-top: 20px;
            padding: 10px;
            background-color: #f5f5f5;
            border-radius: 4px;
            white-space: pre-wrap;
        }
    </style>
</head>
<body>
    <h1>File Upload Tests</h1>

    <!-- Test 1: Single file upload -->
    <div class="test-section">
        <h2>1. Single File Upload</h2>
        <form id="form1" enctype="multipart/form-data">
            <input type="file" name="avatar" required><br>
            <button type="submit">Upload File</button>
        </form>
        <div id="result1" class="result"></div>
    </div>

    <!-- Test 2: Multiple files upload -->
    <div class="test-section">
        <h2>2. Multiple Files Upload (Different Field Names)</h2>
        <form id="form2" enctype="multipart/form-data">
            Avatar: <input type="file" name="avatar"><br>
            Document: <input type="file" name="document"><br>
            Photo: <input type="file" name="photo"><br>
            <button type="submit">Upload Files</button>
        </form>
        <div id="result2" class="result"></div>
    </div>

    <!-- Test 3: Files with parameters -->
    <div class="test-section">
        <h2>3. Files + Text Parameters</h2>
        <form id="form3" enctype="multipart/form-data">
            Username: <input type="text" name="username" value="John"><br>
            Age: <input type="text" name="age" value="25"><br>
            Profile Picture: <input type="file" name="profilePic"><br>
            Resume: <input type="file" name="resume"><br>
            <button type="submit">Upload with Params</button>
        </form>
        <div id="result3" class="result"></div>
    </div>

    <!-- Test 4: JSON response -->
    <div class="test-section">
        <h2>4. Upload with JSON Response</h2>
        <form id="form4" enctype="multipart/form-data">
            Description: <input type="text" name="description" value="My files"><br>
            File 1: <input type="file" name="file1"><br>
            File 2: <input type="file" name="file2"><br>
            <button type="submit">Upload (JSON)</button>
        </form>
        <div id="result4" class="result"></div>
    </div>

    <!-- Test 5: Multiple files with SAME field name -->
    <div class="test-section">
        <h2>5. Multiple Files with Same Field Name</h2>
        <form id="form5" enctype="multipart/form-data">
            <p>Select multiple files for "attachments" field:</p>
            Attachment 1: <input type="file" name="attachments"><br>
            Attachment 2: <input type="file" name="attachments"><br>
            Attachment 3: <input type="file" name="attachments"><br>
            <p>Also upload multiple "photos":</p>
            Photo 1: <input type="file" name="photos"><br>
            Photo 2: <input type="file" name="photos"><br>
            <button type="submit">Upload Multiple</button>
        </form>
        <div id="result5" class="result"></div>
    </div>

    <!-- Test 6: Multiple files with same field name + JSON -->
    <div class="test-section">
        <h2>6. Multiple Files (Same Field) + JSON Response</h2>
        <form id="form6" enctype="multipart/form-data">
            Category: <input type="text" name="category" value="Documents"><br>
            <p>Upload multiple documents:</p>
            Document: <input type="file" name="documents"><br>
            Document: <input type="file" name="documents"><br>
            <p>Upload multiple images:</p>
            Image: <input type="file" name="images"><br>
            Image: <input type="file" name="images"><br>
            <button type="submit">Upload (JSON)</button>
        </form>
        <div id="result6" class="result"></div>
    </div>

    <script>
        // Helper function to handle form submission
        function setupForm(formId, url, resultId, asJson = false) {
            document.getElementById(formId).addEventListener('submit', async (e) => {
                e.preventDefault();
                const form = e.target;
                const formData = new FormData(form);
                const resultDiv = document.getElementById(resultId);
                
                try {
                    resultDiv.textContent = 'Uploading...';
                    const response = await fetch(url, {
                        method: 'POST',
                        body: formData
                    });
                    
                    if (asJson) {
                        const data = await response.json();
                        resultDiv.textContent = JSON.stringify(data, null, 2);
                    } else {
                        const text = await response.text();
                        resultDiv.textContent = text;
                    }
                } catch (error) {
                    resultDiv.textContent = 'Error: ' + error.message;
                }
            });
        }

        // Setup all forms
        setupForm('form1', 'upload/single', 'result1');
        setupForm('form2', 'upload/single', 'result2');
        setupForm('form3', 'upload/with-params', 'result3');
        setupForm('form4', 'upload/json', 'result4', true);
        setupForm('form5', 'upload/multiple', 'result5');
        setupForm('form6', 'upload/multiple-json', 'result6', true);
    </script>
</body>
</html>
