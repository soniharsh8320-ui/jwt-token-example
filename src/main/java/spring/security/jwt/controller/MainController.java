package spring.security.jwt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Main APIs", description = "Public and JWT-protected sample endpoints.")
public class MainController {
    @GetMapping("/welcome")
    @Operation(summary = "Public welcome endpoint", description = "Returns a public response without authentication.")
    public String allAccess() {
        return "Everyone access";
    }

    @GetMapping("/user")
    @Operation(
            summary = "User protected endpoint",
            description = "Returns user content for requests with a valid JWT access token.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public String userAccess() {
        return "User Content with JWT";
    }

    @GetMapping("/special")
    @Operation(
            summary = "Special protected endpoint",
            description = "Returns special content for requests with a valid JWT access token.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public String specialAccess() {
        return "Special access with JWT";
    }
}
