package net.javaguides.springboot.soa.service;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import java.util.Base64;

@Service
public class AuthService {
    private String currentUsername;
    private String currentPassword;

    public void setCredentials(String username, String password) {
        this.currentUsername = username;
        this.currentPassword = password;
    }

    public void clearCredentials() {
        this.currentUsername = null;
        this.currentPassword = null;
    }

    public HttpHeaders createHeaders() {
        if (currentUsername == null || currentPassword == null) {
            return new HttpHeaders();
        }
        String auth = currentUsername + ":" + currentPassword;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + encodedAuth);
        return headers;
    }
}