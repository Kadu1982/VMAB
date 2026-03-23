package com.seguranca.plataforma.auth;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserManagementController {
    // Disponibiliza a gestao de usuarios e perfis para o painel administrativo.

    private final UserManagementService userManagementService;

    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    public List<AppUserResponse> listUsers() {
        return userManagementService.listUsers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppUserResponse createUser(@Valid @RequestBody CreateAppUserRequest request) {
        return userManagementService.createUser(request);
    }

    @PutMapping("/{id}")
    public AppUserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UpdateAppUserRequest request) {
        return userManagementService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userManagementService.deleteUser(id);
    }
}
