package com.smartvn.order_service.dto.cart;

import com.smartvn.order_service.client.ProductServiceClient;
import com.smartvn.order_service.dto.product.ProductDTO;
import com.smartvn.order_service.model.CartItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDTO {
    private Long id;
    private Long productId;
    private String size;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal discountedPrice;
    private String productName;
    private String imageUrl;
    private Integer discountPercent;

    /**
     * Constructor từ CartItem entity
     * KHÔNG gọi ProductService trong constructor
     * Sẽ được enrich sau bởi service layer
     */
    public CartItemDTO(CartItem cartItem) {
        this.id = cartItem.getId();
        this.productId = cartItem.getProductId();
        this.size = cartItem.getSize();
        this.quantity = cartItem.getQuantity();
        this.price = cartItem.getPrice();
        this.discountedPrice = cartItem.getDiscountedPrice();
        this.discountPercent = cartItem.getDiscountPercent();

        // productName và imageUrl sẽ được set sau
        this.productName = null;
        this.imageUrl = null;
    }

    /**
     * Enrich với thông tin từ ProductDTO
     */
    public void enrichWithProductInfo(ProductDTO product) {
        if (product != null) {
            this.productName = product.getTitle();
            this.imageUrl = product.getFirstImageUrl();
        }
    }
}