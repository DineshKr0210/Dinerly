package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.request.UpdateSeatedGuestRequest;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.entity.Waitlist;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.RestaurantSettingsRepository;
import com.restaurant.waitlist.backend.repository.TableRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceUpdateSeatedTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private WaitlistRepository waitlistRepository;

    @Mock
    private TableRepository tableRepository;

    @Mock
    private StaffService staffService;

    @Mock
    private TableService tableService;

    @Mock
    private WaitlistService waitlistService;

    @Mock
    private SmsService smsService;

    @Mock
    private RestaurantSettingsRepository restaurantSettingsRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    @Test
    void updateSeatedGuest_shouldUpdatePartySizeAndTableNameForSeatedRecord() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);

        Waitlist waitlist = Waitlist.builder()
                .id(100L)
                .restaurant(restaurant)
                .guestName("John")
                .guestPhone("1234567890")
                .partySize(2)
                .tableName("A1")
                .status(Waitlist.WaitlistStatus.SEATED)
                .build();

        UpdateSeatedGuestRequest request = new UpdateSeatedGuestRequest();
        request.setPartySize(4);
        request.setTableName("B2");

        when(waitlistService.getWaitlistById(1L, 100L)).thenReturn(waitlist);
        when(waitlistRepository.save(any(Waitlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = restaurantService.updateSeatedGuest(1L, 100L, request);

        assertEquals(4, response.getPartySize());
        assertEquals("B2", response.getTableName());
        assertEquals(Waitlist.WaitlistStatus.SEATED, response.getStatus());
    }
}
