package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.request.AddTableRequest;
import com.restaurant.waitlist.backend.dto.response.TableResponse;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.entity.Table;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.TableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TableService {

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    public TableResponse addTable(Long restaurantId, AddTableRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        Table table = Table.builder()
                .restaurant(restaurant)
                .tableNumber(request.getTableNumber())
                .capacity(request.getCapacity())
                .status(Table.TableStatus.OPEN)
                .build();

        table = tableRepository.save(table);
        return TableResponse.fromTable(table);
    }

    public List<TableResponse> getRestaurantTables(Long restaurantId) {
        List<Table> tables = tableRepository.findByRestaurantId(restaurantId);
        return tables.stream()
                .map(TableResponse::fromTable)
                .collect(Collectors.toList());
    }

    public void updateTableStatus(Long restaurantId, Long tableId, Table.TableStatus status) {
        Table table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found"));

        if (table.getRestaurant() == null || !table.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("Table does not belong to the specified restaurant");
        }

        table.setStatus(status);
        tableRepository.save(table);
    }

    public Table getTable(Long restaurantId, Long tableId) {
        Table table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found"));

        if (table.getRestaurant() == null || !table.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("Table does not belong to the specified restaurant");
        }

        return table;
    }
}

