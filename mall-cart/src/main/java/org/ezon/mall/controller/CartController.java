package org.ezon.mall.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.ezon.mall.dto.CartItemDto;
import org.ezon.mall.dto.CartItemRequestDTO;
import org.ezon.mall.dto.DeleteCartItemsRequestDTO;
import org.ezon.mall.dto.UserDto;
import org.ezon.mall.entity.CartItem;
import org.ezon.mall.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/cart")
public class CartController {
	
	@Autowired
	private CartService cartService;
	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	
	public CartController(CartService cartService) {
		this.cartService = cartService;
	}

	// ======= [수정] 세션에서 userId 추출 유틸 =======
	private Long getUserIdFromSession() {
		String userId = redisTemplate.opsForValue().get("userId");
	    System.out.println("userId : " + userId);
	    if (userId == null)
	        throw new RuntimeException("로그인이 필요합니다.");
	    return Long.parseLong(userId);
	}


	// 장바구니 상품 추가 (userId를 세션에서 넣어줌)
	@PostMapping
	public ResponseEntity<String> addCartItem(HttpSession session, @RequestBody CartItemRequestDTO req) {
	    try {
	        Long userId = getUserIdFromSession();
	        System.out.println("★ [DEBUG] AddCartItem - userId: " + userId + ", productId: " + req.getProductId());
	        cartService.addCartItem(userId, req);
	        return ResponseEntity.ok("장바구니에 상품이 담겼습니다!");
	    } catch (Exception e) {
	        System.err.println("★ [ERROR] AddCartItem failed: " + e.getMessage());
	        return ResponseEntity.status(401).body(e.getMessage());
	    }
	}
	
	// 장바구니 목록 조회 (파라미터 userId 제거, 세션 기반)
	@GetMapping
	public ResponseEntity<List<CartItem>> getCartItems(HttpSession session) {
	    Long userId = getUserIdFromSession();
	    List<CartItem> items = cartService.getCartItems(userId);
	    System.out.println(userId);
	    return ResponseEntity.ok(items);
	}
	
	// 장바구니 상품 수량 변경
	@PutMapping("/{cartItemId}")
	public ResponseEntity<String> updateCartItem(
	        HttpSession session,
	        @PathVariable Long cartItemId,
	        @RequestBody CartItemRequestDTO req) {
	    Long userId = getUserIdFromSession();
	    cartService.updateCartItem(userId, cartItemId, req.getQuantity());
	    return ResponseEntity.ok("수량이 변경되었습니다.");
	}
	
	// 장바구니 상품 삭제
	@DeleteMapping("/{cartItemId}") 
	public ResponseEntity<String> deleteCartItem(HttpSession session, @PathVariable Long cartItemId) {
	    Long userId = getUserIdFromSession();
	    cartService.deleteCartItem(userId, cartItemId);
	    return ResponseEntity.ok("상품이 장바구니에서 삭제되었습니다.");
	}
	
	// 결제 완료 후 여러 cartItemId를 한 번에 삭제 
	@DeleteMapping("/items")
	public ResponseEntity<Void> deleteCartItems(
	        HttpSession session,
	        @RequestBody DeleteCartItemsRequestDTO req) {
	    Long userId = getUserIdFromSession();
	    cartService.deleteItemsByCartItemIds(userId, req.getCartItems());
	    return ResponseEntity.ok().build();
	}
	
	// 장바구니에서 특정 상품ID만 조회 (userId 파라미터 제거)
    @GetMapping("/items")
    public ResponseEntity<List<CartItemDto>> getCartItemsByIds(
        HttpSession session,
        @RequestParam String ids // "1,2,3" 형태
    ) {
        Long userId = getUserIdFromSession();
        List<Long> productIds = Arrays.stream(ids.split(","))
                                .filter(s -> !s.isBlank())
                                .map(Long::parseLong)
                                .collect(Collectors.toList());
        List<CartItemDto> cartItems = cartService.findCartItemsByUserAndProductIds(userId, productIds);
        return ResponseEntity.ok(cartItems);
    }
}
