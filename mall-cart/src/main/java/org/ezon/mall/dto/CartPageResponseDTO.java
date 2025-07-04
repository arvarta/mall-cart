package org.ezon.mall.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartPageResponseDTO {
    private List<CartResponseDTO> items; // CartResponseDTO=CartItemDto
    private int page;
    private int size;
    private int totalCount;
}
