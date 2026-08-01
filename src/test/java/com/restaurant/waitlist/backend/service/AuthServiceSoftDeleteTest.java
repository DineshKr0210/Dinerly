package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.request.RegisterRequest;
import com.restaurant.waitlist.backend.entity.EmailVerificationToken;
import com.restaurant.waitlist.backend.entity.User;
import com.restaurant.waitlist.backend.repository.EmailVerificationTokenRepository;
import com.restaurant.waitlist.backend.repository.PasswordResetTokenRepository;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.UserRepository;
import com.restaurant.waitlist.backend.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceSoftDeleteTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerGuest_shouldRestoreSoftDeletedEmailAndSendVerificationAgain() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Alice");
        request.setEmail("alice@example.com");
        request.setPhone("1234567890");
        request.setPassword("secret123");

        User softDeletedUser = User.builder()
                .id(12L)
                .name("Alice")
                .email("alice@example.com")
                .phone("1234567890")
                .password("encoded")
                .role(User.UserRole.GUEST)
                .emailVerified(false)
                .enabled(false)
                .deletedAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmailIncludingDeleted("alice@example.com"))
                .thenReturn(Optional.of(softDeletedUser));
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        when(emailVerificationTokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        authService.registerGuest(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        verify(emailVerificationTokenRepository).save(any(EmailVerificationToken.class));
        verify(emailService).sendVerificationEmail(eq("alice@example.com"), any(String.class));

        User savedUser = userCaptor.getValue();
        assertFalse(savedUser.getEmailVerified());
        assertTrue(savedUser.getEnabled());
        assertNull(savedUser.getDeletedAt());
    }
}
