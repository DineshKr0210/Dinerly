package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.response.admin.RewardTierResponse;
import com.restaurant.waitlist.backend.entity.RewardTier;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.RewardSettingsRepository;
import com.restaurant.waitlist.backend.repository.RewardTierRepository;
import com.restaurant.waitlist.backend.service.AdminLocationAccessService;
import com.restaurant.waitlist.backend.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminRewardServiceImplTest {

    @Mock
    private RewardTierRepository rewardTierRepository;

    @Mock
    private RewardSettingsRepository rewardSettingsRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private AdminLocationAccessService adminLocationAccessService;

    @Mock
    private AuditLogService auditLogService;

    private AdminRewardServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminRewardServiceImpl(
                rewardTierRepository,
                rewardSettingsRepository,
                restaurantRepository,
                adminLocationAccessService,
                auditLogService
        );
    }

    @Test
    void listTiers_shouldReturnRewardTierThresholdValuesCorrectly() {
        RewardTier tier = RewardTier.builder()
                .id(10L)
                .name("Gold")
                .pointsThreshold(350L)
                .tierOrder(2)
                .color("gold")
                .perks(List.of("Priority seating", "Free dessert"))
                .build();

        PageRequest pageRequest = PageRequest.of(0, 20);
        when(adminLocationAccessService.resolveRestaurantIds(1L)).thenReturn(List.of(1L));
        when(rewardTierRepository.findByRestaurantIdInOrderByTierOrderAsc(List.of(1L), pageRequest))
                .thenReturn(new PageImpl<>(List.of(tier), pageRequest, 1));

        Page<RewardTierResponse> result = service.listTiers(1L, pageRequest);

        assertEquals(1, result.getTotalElements());
        assertEquals(350L, result.getContent().get(0).getPointsThreshold());
        assertEquals("Gold", result.getContent().get(0).getName());
        assertEquals(2, result.getContent().get(0).getTierOrder());
    }
}
