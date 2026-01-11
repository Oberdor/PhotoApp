package org.ks.photoapp.domain.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ks.photoapp.domain.user.dto.UserDto;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.*;

class UserServiceTest {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void registerUser_encodesPasswordAndSaves() {
        UserDto dto = new UserDto();
        dto.setEmail("a@b.c");
        dto.setPassword("plain");

        when(passwordEncoder.encode("plain")).thenReturn("encoded");

        userService.registerUser(dto);

        verify(passwordEncoder).encode("plain");
        verify(userRepository).save(any());
    }
}
