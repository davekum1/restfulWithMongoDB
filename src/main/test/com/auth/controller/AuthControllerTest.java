package com.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.auth.domain.User;
import com.auth.service.AuthService;

@RunWith(SpringRunner.class)
public class AuthControllerTest {
    private static final String AUTH_URI= "/api/auth/login";
    private MockMvc mockMvc;

    @InjectMocks
	private AuthController authController;

    @Mock
	private AuthService authService;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        assertThat(this.authService).isNotNull();
        this.mockMvc = MockMvcBuilders.standaloneSetup(this.authController)
            .build();
    }
    
    @Test
    public void testLoginHappyPath() throws Exception {
        User user = User.builder().userName("john").userPassword("doe").build();
        when(authService.login(anyString(), anyString())).thenReturn(user);
        MockHttpServletRequestBuilder post = MockMvcRequestBuilders
            .post(AUTH_URI)
            .param("userName", "John")
            .param("userPassword", "doe")
            .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(post)
            .andExpect(status().isBadRequest());
    }
}
