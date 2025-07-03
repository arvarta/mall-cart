package org.ezon.mall.dto;

import lombok.Data;

@Data
public class UserDto {
	
	private Long id;
	private String email;
	private String role;
	private String cartItemId;
	private String productId;
	private String quantity;
}
