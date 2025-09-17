package com.webanhang.team_project.controller.chat;

import com.webanhang.team_project.dto.product.FilterProduct;
import com.webanhang.team_project.dto.response.ApiResponse;
import com.webanhang.team_project.model.Order;
import com.webanhang.team_project.model.Product;
import com.webanhang.team_project.service.order.IOrderService;
import com.webanhang.team_project.service.product.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/chatbot")
public class ChatbotController {

    @Autowired
    private IProductService productService;

    @Autowired
    private IOrderService orderService;

    @GetMapping("/products/recommendations")
    public ResponseEntity<ApiResponse> getProductRecommendations(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String feature) {

        FilterProduct filterProduct = new FilterProduct();
        filterProduct.setTopLevelCategory(category);
        filterProduct.setMinPrice(minPrice);
        filterProduct.setMaxPrice(maxPrice);
        filterProduct.setColor(feature);

        List<Product> products = productService.findAllProductsByFilter(filterProduct);

        return ResponseEntity.ok(ApiResponse.success(products, "Product recommendations"));
    }

    @GetMapping("/order/{orderId}/status")
    public ResponseEntity<ApiResponse> getOrderStatus(@PathVariable Long orderId) {
        Order order = orderService.findOrderById(orderId);
        return ResponseEntity.ok(ApiResponse.success(order, "Order status"));
    }

    @GetMapping("/product/stock/{productId}")
    public ResponseEntity<ApiResponse> checkProductStock(@PathVariable Long productId) {
        Product product = productService.findProductById(productId);

        Map<String, Object> stock = Map.of(
                "productId", product.getId(),
                "productName", product.getTitle(),
                "inStock", product.getQuantity() > 0,
                "availableQuantity", product.getQuantity()
        );

        return ResponseEntity.ok(ApiResponse.success(stock, "Product stock"));
    }
}