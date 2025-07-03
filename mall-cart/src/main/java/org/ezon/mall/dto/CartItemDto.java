package org.ezon.mall.dto;

import org.ezon.mall.entity.CartItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDto {
    private Long productId;
    private int quantity;
    private String name;
    private int price;
    private int discountPrice;
    private String image;
    private int shippingFee;

    public static CartItemDto fromEntity(CartItem entity) {
        CartItemDto dto = new CartItemDto();
        dto.setProductId(entity.getProductId());
        dto.setQuantity(entity.getQuantity());
        // 필요한 상품 정보는 상품 서비스에서 연동 필요!
        // 예시로 CartItem 엔티티에 연관된 상품 정보를 직접 가져오는 구조면 직접 맵핑
        return dto;
    }
}
