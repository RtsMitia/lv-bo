<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div class="assignation-date-form">
    <h2>Sélectionner une date</h2>
    
    <% 
        String error = (String) request.getAttribute("error");
        if (error != null) {
    %>
        <div style="padding: 10px; background-color: #ffcccc; color: #cc0000; border: 1px solid #cc0000; border-radius: 5px; margin-bottom: 15px;">
            <%= error %>
        </div>
    <%
        }
    %>
    
    <form action="${pageContext.request.contextPath}/assignation/list" method="get" style="max-width: 400px;">
        <div style="margin-bottom: 20px;">
            <label for="date" style="display: block; margin-bottom: 5px; font-weight: bold;">
                Choisir la date des trajets :
            </label>
            <input 
                type="date" 
                id="date" 
                name="date" 
                required 
                style="width: 100%; padding: 10px; font-size: 16px; border: 1px solid #ccc; border-radius: 5px;"
            />
        </div>
        
        <div style="margin-top: 20px;">
            <button 
                type="submit" 
                style="padding: 12px 30px; font-size: 16px; background-color: #007bff; color: white; border: none; border-radius: 5px; cursor: pointer;">
                Voir les trajets
            </button>
        </div>
    </form>
</div>

<style>
    .assignation-date-form {
        padding: 20px;
    }
    
    .assignation-date-form h2 {
        margin-bottom: 20px;
        color: #333;
    }
    
    button:hover {
        background-color: #0056b3 !important;
    }
</style>
