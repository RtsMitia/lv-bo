<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.test.dto.AssignationWithDetails" %>
<%@ page import="com.test.model.Reservation" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.math.BigDecimal" %>

<%
    String dateFormatted = (String) request.getAttribute("dateFormatted");
    List<AssignationWithDetails> assignations = (List<AssignationWithDetails>) request.getAttribute("assignations");
    List<Reservation> unassignedReservations = (List<Reservation>) request.getAttribute("unassignedReservations");
    Map<Integer, String> hotelMap = (Map<Integer, String>) request.getAttribute("hotelMap");
    Integer assignedCount = (Integer) request.getAttribute("assignedCount");

    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    java.time.LocalDate dateObj = (java.time.LocalDate) request.getAttribute("date");
    String dateParam = null;
    if (dateObj != null) {
        dateParam = dateObj.format(java.time.format.DateTimeFormatter.ISO_DATE);
    }
%>

<div class="assignation-list">
    <div style="margin-bottom: 20px;">
        <h2>Trajets du <%= dateFormatted %></h2>
        <h1>3363, 3331, 3335</h1>
        <a href="${pageContext.request.contextPath}/assignation/date" 
           style="padding: 8px 16px; background-color: #6c757d; color: white; text-decoration: none; border-radius: 5px; display: inline-block;">
            ← Retour
        </a>

        <% if (assignedCount != null && assignedCount > 0) { %>
            <div style="margin-top: 15px; padding: 10px; background-color: #d4edda; color: #155724; border-radius: 5px;">
                ✓ <%= assignedCount %> réservation(s) assignée(s) et enregistrées avec succès.
            </div>
        <% } else { %>
            <div style="margin-top: 15px; padding: 10px; background-color: #d4edda; color: #155724; border-radius: 5px;">
                ✓ Assignations enregistrées.
            </div>
        <% } %>
    </div>
    
    <div class="tables-container">
        <div class="table-section">
            <h3>Trajets assignés (avec véhicule)</h3>
            <% if (assignations == null || assignations.isEmpty()) { %>
                <p style="color: #888;">Aucun trajet assigné pour cette date.</p>
            <% } else { %>
                <table class="assignation-table">
                    <thead>
                        <tr>
                            <th>Véhicule</th>
                            <th>Réservations</th>
                            <th>Hôtels</th>
                            <th>Départ Aéroport</th>
                            <th>Retour Aéroport</th>
                            <th>Passagers</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% int rowIdx = 0; for (AssignationWithDetails assignation : assignations) { %>
                            <tr style="cursor:pointer;" onclick="showTrajetPopup(<%= rowIdx %>)">
                                <td>
                                    <strong><%= assignation.getVehiculeReference() %></strong>
                                    <br/>
                                    <small>(Capacité: <%= assignation.getVehiculePlace() %> places)</small>
                                    <%-- <% if (!isSimulation && assignation.getAssignationId() != null) { %>
                                        <br/>
                                        <a href="${pageContext.request.contextPath}/assignation/detail/<%= assignation.getAssignationId() %><%= dateParam != null ? ("?date=" + dateParam) : "" %>" style="margin-top:6px; display:inline-block; padding:6px 10px; background:#007bff; color:#fff; text-decoration:none; border-radius:4px; font-size:12px;" onclick="event.stopPropagation()">Détails</a>
                                    <% } %> --%>
                                </td>
                                <td>
                                    <% 
                                        if (assignation.getReservations().isEmpty()) {
                                            out.print("<em>Aucune réservation</em>");
                                        } else {
                                            for (AssignationWithDetails.ReservationWithHotel res : assignation.getReservations()) {
                                    %>
                                        • Rés. #<%= res.getReservationId() %> 
                                        (Client: <%= res.getIdClient() %>, 
                                        <%= res.getNbPersPrises() %> pers.)<br/>
                                    <%
                                            }
                                        }
                                    %>
                                </td>
                                <td>
                                    <% 
                                        if (assignation.getReservations().isEmpty()) {
                                            out.print("-");
                                        } else {
                                            for (AssignationWithDetails.ReservationWithHotel res : assignation.getReservations()) {
                                    %>
                                        • <%= res.getHotelNom() != null ? res.getHotelNom() : "N/A" %><br/>
                                    <%
                                            }
                                        }
                                    %>
                                </td>
                                <td>
                                    <% if (assignation.getDepartAeroport() != null) { %>
                                        <%= assignation.getDepartAeroport().format(timeFormatter) %>
                                    <% } else { %>
                                        N/A
                                    <% } %>
                                </td>
                                <td>
                                    <% if (assignation.getRetourAeroport() != null) { %>
                                        <%= assignation.getRetourAeroport().format(timeFormatter) %>
                                    <% } else { %>
                                        N/A
                                    <% } %>
                                </td>
                                <td>
                                    <%= assignation.getTotalPassagers() %> / <%= assignation.getVehiculePlace() %>
                                    <br/>
                                    <small style="color: #28a745;">(Reste: <%= assignation.getRestePlace() %>)</small>
                                </td>
                            </tr>
                        <% rowIdx++; } %>
                    </tbody>
                </table>
            <% } %>
        </div>
        
        <div class="table-section">
            <h3>Réservations non assignées</h3>
            <% if (unassignedReservations == null || unassignedReservations.isEmpty()) { %>
                <p style="color: #28a745;"> Aucune reservation</p>
            <% } else { %>
                <table class="assignation-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Client</th>
                            <th>Passagers</th>
                            <th>Date/Heure Arrivée</th>
                            <th>Hôtel</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Reservation res : unassignedReservations) { %>
                            <tr>
                                <td><strong>#<%= res.getId() %></strong></td>
                                <td><%= res.getIdClient() %></td>
                                <td><%= res.getNbPassager() %></td>
                                <td>
                                    <% if (res.getDateHeureArrivee() != null) { %>
                                        <%= res.getDateHeureArrivee().format(dateTimeFormatter) %>
                                    <% } else { %>
                                        N/A
                                    <% } %>
                                </td>
                                <td>
                                    <% 
                                        if (res.getIdHotel() != null && hotelMap.containsKey(res.getIdHotel())) {
                                            out.print(hotelMap.get(res.getIdHotel()));
                                        } else {
                                            out.print("N/A");
                                        }
                                    %>
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            <% } %>
        </div>
    </div>
