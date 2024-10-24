package com.docde.domain.userDetails;

import com.docde.domain.auth.entity.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDetailsTest {
    @Test
    public void test() {
        UserDetailsImpl mockUserDetails = Mockito.mock(UserDetailsImpl.class);
        when(mockUserDetails.getUsername()).thenReturn("testUser");
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_DOCTOR_PRESIDENT"));
        Mockito.when(mockUserDetails.getAuthorities()).thenReturn((Collection) authorities);

        // Authentication 모킹
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(mockUserDetails);

        // SecurityContext 모킹
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // SecurityContextHolder에 설정
        SecurityContextHolder.setContext(securityContext);

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        assertEquals("testUser", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(authority->
                authority.getAuthority().equals("ROLE_DOCTOR_PRESIDENT")));
    }
}
