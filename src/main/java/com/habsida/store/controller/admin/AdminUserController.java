package com.habsida.store.controller.admin;

import com.habsida.store.dto.PageResponse;
import com.habsida.store.dto.request.UserRequest;
import com.habsida.store.dto.response.UserResponse;
import com.habsida.store.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Users", description = "Admin: user account management")
public class AdminUserController {

    private final UserService userService;

    @Operation(summary = "List all users (filterable)")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping
    public PageResponse<UserResponse> findAll(
            @RequestParam(required = false) Map<String, String> filter,
            Pageable pageable) {
        return userService.findAll(filter, pageable);
    }

    @Operation(summary = "Get a user by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @Operation(summary = "Create a user")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @Operation(summary = "Update a user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Updated"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.updateOrThrow(id, request));
    }

    @Operation(summary = "Delete a user")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deleted"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteOrThrow(id);
        return ResponseEntity.noContent().build();
    }
}