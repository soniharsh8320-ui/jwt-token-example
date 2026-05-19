package spring.security.jwt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import spring.security.jwt.dto.AuthResponse;
import spring.security.jwt.dto.RefreshTokenRequest;
import spring.security.jwt.entity.User;
import spring.security.jwt.repository.UserRepository;
import spring.security.jwt.security.JwtUtils;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication APIs", description = "Endpoints for signup, signin, and token refresh.")
public class AuthenticationController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/signin")
    @Operation(summary = "Sign in user", description = "Authenticates user credentials and returns access and refresh JWT tokens.")
    public AuthResponse authenticateUser(@RequestBody User user){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        user.getPassword())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return issueTokens(userDetails.getUsername());
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT tokens", description = "Validates refresh token and issues a new access and refresh token pair.")
    public AuthResponse refreshToken(@RequestBody RefreshTokenRequest request) {
        if (request == null || request.refreshToken() == null || request.refreshToken().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Refresh token is required");
        }
        if (!jwtUtils.validateRefreshToken(request.refreshToken())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid refresh token");
        }
        String username = jwtUtils.getUserFromToken(request.refreshToken());
        return issueTokens(username);
    }

    @PostMapping("/signup")
    @Operation(summary = "Register user", description = "Registers a new user with encoded password if username is not already present.")
    public String registerUser(@RequestBody User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ResponseStatusException(CONFLICT, "User already exists");
        }

        final User newUser = new User(
                null,
                user.getUsername(),
                passwordEncoder.encode(user.getPassword())
        );
        userRepository.save(newUser);
        return "User registered successfully!";
    }

    private AuthResponse issueTokens(String username) {
        String accessToken = jwtUtils.generateAccessToken(username);
        String refreshToken = jwtUtils.generateRefreshToken(username);
        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtUtils.getAccessExpirationSeconds(),
                jwtUtils.getRefreshExpirationSeconds()
        );
    }
}
