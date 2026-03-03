<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <title>Backoffice</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bo-layout.css" />
    <script>
        function toggle(id) {
            var el = document.getElementById(id);
            if (!el) return;
            el.style.display = (el.style.display === 'block') ? 'none' : 'block';
        }
    </script>
</head>
<body>
    <div class="bo-app">
        <div class="bo-app__wrapper">
        <aside class="bo-sidebar">
            <h2>Admin</h2>
            <div class="dropdown" onclick="toggle('menu-reservation')">Reservation ▾</div>
            <div id="menu-reservation" class="dropdown-items">
                <ul>
                    <li><a href="${pageContext.request.contextPath}/reservation/list">List reservations</a></li>
                    <li><a href="${pageContext.request.contextPath}/reservation/new">New reservation</a></li>
                </ul>
            </div>

            <div class="dropdown" onclick="toggle('menu-vehicule')">Vehicule ▾</div>
            <div id="menu-vehicule" class="dropdown-items">
                <ul>
                    <li><a href="${pageContext.request.contextPath}/vehicule/list">List vehicules</a></li>
                    <li><a href="${pageContext.request.contextPath}/vehicule/form">New vehicule</a></li>
                </ul>
            </div>
        </aside>

        <main class="bo-content">
            <jsp:directive.include file="/WEB-INF/views/include/header.jsp"/>

            <!-- dynamic content -->
            <div class="bo-section">
            <%
                Object content = request.getAttribute("content");
                if (content != null && content instanceof String) {
                    String contentPage = (String) content;
            %>
                <jsp:include page="<%= contentPage %>" />
            <%
                } else {
            %>
                <p>No content specified.</p>
            <%
                }
            %>
            </div>

            <jsp:directive.include file="/WEB-INF/views/include/footer.jsp"/>
        </main>
        </div>
    </div>
</body>
</html>
