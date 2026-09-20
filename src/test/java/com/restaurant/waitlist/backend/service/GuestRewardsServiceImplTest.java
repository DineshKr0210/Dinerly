package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.response.GuestRewardsProfileResponse;
import com.restaurant.waitlist.backend.entity.RewardTier;
import com.restaurant.waitlist.backend.repository.*;
import com.restaurant.waitlist.backend.service.impl.GuestRewardsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuestRewardsServiceImplTest {

    @Mock
    private PointsService pointsService;

    @Mock
    private RewardTierRepository rewardTierRepository;

    @Mock
    private RewardItemRepository rewardItemRepository;

    @Mock
    private PointsEarningRuleRepository pointsEarningRuleRepository;

    @Mock
    private RedemptionRepository redemptionRepository;

    @Mock
    private DinerlyPointsRepository dinerlyPointsRepository;

    private GuestRewardsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GuestRewardsServiceImpl(
                pointsService,
                rewardTierRepository,
                rewardItemRepository,
                pointsEarningRuleRepository,
                redemptionRepository,
                dinerlyPointsRepository
        );
    }

    @Test
    void getRewardsProfile_shouldHandleUserBelowFirstTier() {
        when(pointsService.getBalance(9L)).thenReturn(25L);
        when(rewardTierRepository.findTierForPoints(1L, 25L)).thenReturn(java.util.Optional.empty());
        when(rewardTierRepository.findByRestaurantIdOrderByTierOrderAsc(1L))
                .thenReturn(List.of(
                        RewardTier.builder().id(1L).name("Silver").pointsThreshold(100L).tierOrder(1).color("silver").build(),
                        RewardTier.builder().id(2L).name("Gold").pointsThreshold(250L).tierOrder(2).color("gold").build()
                ));
        when(rewardItemRepository.findByRestaurantIdAndAvailableTrue(1L)).thenReturn(List.of());
        when(pointsEarningRuleRepository.findByRestaurantId(1L)).thenReturn(List.of());

        GuestRewardsProfileResponse response = service.getRewardsProfile(9L, 1L);

        assertNotNull(response);
        assertEquals(25L, response.getCurrentPoints());
        assertEquals(null, response.getCurrentTier());
        assertNotNull(response.getTierProgress());
        assertEquals("Silver", response.getTierProgress().getNextTierName());
        assertEquals(75L, response.getTierProgress().getPointsToNextTier());
        assertEquals(0.0, response.getTierProgress().getProgressPercentage());
    }
}
