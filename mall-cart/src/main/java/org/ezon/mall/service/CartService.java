package org.ezon.mall.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.ezon.mall.dto.CartItemDto;
import org.ezon.mall.dto.CartItemRequestDTO;
import org.ezon.mall.dto.UserDto;
import org.ezon.mall.entity.Cart;
import org.ezon.mall.entity.CartItem;
import org.ezon.mall.exception.CartErrorCode;
import org.ezon.mall.exception.CartException;
import org.ezon.mall.repository.CartItemRepository;
import org.ezon.mall.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartService {
	
	@Autowired
	private CartRepository cartRepository;
	
	@Autowired
	private CartItemRepository cartItemRepository;

	public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository) {
		this.cartRepository = cartRepository;
		this.cartItemRepository = cartItemRepository;
	}

	// 장바구니에 상품 추가 (userId 인자 추가)
	public void addCartItem(Long userId, CartItemRequestDTO req) {
		System.out.println("Ok1");
		if(req.getQuantity() <= 0) {
			throw new CartException("상품의 수량은 1개 이상이어야 합니다.", CartErrorCode.INVALID_REQUEST);
		}
		
		System.out.println("Ok2");
		Cart cart = cartRepository.findByUserId(userId);
		if(cart == null) {
			cart = Cart.builder().userId(userId).build();
			cart = cartRepository.save(cart);
		}
		System.out.println("Ok3");
		
		List<CartItem> items = cartItemRepository.findByCartId(cart.getCartId());
		CartItem target = null;
		for(int i = 0; i < items.size(); i++) {
			CartItem item = items.get(i);
			if(item.getProductId().equals(req.getProductId())) {
				target = item;
				break;
			}
		}
		
		if(target != null) {
			System.out.println("Ok4");
			target.setQuantity(target.getQuantity() + req.getQuantity());
			if(req.getSelected() != null) {
				target.setSelected(req.getSelected());
			}
			cartItemRepository.save(target);
		} else {
			System.out.println("Ok5");
			CartItem cartItem = CartItem.builder()
					.cartId(cart.getCartId())
					.productId(req.getProductId())
					.quantity(req.getQuantity())
					.selected(req.getSelected())
					.build();
			cartItemRepository.save(cartItem);
		}
	}
	
	// 장바구니 목록 조회 (userId 인자 유지)
	public List<CartItem> getCartItems(Long userId) {
		Cart cart = cartRepository.findByUserId(userId);
		if(cart == null) {
			return new ArrayList<CartItem>();
		}
		return cartItemRepository.findByCartId(cart.getCartId());
	}
	
	
	// 장바구니 상품 수량 변경 (userId 인자 추가)
	public void updateCartItem(Long userId, Long cartItemId, int quantity) {
		if(quantity <= 0) {
			throw new CartException("상품의 수량은 1개 이상이어야 합니다.", CartErrorCode.INVALID_REQUEST);
		}
		Optional<CartItem> opt = cartItemRepository.findById(cartItemId);
		if(!opt.isPresent()) {
			throw new CartException("장바구니에 해당 상품이 없습니다.", CartErrorCode.NOT_FOUND);
		}
		CartItem item = opt.get();
		// 소유권 체크(실무 필수! 미구현 시 주석 남겨둠)
		// if (!item.getCart().getUserId().equals(userId)) throw new CartException(...);
		item.setQuantity(quantity);
		cartItemRepository.save(item);
	}
	
	// 장바구니 상품 삭제 (userId 인자 추가)
	public void deleteCartItem(Long userId, Long cartItemId) {
		Optional<CartItem> opt = cartItemRepository.findById(cartItemId);
		if(!opt.isPresent()) {
			throw new CartException("장바구니에 해당 상품이 없습니다.", CartErrorCode.NOT_FOUND);
		}
		CartItem item = opt.get();
		// 소유권 체크(실무 필수! 미구현 시 주석 남겨둠)
		// if (!item.getCart().getUserId().equals(userId)) throw new CartException(...);
		cartItemRepository.deleteById(cartItemId);
	}
	
	// 결제후 장바구니 상품 삭제 (userId 인자 추가)
	public void deleteItemsByCartItemIds(Long userId, List<Long> cartItems) {
	    if (cartItems == null || cartItems.isEmpty()) {
	        throw new CartException("삭제할 상품 정보가 없습니다.", CartErrorCode.INVALID_REQUEST);
	    }
	    boolean deleted = false;
	    for (Long cartItemId : cartItems) {
	        Optional<CartItem> opt = cartItemRepository.findById(cartItemId);
	        if(opt.isPresent()) {
	            CartItem item = opt.get();
	            // 소유권 체크(실무 필수! 미구현 시 주석 남겨둠)
	            // if (!item.getCart().getUserId().equals(userId)) continue;
	            cartItemRepository.deleteById(cartItemId);
	            deleted = true;
	        }
	    }
	    if (!deleted) {
	        throw new CartException("삭제할 장바구니 상품이 없습니다.", CartErrorCode.NOT_FOUND);
	    }
	}
	
	// 장바구니에서 유저+상품ID 리스트로만 조회
    public List<CartItemDto> findCartItemsByUserAndProductIds(Long userId, List<Long> productIds) {
        List<CartItem> cartItems = cartItemRepository.findByUserIdAndProductIdIn(userId, productIds);
        return cartItems.stream()
                .map(CartItemDto::fromEntity)
                .collect(Collectors.toList());
    }

}
