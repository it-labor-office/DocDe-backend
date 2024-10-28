package com.docde.domain.user.controller;

import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("@userService.hasPermissionToDeleteUser(#userId, #authUser)")
    ResponseEntity deleteUser(@PathVariable Long userId, @RequestHeader("X-User-Password") String password, @AuthenticationPrincipal AuthUser authUser) {
        userService.deleteUser(userId, password);
        return ResponseEntity.noContent().build();
    }
}
