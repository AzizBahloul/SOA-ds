package net.javaguides.springboot.soa.controller;

import net.javaguides.springboot.soa.entity.User;
import net.javaguides.springboot.soa.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public UserController(UserService userService, UserDetailsService userDetailsService, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> signup(@RequestBody User user) {
        if (!user.getRole().startsWith("ROLE_")) {
            user.setRole("ROLE_" + user.getRole());
        }
        return ResponseEntity.ok(userService.save(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );
            
            if (authentication.isAuthenticated()) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                User foundUser = userService.findByUsername(user.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                
                return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + createBasicAuthToken(user.getUsername(), foundUser.getPassword()))
                    .body(userDetails.getAuthorities());
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // New Logout Endpoint
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Since we're using stateless sessions, there's nothing to invalidate on the server.
        // This endpoint can be used for logging purposes or future enhancements.
        return ResponseEntity.ok("Logged out successfully.");
    }

    private String createBasicAuthToken(String username, String encodedPassword) {
        String auth = username + ":" + encodedPassword;
        return Base64.getEncoder().encodeToString(auth.getBytes());
    }
}