package com.test.listener;

import com.test.model.ApiToken;
import com.test.repository.TokenRepository;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class TokenStartupListener implements ServletContextListener {

    private static final int DEFAULT_VALID_DAYS = 30;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            TokenRepository repo = new TokenRepository();
            ApiToken current = repo.findLastValidToken();

            if (current == null) {
                ApiToken created = repo.createAndSaveNewToken(DEFAULT_VALID_DAYS);
                System.out.println("[TokenStartupListener] Generated startup token: " + created.getToken());
            } else {
                System.out.println("[TokenStartupListener] Existing valid token found, expires: " + current.getDateExpiration());
            }
        } catch (Exception e) {
            System.err.println("[TokenStartupListener] Error during token generation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        
    }
}
