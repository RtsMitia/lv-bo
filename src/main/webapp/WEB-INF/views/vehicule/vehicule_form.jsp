<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.test.model.Vehicule" %>

    <h1>Vehicule Form</h1>
    <%
        Vehicule vehicule = (Vehicule) request.getAttribute("vehicule");
        if (vehicule == null) {
            vehicule = new Vehicule();
        }
        String idValue = (vehicule.getId() == null) ? "" : String.valueOf(vehicule.getId());
    %>

    <form class="bo-form" action="<%= request.getContextPath() + "/vehicule/save" %>" method="post">
        <input type="hidden" name="id" value="<%= idValue %>" />
        <div>
            <label>Reference:</label>
            <input type="text" name="reference" value="<%= vehicule.getReference() == null ? "" : vehicule.getReference() %>" />
        </div>
        <div>
            <label>Place:</label>
            <input type="number" name="place" value="<%= vehicule.getPlace() == null ? "" : vehicule.getPlace() %>" />
        </div>
        <div>
            <label>Type carburant:</label>
            <input type="text" name="typeCarburant" value="<%= vehicule.getTypeCarburant() == null ? "" : vehicule.getTypeCarburant() %>" />
        </div>
        <div>
            <button type="submit">Save</button>
            <a href="<%= request.getContextPath() + "/vehicule/list" %>">Cancel</a>
        </div>
    </form>
