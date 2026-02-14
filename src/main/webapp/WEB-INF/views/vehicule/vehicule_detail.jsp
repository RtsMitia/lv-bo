<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.test.model.Vehicule" %>

    <h1>Vehicule Detail</h1>
    <%
        Vehicule vehicule = (Vehicule) request.getAttribute("vehicule");
        if (vehicule == null) {
    %>
    <p>Vehicule not found.</p>
    <p><a href="<%= request.getContextPath() + "/vehicule/list" %>">Back to list</a></p>
    <%
        } else {
    %>
    <table class="bo-table" cellpadding="6">
        <tr><td><strong>ID</strong></td><td><%= vehicule.getId() %></td></tr>
        <tr><td><strong>Reference</strong></td><td><%= vehicule.getReference() %></td></tr>
        <tr><td><strong>Place</strong></td><td><%= vehicule.getPlace() %></td></tr>
        <tr><td><strong>Type carburant</strong></td><td><%= vehicule.getTypeCarburant() %></td></tr>
    </table>
    <p>
        <a href="<%= request.getContextPath() + "/vehicule/form/" + vehicule.getId() %>">Edit</a>
        &nbsp;|&nbsp;
        <a href="<%= request.getContextPath() + "/vehicule/delete/" + vehicule.getId() %>" onclick="return confirm('Delete vehicule #<%= vehicule.getId() %>?');">Delete</a>
        &nbsp;|&nbsp;
        <a href="<%= request.getContextPath() + "/vehicule/list" %>">Back to list</a>
    </p>
    <%
        }
    %>