package com.hotelbooking.controller;

import com.hotelbooking.dto.LoginDTO;
import com.hotelbooking.dto.UserDTO;
import com.hotelbooking.entity.User;
import com.hotelbooking.enums.Role;
import com.hotelbooking.exception.AuthenticationFailedException;
import com.hotelbooking.mapper.ManagerHotelMapper;
import com.hotelbooking.mapper.UserMapper;
import com.hotelbooking.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ManagerHotelMapper managerHotelMapper;

    @InjectMocks
    private AuthController authController;

    @Test
    void adminLoginShouldRedirectToDashboardWhenAuthenticationSucceeds() {
        LoginDTO loginDTO = LoginDTO.builder().email("admin@test.com").password("secret").build();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(true)).thenReturn(session);
        when(authService.login("admin@test.com", "secret", Role.ROLE_ADMIN))
                .thenReturn(User.builder().id(1L).build());

        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        String view = authController.adminLogin(loginDTO, request, ra);

        assertEquals("redirect:/admin/dashboard", view);
        verify(session).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );
    }

    @Test
    void userLoginShouldRedirectBackWhenAuthenticationFails() {
        LoginDTO loginDTO = LoginDTO.builder().email("user@test.com").password("bad").build();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(authService.login("user@test.com", "bad", Role.ROLE_USER))
                .thenThrow(new AuthenticationFailedException("Invalid email or password"));

        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        String view = authController.userLogin(loginDTO, request, ra);

        assertEquals("redirect:/userLogin", view);
        assertEquals("Invalid email or password", ra.getFlashAttributes().get("error"));
    }

    @Test
    void userRegisterShouldSetRoleEnabledAndRedirect() {
        UserDTO userDTO = UserDTO.builder()
                .email("user@test.com")
                .password("secret123")
                .fullName("User")
                .build();
        User mapped = User.builder().email("user@test.com").password("encoded").build();
        when(userMapper.toEntity(userDTO)).thenReturn(mapped);

        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        String view = authController.userRegister(userDTO, ra);

        verify(authService).registerUser(mapped, Role.ROLE_USER);
        assertEquals(Role.ROLE_USER, userDTO.getRole());
        assertEquals("redirect:/userLogin", view);
    }
}
