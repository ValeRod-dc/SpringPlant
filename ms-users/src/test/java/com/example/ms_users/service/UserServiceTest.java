package com.example.ms_users.service;

import com.example.ms_users.dto.request.UserUpdateDTO;
import com.example.ms_users.exception.custom.EmailAlreadyExistsException;
import com.example.ms_users.exception.custom.UserNotFoundException;
import com.example.ms_users.exception.custom.UsernameAlreadyExistsException;
import com.example.ms_users.model.Role;
import com.example.ms_users.model.User;
import com.example.ms_users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .username("testUser")
                .email("test@example.com")
                .password("encodedPass")
                .role(Role.CLIENT)
                .phone("123456789")
                .address("Calle Test 123")
                .build();
    }

    @Test
    void shouldReturnTrueWhenUserExistsById() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = userService.existsById(1L);

        // Then
        assertTrue(result);
        verify(userRepository).existsById(1L);
    }

    @Test
    void shouldReturnFalseWhenUserDoesNotExistById() {
        // Given
        when(userRepository.existsById(999L)).thenReturn(false);

        // When
        boolean result = userService.existsById(999L);

        // Then
        assertFalse(result);
        verify(userRepository).existsById(999L);
    }

    @Test
    void shouldRegisterUserWhenValid() {
        // Given
        when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.register("newUser", "password", Role.CLIENT, "new@example.com", "987654321", "Nueva Calle");

        // Then
        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowUsernameAlreadyExistsExceptionOnRegister() {
        // Given
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

        // When / Then
        assertThrows(UsernameAlreadyExistsException.class, () ->
                userService.register("testUser", "pass", Role.CLIENT, "email@test.com", "123", "addr")
        );
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowEmailAlreadyExistsExceptionOnRegister() {
        // Given
        when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When / Then
        assertThrows(EmailAlreadyExistsException.class, () ->
                userService.register("newUser", "pass", Role.CLIENT, "test@example.com", "123", "addr")
        );
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldFindUserByUsername() {
        // Given
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findByUsername("testUser");

        // Then
        assertTrue(result.isPresent());
        assertEquals("testUser", result.get().getUsername());
        verify(userRepository).findByUsername("testUser");
    }

    @Test
    void shouldFindUserByUsernameOrThrow() {
        // Given
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

        // When
        User result = userService.findByUsernameOrThrow("testUser");

        // Then
        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        verify(userRepository).findByUsername("testUser");
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUsernameNotFound() {
        // Given
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // When / Then
        assertThrows(UserNotFoundException.class, () -> userService.findByUsernameOrThrow("unknown"));
        verify(userRepository).findByUsername("unknown");
    }

    @Test
    void shouldUpdateUserProfile() {
        // Given
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setEmail("new@example.com");
        updateDTO.setPhone("111111111");
        updateDTO.setAddress("Nueva Dirección");

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.updateUserProfile("testUser", updateDTO);

        // Then
        assertNotNull(result);
        assertEquals("new@example.com", result.getEmail());
        assertEquals("111111111", result.getPhone());
        assertEquals("Nueva Dirección", result.getAddress());
        verify(userRepository).save(testUser);
    }

    @Test
    void shouldThrowEmailAlreadyExistsWhenUpdatingToExistingEmail() {
        // Given
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setEmail("existing@example.com");
        User otherUser = User.builder().userId(2L).username("other").email("existing@example.com").build();
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(otherUser));

        // When / Then
        assertThrows(EmailAlreadyExistsException.class, () ->
                userService.updateUserProfile("testUser", updateDTO)
        );
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldDeleteByUsername() {
        // Given
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).deleteByUsername("testUser");

        // When
        userService.deleteByUsername("testUser");

        // Then
        verify(userRepository).deleteByUsername("testUser");
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenDeletingNonExisting() {
        // Given
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // When / Then
        assertThrows(UserNotFoundException.class, () -> userService.deleteByUsername("unknown"));
        verify(userRepository, never()).deleteByUsername(anyString());
    }
}