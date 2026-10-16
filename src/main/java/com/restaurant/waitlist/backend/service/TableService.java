package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.request.AddTableRequest;
import com.restaurant.waitlist.backend.dto.response.TableResponse;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.entity.Table;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.TableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (status != Table.TableStatus.OCCUPIED) {
            // Table turned over (cleared/reserved/needs cleaning) - no server is actively serving it anymore.
            table.setCurrentServerId(null);
        }
        tableRepository.save(table);
    }

    /**
     * Marks a table OCCUPIED and records which server is currently serving it. A merged
     * group of tables is treated as a single seating event: the same server/status is
     * propagated to every table in the merge group.
     */
    @Transactional
    public void assignServerToTable(Long restaurantId, Long tableId, Long serverId) {
        Table table = getValidTable(restaurantId, tableId);
        table.setStatus(Table.TableStatus.OCCUPIED);
        table.setCurrentServerId(serverId);
        tableRepository.save(table);

        if (table.getMergedTableId() != null) {
            List<Table> group = tableRepository.findByRestaurantIdAndMergedTableId(restaurantId, table.getMergedTableId());
            for (Table partner : group) {
                if (!partner.getId().equals(table.getId())) {
                    partner.setStatus(Table.TableStatus.OCCUPIED);
                    partner.setCurrentServerId(serverId);
                    tableRepository.save(partner);
                }
            }
        }
    }

    @Transactional
    public void mergeTables(Long restaurantId, Long tableId, Long mergedTableId) {
        Table primaryTable = getValidTable(restaurantId, tableId);
        Table secondaryTable = getValidTable(restaurantId, mergedTableId);

        if (primaryTable.getId().equals(secondaryTable.getId())) {
            throw new RuntimeException("A table cannot be merged with itself");
        }

        if (primaryTable.getRestaurant() == null || !primaryTable.getRestaurant().getId().equals(restaurantId)
                || secondaryTable.getRestaurant() == null || !secondaryTable.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("Tables do not belong to the specified restaurant");
        }

        if (primaryTable.getStatus() != Table.TableStatus.OPEN || secondaryTable.getStatus() != Table.TableStatus.OPEN) {
            throw new RuntimeException("Both tables must be open to merge");
        }

        primaryTable.setMergedTableId(secondaryTable.getId());
        secondaryTable.setMergedTableId(secondaryTable.getId());

        tableRepository.save(primaryTable);
        tableRepository.save(secondaryTable);
    }

    @Transactional
    public void unmergeTables(Long restaurantId, Long tableId, Long mergedTableId) {
        Table primaryTable = getValidTable(restaurantId, tableId);
        Table secondaryTable = getValidTable(restaurantId, mergedTableId);

        if (!primaryTable.getMergedTableId().equals(mergedTableId) || !secondaryTable.getMergedTableId().equals(mergedTableId)) {
            throw new RuntimeException("Tables are not merged with the specified merged table id");
        }

        primaryTable.setMergedTableId(null);
        secondaryTable.setMergedTableId(null);

        tableRepository.save(primaryTable);
        tableRepository.save(secondaryTable);
    }

    private Table getValidTable(Long restaurantId, Long tableId) {
        Table table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found"));

        if (table.getRestaurant() == null || !table.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("Table does not belong to the specified restaurant");
        }

        return table;
    }

    public Table getTable(Long restaurantId, Long tableId) {
        return getValidTable(restaurantId, tableId);
    }
}

