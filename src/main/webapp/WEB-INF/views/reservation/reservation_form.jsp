<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List, com.test.model.Hotel" %>

    <h1>Créer une réservation</h1>

    <%
        List<Hotel> hotels = (List<Hotel>) request.getAttribute("hotels");
    %>

    <form class="bo-form" action="${pageContext.request.contextPath}/reservation/save" method="post">
        <label for="idClient">ID client</label>
        <input type="text" id="idClient" name="idClient" maxlength="4" required />

        <label for="nbPassager">Nombre de passagers</label>
        <input type="number" id="nbPassager" name="nbPassager" min="0" />

        <label for="dateHeureArrivee">Date et heure d'arrivée</label>
        <input type="datetime-local" id="dateHeureArrivee" name="dateHeureArrivee" />

        <label for="idHotel">Hôtel</label>
        <select id="idHotel" name="idHotel">
            <option value="">-- Sélectionnez un hôtel --</option>
            <% if (hotels != null) {
                   for (Hotel h : hotels) { %>
                <option value="<%= h.getId() %>"><%= h.getNom() %></option>
            <%   }
               } %>
        </select>

        <div class="actions">
            <button type="submit">Enregistrer</button>
            <a href="/reservation/list">Retour à la liste</a>
        </div>
    </form>
