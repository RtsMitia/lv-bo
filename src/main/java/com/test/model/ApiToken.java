package com.test.model;

import java.time.LocalDateTime;

public class ApiToken {
    private Integer id;
    private String token;
    private LocalDateTime dateExpiration;
    
    public ApiToken(Integer id, String token, LocalDateTime dateExpiration) {
        this.id = id;
        this.token = token;
        this.dateExpiration = dateExpiration;
    }
    
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public LocalDateTime getDateExpiration() {
        return dateExpiration;
    }
    public void setDateExpiration(LocalDateTime dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

}
