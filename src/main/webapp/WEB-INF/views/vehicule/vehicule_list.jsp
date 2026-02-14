<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, com.test.model.Vehicule" %>

    <h1>Vehicules</h1>
    <p><a href="${pageContext.request.contextPath}/vehicule/form">New Vehicule</a></p>

    <table class="bo-table" cellpadding="6" cellspacing="0">
        <tr>
            <th>ID</th>
            <th>Reference</th>
            <th>Place</th>
            <th>Type carburant</th>
            <th>Actions</th>
        </tr>
        <%
            List<Vehicule> vehicules = (List<Vehicule>) request.getAttribute("vehicules");
            if (vehicules != null && !vehicules.isEmpty()) {
                for (Vehicule v : vehicules) {
        %>
        <tr>
            <td><%= v.getId() %></td>
            <td><%= v.getReference() %></td>
            <td><%= v.getPlace() %></td>
            <td><%= v.getTypeCarburant() %></td>
            <td class="bo-actions">
                <a class="bo-link" href="<%= request.getContextPath() + "/vehicule/" + v.getId() %>">View</a>
                <a class="bo-link" href="<%= request.getContextPath() + "/vehicule/form/" + v.getId() %>">Edit</a>
                <a class="bo-link" href="<%= request.getContextPath() + "/vehicule/delete/" + v.getId() %>" onclick="return confirm('Delete vehicule #<%= v.getId() %>?');">Delete</a>
            </td>
        </tr>
        <%
                }
            } else {
        %>
        <tr>
            <td colspan="5">No vehicules found.</td>
        </tr>
        <%
            }
        %>
    </table>