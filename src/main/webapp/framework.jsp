<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Framework Java - Page non trouvée</title>
    <style>
        body { font-family: 'Segoe UI', sans-serif; background: #f5f7fa; margin: 0; padding: 40px; }
        .container { max-width: 600px; margin: auto; background: white; padding: 40px; border-radius: 15px;
                     box-shadow: 0 10px 30px rgba(0,0,0,0.1); }
        h1 { border-bottom: 2px solid #333; padding-bottom: 15px; }
        .message { background: #f8f9fa; padding: 20px; border-radius: 10px; margin-top: 20px; }
        ul { padding-left: 20px; }
        li { margin: 5px 0; }
        .path { background: #eee; padding: 8px 12px; border-radius: 6px; display: inline-block; }
    </style>
</head>
<body>
<div class="container">
    <h1>FRAMEWORK JAVA</h1>

    <div class="message">
        <h3>Ressource non trouvée</h3>
        <p>Voici l'URL demandée :</p>
        <div class="path">${requestedPath}</div>
    </div>

    <div class="message">
        <h3>Manage Mappings</h3>
        <c:choose>
            <c:when test="${not empty manageMappings}">
                <ul>
                    <c:forEach var="mapping" items="${manageMappings}">
                        <li>${mapping}</li>
                    </c:forEach>
                </ul>
            </c:when>
            <c:otherwise>
                <p>(no mappings found)</p>
            </c:otherwise>
        </c:choose>
    </div>
</div>
</body>
</html>
