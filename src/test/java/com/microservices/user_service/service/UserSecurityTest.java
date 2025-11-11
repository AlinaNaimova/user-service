package com.microservices.user_service.service;

import com.microservices.user_service.config.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSecurityTest {

    private final UserSecurity userSecurity = new UserSecurity();

    @Test
    void checkUserIdWithNullAuthenticationShouldReturnFalse() {
        assertFalse(userSecurity.checkUserId(null, 1L));
    }

    @Test
    void checkUserIdWithUnauthenticatedUserShouldReturnFalse() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);

        assertFalse(userSecurity.checkUserId(authentication, 1L));
    }

    @Test
    void checkUserIdWithSameUserIdShouldReturnTrue() {
        UserPrincipal principal = new UserPrincipal(1L, "user@test.com", "ROLE_USER");
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);

        assertTrue(userSecurity.checkUserId(authentication, 1L));
    }

    @Test
    void checkUserIdWithDifferentUserIdButAdminShouldReturnTrue() {
        UserPrincipal principal = new UserPrincipal(2L, "admin@test.com", "ROLE_ADMIN");
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);

        assertTrue(userSecurity.checkUserId(authentication, 1L));
    }

    @Test
    void checkUserIdWithDifferentUserIdAndUserRoleShouldReturnFalse() {
        UserPrincipal principal = new UserPrincipal(2L, "user@test.com", "ROLE_USER");
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);

        assertFalse(userSecurity.checkUserId(authentication, 1L));
    }

    @Test
    void checkUserIdWithNullUserIdAndAdminRoleShouldReturnTrue() {
        UserPrincipal principal = new UserPrincipal(1L, "admin@test.com", "ROLE_ADMIN");
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);

        assertTrue(userSecurity.checkUserId(authentication, null));
    }

    @Test
    void checkUserIdWithNullUserIdAndUserRoleShouldReturnFalse() {
        UserPrincipal principal = new UserPrincipal(1L, "user@test.com", "ROLE_USER");
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);

        assertFalse(userSecurity.checkUserId(authentication, null));
    }

    @Test
    void checkCardAccessShouldDelegateToCheckUserId() {
        UserPrincipal principal = new UserPrincipal(1L, "user@test.com", "ROLE_USER");
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);

        assertTrue(userSecurity.checkCardAccess(authentication, 1L));
    }
}