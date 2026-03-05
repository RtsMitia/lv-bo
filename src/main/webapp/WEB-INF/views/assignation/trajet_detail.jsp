<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.test.model.Trajet" %>
<%@ page import="com.test.dto.AssignationWithDetails" %>
<%@ page import="com.test.dto.AssignationWithDetails.ReservationWithHotel" %>
<%@ page import="java.util.List" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.time.LocalDate" %>

<%
    Trajet trajet = (Trajet) request.getAttribute("trajet");
    BigDecimal totalDistance = (BigDecimal) request.getAttribute("totalDistance");
    List<String> lieux = (List<String>) request.getAttribute("lieux");
    List<BigDecimal> segmentDistances = trajet != null ? trajet.getSegmentDistances() : null;
    AssignationWithDetails assignationDetails = (AssignationWithDetails) request.getAttribute("assignationDetails");
    List<ReservationWithHotel> reservations = assignationDetails != null ? assignationDetails.getReservations() : null;

    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    String dateParam = (String) request.getAttribute("dateParam");
    String dateFormattedView = (String) request.getAttribute("dateFormatted");
    if (dateParam == null) {
        java.time.LocalDate dateObj = (java.time.LocalDate) request.getAttribute("date");
        if (dateObj != null) {
            dateParam = dateObj.format(java.time.format.DateTimeFormatter.ISO_DATE);
            if (dateFormattedView == null) {
                dateFormattedView = dateObj.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
        }
    }
%>

<div class="trajet-detail" style="padding:20px;">
    <div style="margin-bottom:20px; display:flex; align-items:center; gap:12px;">
        <% if (dateParam != null) { %>
            <a href="${pageContext.request.contextPath}/assignation/list?date=<%= dateParam %>" style="padding:8px 14px; background:#007bff; color:#fff; text-decoration:none; border-radius:5px;">← Retour à la liste (<%= dateFormattedView != null ? dateFormattedView : dateParam %>)</a>
        <% } else { %>
            <a href="javascript:history.back()" style="padding:8px 14px; background:#6c757d; color:#fff; text-decoration:none; border-radius:5px;">← Retour</a>
        <% } %>
        <h2 style="margin:0;">Détails du trajet</h2>
    </div>

    <div style="display:flex; gap:20px; flex-wrap:wrap; margin-bottom:20px;">
        <div style="flex:1; min-width:260px; background:#fff; padding:16px; border-radius:6px; box-shadow:0 1px 3px rgba(0,0,0,0.05);">
            <h3>Véhicule</h3>
            <p><strong>Référence :</strong> <%= assignationDetails != null && assignationDetails.getVehiculeReference() != null ? assignationDetails.getVehiculeReference() : "N/A" %></p>
            <p><strong>Capacité :</strong> <%= assignationDetails != null && assignationDetails.getVehiculePlace() != null ? assignationDetails.getVehiculePlace() : "N/A" %> places</p>
            <p><strong>Départ aéroport :</strong> <% if (assignationDetails != null && assignationDetails.getDepartAeroport() != null) { out.print(assignationDetails.getDepartAeroport().format(timeFormatter)); } else { out.print("N/A"); } %></p>
            <p><strong>Retour aéroport :</strong> <% if (assignationDetails != null && assignationDetails.getRetourAeroport() != null) { out.print(assignationDetails.getRetourAeroport().format(timeFormatter)); } else { out.print("N/A"); } %></p>
            <p><strong>Passagers :</strong> <%= assignationDetails != null && assignationDetails.getTotalPassagers() != null ? assignationDetails.getTotalPassagers() : 0 %> / <%= assignationDetails != null && assignationDetails.getVehiculePlace() != null ? assignationDetails.getVehiculePlace() : 0 %>
                <br/><small style="color:#28a745;">(Reste: <%= assignationDetails != null && assignationDetails.getRestePlace() != null ? assignationDetails.getRestePlace() : 0 %>)</small></p>
        </div>

        <div style="flex:1; min-width:300px; background:#fff; padding:16px; border-radius:6px; box-shadow:0 1px 3px rgba(0,0,0,0.05);">
            <h3>Trajet</h3>
            <p><strong>Distance totale :</strong> <%= totalDistance != null ? totalDistance : "N/A" %> km</p>
            <p><strong>Itinéraire :</strong></p>
            <ul style="list-style:none; padding-left:0;">
                <% if (lieux != null && !lieux.isEmpty()) {
                       for (int i = 0; i < lieux.size(); i++) {
                           BigDecimal segDist = (segmentDistances != null && i < segmentDistances.size()) ? segmentDistances.get(i) : null;
                           String prevPoint = (i == 0) ? "Aéroport" : lieux.get(i - 1);
                %>
                           <li style="margin-bottom:6px;">
                               <% if (segDist != null) { %>
                                   <span style="display:inline-block; background:#e9ecef; border-radius:4px; padding:2px 8px; font-size:0.85em; color:#495057; margin-right:6px;">← <%= segDist %> km</span>
                               <% } %>
                               <strong><%= (i + 1) + ". " + lieux.get(i) %></strong>
                           </li>
                <%     }
                   } else { %>
                       <li>N/A</li>
                <% } %>
            </ul>
        </div>
    </div>

    <div style="background:#fff; padding:16px; border-radius:6px; box-shadow:0 1px 3px rgba(0,0,0,0.05);">
        <h3>Réservations liées à cette assignation</h3>
        <% if (reservations == null || reservations.isEmpty()) { %>
            <p>Aucune réservation pour cette assignation.</p>
        <% } else { %>
            <table class="assignation-table" style="width:100%; border-collapse:collapse;">
                <thead style="background:#6c757d; color:#fff;">
                    <tr>
                        <th style="padding:10px; border:1px solid #ddd; text-align:left;">ID</th>
                        <th style="padding:10px; border:1px solid #ddd; text-align:left;">Client</th>
                        <th style="padding:10px; border:1px solid #ddd; text-align:left;">Passagers</th>
                        <th style="padding:10px; border:1px solid #ddd; text-align:left;">Date/Heure Arrivée</th>
                        <th style="padding:10px; border:1px solid #ddd; text-align:left;">Hôtel</th>
                        <th style="padding:10px; border:1px solid #ddd; text-align:left;">Passagers pris</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (ReservationWithHotel res : reservations) { %>
                        <tr>
                            <td style="padding:10px; border:1px solid #ddd;"><strong>#<%= res.getReservationId() %></strong></td>
                            <td style="padding:10px; border:1px solid #ddd;"><%= res.getIdClient() %></td>
                            <td style="padding:10px; border:1px solid #ddd;"><%= res.getNbPassager() != null ? res.getNbPassager() : 0 %></td>
                            <td style="padding:10px; border:1px solid #ddd;">
                                <% if (res.getDateHeureArrivee() != null) { out.print(res.getDateHeureArrivee().format(dateTimeFormatter)); } else { out.print("N/A"); } %>
                            </td>
                            <td style="padding:10px; border:1px solid #ddd;"><%= res.getHotelNom() != null ? res.getHotelNom() : "N/A" %></td>
                            <td style="padding:10px; border:1px solid #ddd;"><%= res.getNbPersPrises() != null ? res.getNbPersPrises() : 0 %></td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        <% } %>
    </div>

</div>

<style>
    .assignation-table thead { background:#6c757d; color:#fff; }
    .assignation-table th, .assignation-table td { padding:10px; border:1px solid #ddd; }
</style>
