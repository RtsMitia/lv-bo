<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<div>
    <h2>Welcome to the Backoffice</h2>
    <p>Use the sidebar to manage reservations and vehicules.</p>
    <ul>
        <li><a class="bo-link" href="${pageContext.request.contextPath}/reservation/list">Reservations</a></li>
        <li><a class="bo-link" href="${pageContext.request.contextPath}/vehicule/list">Vehicules</a></li>
    </ul>
</div>
