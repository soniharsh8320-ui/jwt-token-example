package spring.security.jwt.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;
import spring.security.jwt.dto.AuthResponse;
import spring.security.jwt.dto.RefreshTokenRequest;
import spring.security.jwt.entity.User;
import spring.security.jwt.security.JwtUtils;
import spring.security.jwt.services.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AuthenticationController authenticationController;

    @Test
    void authenticateUserShouldReturnTokensWhenCredentialsAreValid() {
        User request = new User(null, "john", "john123");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("john");
        mockTokenIssuance("access-1", "refresh-1", 900L, 604800L);

        AuthResponse response = authenticationController.authenticateUser(request);

        assertNotNull(response);
        assertEquals("access-1", response.accessToken());
        assertEquals("refresh-1", response.refreshToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(900L, response.accessExpiresInSeconds());
        assertEquals(604800L, response.refreshExpiresInSeconds());

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertEquals("john", captor.getValue().getPrincipal());
        assertEquals("john123", captor.getValue().getCredentials());
    }

    @Test
    void refreshTokenShouldThrowBadRequestWhenRequestIsNull() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authenticationController.refreshToken(null)
        );

        assertEquals(BAD_REQUEST.value(), exception.getStatusCode().value());
        assertEquals("Refresh token is required", exception.getReason());
    }

    @Test
    void refreshTokenShouldThrowBadRequestWhenTokenIsBlank() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authenticationController.refreshToken(new RefreshTokenRequest("   "))
        );

        assertEquals(BAD_REQUEST.value(), exception.getStatusCode().value());
        assertEquals("Refresh token is required", exception.getReason());
    }

    @Test
    void refreshTokenShouldThrowUnauthorizedWhenTokenIsInvalid() {
        String refreshToken = "invalid-refresh-token";
        when(jwtUtils.validateRefreshToken(refreshToken)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authenticationController.refreshToken(new RefreshTokenRequest(refreshToken))
        );

        assertEquals(UNAUTHORIZED.value(), exception.getStatusCode().value());
        assertEquals("Invalid refresh token", exception.getReason());
        verify(jwtUtils).validateRefreshToken(refreshToken);
    }

    @Test
    void refreshTokenShouldReturnNewTokensWhenTokenIsValid() {
        String refreshToken = "valid-refresh-token";
        when(jwtUtils.validateRefreshToken(refreshToken)).thenReturn(true);
        when(jwtUtils.getUserFromToken(refreshToken)).thenReturn("john");
        mockTokenIssuance("access-2", "refresh-2", 900L, 604800L);

        AuthResponse response = authenticationController.refreshToken(new RefreshTokenRequest(refreshToken));

        assertEquals("access-2", response.accessToken());
        assertEquals("refresh-2", response.refreshToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(900L, response.accessExpiresInSeconds());
        assertEquals(604800L, response.refreshExpiresInSeconds());
        verify(jwtUtils).validateRefreshToken(refreshToken);
        verify(jwtUtils).getUserFromToken(refreshToken);
    }

    @Test
    void registerUserShouldThrowConflictWhenUserAlreadyExists() {
        User request = new User(null, "john", "john123");
        doThrow(new ResponseStatusException(CONFLICT, "User already exists"))
                .when(userService).registerUser(request);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authenticationController.registerUser(request)
        );

        assertEquals(CONFLICT.value(), exception.getStatusCode().value());
        assertEquals("User already exists", exception.getReason());
        verify(userService).registerUser(request);
    }

    @Test
    void registerUserShouldSaveEncodedPasswordWhenUserDoesNotExist() {
        User request = new User(null, "john", "john123");
        doNothing().when(userService).registerUser(request);

        String response = authenticationController.registerUser(request);

        assertEquals("User registered successfully!", response);
        verify(userService).registerUser(request);
    }

    private void mockTokenIssuance(
            String accessToken,
            String refreshToken,
            long accessExpirationSeconds,
            long refreshExpirationSeconds
    ) {
        when(jwtUtils.generateAccessToken("john")).thenReturn(accessToken);
        when(jwtUtils.generateRefreshToken("john")).thenReturn(refreshToken);
        when(jwtUtils.getAccessExpirationSeconds()).thenReturn(accessExpirationSeconds);
        when(jwtUtils.getRefreshExpirationSeconds()).thenReturn(refreshExpirationSeconds);
    }
}
