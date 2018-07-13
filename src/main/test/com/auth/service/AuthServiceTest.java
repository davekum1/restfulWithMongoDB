package com.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringRunner;

import com.auth.domain.User;

@RunWith(SpringRunner.class)
public class AuthServiceTest {
	
    @Mock
	private AuthService authService;
	
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        assertThat(this.authService).isNotNull();
    }
    
    @Test
    public void testLogin() {
        User user = User.builder().userName("john").userPassword("doe").build();
        when(authService.login(anyString(), anyString())).thenReturn(user);
        assertThat(user.getUserName()).isEqualTo("john");
        assertThat(user.getUserPassword()).isEqualTo("doe");
    }
}
