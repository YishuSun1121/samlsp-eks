package com.nextjump.saml2.controllers;

import com.nextjump.saml2.service.UserService;
import com.nextjump.saml2.service.request.DefaultUserCreateRequest;
import com.nextjump.saml2.service.request.DefaultUserQueryPageRequest;
import com.nextjump.saml2.view.UserView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(name = "UserManagement")
@RestController
public class UserMgrRestController {
    @Autowired
    private UserService userService;

    @Operation(summary = "Query all users")
    @GetMapping("/mgr/users")
    public Page<UserView> queryPage(DefaultUserQueryPageRequest param) {
        return userService.queryPage(param);
    }

    @Operation(summary = "Create new user")
    @PostMapping("/mgr/users")
    public UserView create(@RequestBody DefaultUserCreateRequest param) {
        return userService.create(param);
    }

    @Operation(summary = "Delete user by id")
    @DeleteMapping("/mgr/users/{id}")
    public void deleteById(@PathVariable String id) {
        userService.deleteById(id);
    }

    @Operation(summary = "Update user by id")
    @PostMapping("/mgr/users/{id}")
    public void update(@PathVariable String id, @RequestBody DefaultUserCreateRequest param) {
        userService.update(id, param);
    }
}