</div>

<!-- Popup modal pour détail du trajet -->
<div id="trajetModal" style="display:none; position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.5); z-index:1000; justify-content:center; align-items:center;">
    <div style="background:#fff; border-radius:8px; padding:24px; max-width:500px; width:90%; max-height:80vh; overflow-y:auto; box-shadow:0 4px 20px rgba(0,0,0,0.3); position:relative;">
        <button onclick="closeTrajetPopup()" style="position:absolute; top:10px; right:14px; background:none; border:none; font-size:22px; cursor:pointer; color:#888;">&times;</button>
        <h3 style="margin-top:0; margin-bottom:16px; color:#333;">Détail du trajet</h3>
        <div id="trajetModalContent"></div>
    </div>
</div>

<script>
var trajetsData = [
<% if (assignations != null) {
    for (int ti = 0; ti < assignations.size(); ti++) {
        AssignationWithDetails ta = assignations.get(ti);
        java.util.List<String> noms = ta.getTrajetLieuxNoms();
        java.util.List<BigDecimal> segs = ta.getTrajetSegmentDistances();
        BigDecimal total = ta.getTrajetTotalDistance();
%>
  { vehicule: "<%= ta.getVehiculeReference() != null ? ta.getVehiculeReference().replace("\"", "\\\"") : "" %>",
    lieux: [<% if (noms != null) { for (int ni = 0; ni < noms.size(); ni++) { %>"<%= noms.get(ni).replace("\"", "\\\"") %>"<%= ni < noms.size()-1 ? "," : "" %><% } } %>],
    segments: [<% if (segs != null) { for (int si = 0; si < segs.size(); si++) { %><%= segs.get(si) %><%= si < segs.size()-1 ? "," : "" %><% } } %>],
    total: <%= total != null ? total : "0" %>
  }<%= ti < assignations.size()-1 ? "," : "" %>
<% } } %>
];

function showTrajetPopup(idx) {
    var d = trajetsData[idx];
    if (!d) {
        return;
    }
    if (!d.lieux || d.lieux.length === 0) {
        document.getElementById('trajetModalContent').innerHTML = '<p><strong>Véhicule :</strong> ' + d.vehicule + '</p><p>Aucun détail de trajet disponible.</p>';
        document.getElementById('trajetModal').style.display = 'flex';
        return;
    }
    var html = '<p><strong>Véhicule :</strong> ' + d.vehicule + '</p>';
    html += '<p><strong>Distance totale :</strong> ' + d.total + ' km</p>';
    html += '<table style="width:100%; border-collapse:collapse; margin-top:10px;">';
    html += '<thead><tr style="background:#6c757d; color:#fff;"><th style="padding:8px; border:1px solid #ddd; text-align:left;">De</th><th style="padding:8px; border:1px solid #ddd; text-align:left;">Vers</th><th style="padding:8px; border:1px solid #ddd; text-align:right;">Distance</th></tr></thead><tbody>';
    for (var i = 0; i < d.lieux.length; i++) {
        var from = (i === 0) ? 'Aéroport' : d.lieux[i-1];
        var to = d.lieux[i];
        var dist = (i < d.segments.length) ? d.segments[i] : '-';
        html += '<tr><td style="padding:8px; border:1px solid #ddd;">' + from + '</td><td style="padding:8px; border:1px solid #ddd;">' + to + '</td><td style="padding:8px; border:1px solid #ddd; text-align:right;">' + dist + ' km</td></tr>';
    }
    // Retour aéroport
    if (d.lieux.length > 0 && d.segments.length > d.lieux.length) {
        var lastLieu = d.lieux[d.lieux.length - 1];
        var retourDist = d.segments[d.segments.length - 1];
        html += '<tr><td style="padding:8px; border:1px solid #ddd;">' + lastLieu + '</td><td style="padding:8px; border:1px solid #ddd;">Aéroport</td><td style="padding:8px; border:1px solid #ddd; text-align:right;">' + retourDist + ' km</td></tr>';
    }
    html += '</tbody></table>';
    document.getElementById('trajetModalContent').innerHTML = html;
    document.getElementById('trajetModal').style.display = 'flex';
}

function closeTrajetPopup() {
    document.getElementById('trajetModal').style.display = 'none';
}

document.getElementById('trajetModal').addEventListener('click', function(e) {
    if (e.target === this) closeTrajetPopup();
});
</script>

<style>
    .assignation-list {
        padding: 20px;
    }
    
    .assignation-list h2 {
        color: #333;
        margin-bottom: 10px;
    }
    
    .tables-container {
        display: flex;
        gap: 20px;
        margin-top: 20px;
    }
    
    .table-section {
        flex: 1;
        min-width: 0;
    }
    
    .table-section h3 {
        color: #555;
        margin-bottom: 15px;
        padding-bottom: 10px;
        border-bottom: 2px solid #6c757d;
    }
    
    .assignation-table {
        width: 100%;
        border-collapse: collapse;
        background-color: white;
        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
    
    .assignation-table thead {
        background-color: #6c757d;
        color: white;
    }
    
    .assignation-table th,
    .assignation-table td {
        padding: 12px;
        text-align: left;
        border: 1px solid #ddd;
    }
    
    .assignation-table th {
        font-weight: 600;
        font-size: 14px;
    }
    
    .assignation-table tbody tr:nth-child(even) {
        background-color: #f8f9fa;
    }
    
    .assignation-table tbody tr:hover {
        background-color: #e9ecef;
    }
    
    .assignation-table td {
        font-size: 14px;
        line-height: 1.6;
    }
    
    @media (max-width: 1200px) {
        .tables-container {
            flex-direction: column;
        }
    }
</style>
