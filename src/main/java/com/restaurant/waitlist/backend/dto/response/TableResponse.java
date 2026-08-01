package com.restaurant.waitlist.backend.dto.response;

import com.restaurant.waitlist.backend.entity.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableResponse {
    private Long id;
    private String tableNumber;
    private Integer capacity;
    private Table.TableStatus status;
    private Long mergedTableId;

    public static TableResponse fromTable(Table table) {
        return TableResponse.builder()
                .id(table.getId())
                .tableNumber(table.getTableNumber())
                .capacity(table.getCapacity())
                .status(table.getStatus())
                .mergedTableId(table.getMergedTableId())
                .build();
    }
}

