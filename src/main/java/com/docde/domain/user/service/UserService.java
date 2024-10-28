package com.docde.domain.user.service;

import com.docde.common.Apiresponse.ErrorStatus;
import com.docde.common.exceptions.ApiException;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.user.entity.User;
import com.docde.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void deleteUser(Long userId, String password) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorStatus._FORBIDDEN));
        if (!passwordEncoder.matches(password, user.getPassword())) throw new ApiException(ErrorStatus._FORBIDDEN);

        userRepository.delete(user);
    }

    public boolean hasPermissionToDeleteUser(Long userId, AuthUser authUser) {
        return userId.equals(authUser.getId());
    }
}
