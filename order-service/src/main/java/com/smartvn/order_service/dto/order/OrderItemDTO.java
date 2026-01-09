package com.smartvn.order_service.dto.order;

import com.smartvn.order_service.dto.product.ProductDTO;
import com.smartvn.order_service.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Long id;
    private Long productId;
    private String productTitle;
    private String imageUrl;
    private Integer quantity;
    private String size;
    private BigDecimal price;
    private BigDecimal discountedPrice;
    private Integer discountPercent;

    /**
     * Constructor từ OrderItem entity
     * KHÔNG gọi ProductService trong constructor
     */
    public OrderItemDTO(OrderItem orderItem) {
        this.id = orderItem.getId();
        this.productId = orderItem.getProductId();
        this.quantity = orderItem.getQuantity();
        this.size = orderItem.getSize();
        this.price = orderItem.getPrice();
        this.discountedPrice = orderItem.getDiscountedPrice();
        this.discountPercent = orderItem.getDiscountPercent();

        // productTitle và imageUrl sẽ được set sau
        this.productTitle = null;
        this.imageUrl = null;
    }

    /**
     * Enrich với thông tin từ ProductDTO
     */
    public void enrichWithProductInfo(ProductDTO product) {
        if (product != null) {
            this.productTitle = product.getTitle();
            this.imageUrl = product.getFirstImageUrl();
        } else {
            log.info("product is null");
        }
    }
}