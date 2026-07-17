package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.entity.Waitlist;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InboundSmsServiceTest {

    @Mock
    private WaitlistRepository waitlistRepository;

    @InjectMocks
    private InboundSmsService inboundSmsService;

    @Test
    void shouldStoreReplyWithoutChangingStatusWhenGuestSaysTheyCannotCome() {
        Waitlist waitlist = Waitlist.builder()
                .id(11L)
                .guestPhone("+919876543210")
                .status(Waitlist.WaitlistStatus.NOTIFIED)
                .build();

        when(waitlistRepository.findByGuestPhone("+919876543210")).thenReturn(Optional.of(waitlist));

        boolean processed = inboundSmsService.processInboundSms(
                "+919876543210",
                "I can't come now",
                "+15551234567",
                "SM123"
        );

        assertTrue(processed);
        assertEquals("I can't come now", waitlist.getLatestCustomerReply());
        assertEquals(Waitlist.WaitlistStatus.NOTIFIED, waitlist.getStatus());
        verify(waitlistRepository).save(waitlist);
    }

    @Test
    void shouldRecognizeNumberedReplyOptionForOnTheWay() {
        Waitlist waitlist = Waitlist.builder()
                .id(12L)
                .guestPhone("+919876543211")
                .status(Waitlist.WaitlistStatus.NOTIFIED)
                .build();

        when(waitlistRepository.findByGuestPhone("+919876543211")).thenReturn(Optional.of(waitlist));

        boolean processed = inboundSmsService.processInboundSms(
                "+919876543211",
                "1",
                "+15551234568",
                "SM124"
        );

        assertTrue(processed);
        assertEquals("1", waitlist.getLatestCustomerReply());
        assertEquals(Waitlist.WaitlistStatus.NOTIFIED, waitlist.getStatus());
    }
}
