package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.entity.Table;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.TableRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableServiceMergeTest {

    @Mock
    private TableRepository tableRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private TableService tableService;

    @Test
    void mergeTables_shouldMarkBothTablesWithSameMergedTableId() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);

        Table primaryTable = Table.builder()
                .id(1L)
                .restaurant(restaurant)
                .tableNumber("A1")
                .capacity(4)
                .status(Table.TableStatus.OPEN)
                .build();

        Table secondaryTable = Table.builder()
                .id(2L)
                .restaurant(restaurant)
                .tableNumber("A2")
                .capacity(4)
                .status(Table.TableStatus.OPEN)
                .build();

        when(tableRepository.findById(1L)).thenReturn(java.util.Optional.of(primaryTable));
        when(tableRepository.findById(2L)).thenReturn(java.util.Optional.of(secondaryTable));
        when(tableRepository.save(any(Table.class))).thenAnswer(invocation -> invocation.getArgument(0));

        tableService.mergeTables(1L, 1L, 2L);

        assertEquals(2L, primaryTable.getMergedTableId());
        assertEquals(2L, secondaryTable.getMergedTableId());
    }

    @Test
    void mergeTables_shouldThrowWhenEitherTableIsNotOpen() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);

        Table primaryTable = Table.builder()
                .id(1L)
                .restaurant(restaurant)
                .tableNumber("A1")
                .capacity(4)
                .status(Table.TableStatus.OPEN)
                .build();

        Table secondaryTable = Table.builder()
                .id(2L)
                .restaurant(restaurant)
                .tableNumber("A2")
                .capacity(4)
                .status(Table.TableStatus.OCCUPIED)
                .build();

        when(tableRepository.findById(1L)).thenReturn(java.util.Optional.of(primaryTable));
        when(tableRepository.findById(2L)).thenReturn(java.util.Optional.of(secondaryTable));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tableService.mergeTables(1L, 1L, 2L));

        assertEquals("Both tables must be open to merge", exception.getMessage());
    }

    @Test
    void unmergeTables_shouldClearMergedTableIdForBothTables() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);

        Table primaryTable = Table.builder()
                .id(1L)
                .restaurant(restaurant)
                .tableNumber("A1")
                .capacity(4)
                .status(Table.TableStatus.OCCUPIED)
                .mergedTableId(2L)
                .build();

        Table secondaryTable = Table.builder()
                .id(2L)
                .restaurant(restaurant)
                .tableNumber("A2")
                .capacity(4)
                .status(Table.TableStatus.OCCUPIED)
                .mergedTableId(2L)
                .build();

        when(tableRepository.findById(1L)).thenReturn(java.util.Optional.of(primaryTable));
        when(tableRepository.findById(2L)).thenReturn(java.util.Optional.of(secondaryTable));
        when(tableRepository.save(any(Table.class))).thenAnswer(invocation -> invocation.getArgument(0));

        tableService.unmergeTables(1L, 1L, 2L);

        assertNull(primaryTable.getMergedTableId());
        assertNull(secondaryTable.getMergedTableId());
    }
}
