package com.smartvn.order_service.service;

import com.smartvn.order_service.client.ProductServiceClient;
import com.smartvn.order_service.client.UserServiceClient;
import com.smartvn.order_service.dto.cart.AddItemRequest;
import com.smartvn.order_service.dto.product.InventoryCheckRequest;
import com.smartvn.order_service.dto.product.InventoryItemDTO;
import com.smartvn.order_service.dto.product.ProductDTO;
import com.smartvn.order_service.dto.user.UserDTO;
import com.smartvn.order_service.exceptions.AppException;
import com.smartvn.order_service.model.*;
import com.smartvn.order_service.repository.CartItemRepository;
import com.smartvn.order_service.repository.CartRepository;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserServiceClient  userServiceClient;
    private final ProductServiceClient productServiceClient;

    @Transactional(readOnly = true)
    public Cart getCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(
                        "Cart not found for user: " + userId,
                        HttpStatus.NOT_FOUND
                ));
    }

    @Transactional
    public Cart createCart(Long userId) {
        validateUser(userId);

        if(cartRepository.existsByUserId(userId)) {
            throw new AppException(
                    "Cart already exists for user: " + userId,
                    HttpStatus.CONFLICT
            );
        }

        return createNewCart(userId);
    }

    @Transactional
    public Cart getOrCreateCart(Long userId) {
        validateUser(userId);

        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
    }

    @Transactional
    public CartItem addCartItem(Long userId, AddItemRequest req) { // ✅ SỬA: Thay đổi kiểu trả về từ Cart sang CartItem
        Cart cart = getOrCreateCart(userId);
        ProductDTO dto = productServiceClient.getProductById(req.getProductId());
        if(dto == null || !dto.getIsActive()) {
            throw new AppException("Product not available", HttpStatus.BAD_REQUEST);
        }

        List<InventoryItemDTO> inventoryDTOS = productServiceClient.getProductInventory(req.getProductId());
        InventoryItemDTO inventoryItem = inventoryDTOS.stream()
                .filter(i -> i.getSize().equals(req.getSize()))
                .findFirst()
                .orElseThrow(() -> new AppException("Size not found", HttpStatus.NOT_FOUND));

        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductIdAndSize(cart.getId(), req.getProductId(), req.getSize());

        CartItem savedItem; // Biến để lưu CartItem sẽ được trả về

        if(existingItem.isPresent()) {
            CartItem ci = existingItem.get();
            int newTotalQuantity = ci.getQuantity() + req.getQuantity();

            InventoryCheckRequest recheckRequest = new InventoryCheckRequest(
                    req.getProductId(),
                    req.getSize(),
                    newTotalQuantity
            );

            Boolean hasEnoughStock = productServiceClient.checkInventoryAvailability(recheckRequest);
            if (!hasEnoughStock) {
                throw new AppException(
                        "Không đủ hàng. Tồn kho hiện tại không đủ cho số lượng yêu cầu.",
                        HttpStatus.BAD_REQUEST
                );
            }

            ci.setQuantity(newTotalQuantity);
            savedItem = cartItemRepository.save(ci); // Gán item đã được cập nhật
        } else {
            CartItem ci = new CartItem();
            ci.setCart(cart);
            ci.setProductId(req.getProductId());
            ci.setSize(req.getSize());
            ci.setQuantity(req.getQuantity());
            ci.setPrice(inventoryItem.getPrice());
            ci.setDiscountedPrice(inventoryItem.getDiscountedPrice());
            savedItem = cartItemRepository.save(ci); // Gán item mới được tạo
        }

        reCalculateCart(cart); // Vẫn tính toán lại toàn bộ giỏ hàng
        cartRepository.save(cart);

        return savedItem; // ✅ SỬA: Trả về CartItem cụ thể
    }

    @Transactional
    public Cart updateCartItem(Long userId, Long itemId, AddItemRequest req) {
        Cart cart = getOrCreateCart(userId);

        CartItem item = cartItemRepository.findById(itemId).
                orElseThrow(() -> new AppException("Item not found", HttpStatus.NOT_FOUND));

        if(!item.getCart().getId().equals(cart.getId())) {
            throw new AppException("Unauthorized request", HttpStatus.UNAUTHORIZED);
        }

        if (req.getQuantity() <= 0) {
            cartItemRepository.delete(item);
            reCalculateCart(cart);
            return cartRepository.save(cart);
        }

        InventoryCheckRequest checkRequest = new InventoryCheckRequest(
                item.getProductId(),
                item.getSize(),
                req.getQuantity()
        );
        Boolean hasStock = productServiceClient.checkInventoryAvailability(checkRequest);
        if (!hasStock) {
            throw new AppException("Insufficient stock", HttpStatus.BAD_REQUEST);
        }

        item.setQuantity(req.getQuantity());
        cartItemRepository.save(item);
        reCalculateCart(cart);
        return cartRepository.save(cart);
    }


    public void removeCartItem(Long userId, Long itemId) {
        Cart cart = getOrCreateCart(userId);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException("Item not found", HttpStatus.NOT_FOUND));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new AppException("Unauthorized", HttpStatus.FORBIDDEN);
        }

        cartItemRepository.delete(item);
        reCalculateCart(cart);
        cartRepository.save(cart);
    }

    @Transactional  // Thêm annotation này
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteByCartId(cart.getId());

        cart.setTotalItems(0);
        cart.setOriginalPrice(0);
        cart.setTotalDiscountedPrice(0);
        cart.setDiscount(0);
        cartRepository.save(cart);
    }

    public void reCalculateCart(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        // 1. Tổng số lượng items
        int totalItems = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        // 2. Tổng giá GỐC (chưa giảm)
        int originalPrice = items.stream()
                .mapToInt(item -> item.getPrice().intValue() * item.getQuantity())
                .sum();

        // 3. Tổng giá SAU GIẢM
        int totalDiscountedPrice = items.stream()
                .mapToInt(item -> {
                    // Nếu có giá giảm thì dùng, không thì dùng giá gốc
                    BigDecimal priceToUse = (item.getDiscountedPrice() != null && item.getDiscountedPrice().compareTo(BigDecimal.ZERO) > 0)
                            ? item.getDiscountedPrice()
                            : item.getPrice();
                    return priceToUse.intValue() * item.getQuantity();
                })
                .sum();

        // 4. Tính discount = gốc - giảm
        int discount = originalPrice - totalDiscountedPrice;

        // 5. Set vào cart
        cart.setTotalItems(totalItems);
        cart.setOriginalPrice(originalPrice);
        cart.setTotalDiscountedPrice(totalDiscountedPrice);
        cart.setDiscount(discount);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "validateUserFallback")
    @Retry(name = "userService")
    private void validateUser(Long userId) {
        try {
            UserDTO user = userServiceClient.getUserById(userId);
            if(user == null) {
                throw new AppException("User not found", HttpStatus.NOT_FOUND);
            }
            if (Boolean.TRUE.equals(user.isBanned())) {
                throw new AppException("User is already banned", HttpStatus.FORBIDDEN);
            }
            if (Boolean.FALSE.equals(user.isActive())) {
                throw new AppException("User account is not active", HttpStatus.FORBIDDEN);
            }
        } catch (FeignException.NotFound e) {
            throw new AppException("User not found", HttpStatus.NOT_FOUND);
        }
    }

    private void validateUserFallback(Long userId, Exception e) {
        log.error("Failed to validate user {} after retries: {}",
                userId, e.getMessage());
        throw new AppException(
                "User service temporarily unavailable",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    private Cart createNewCart(Long userId) {
        try {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setTotalItems(0);
            cart.setOriginalPrice(0);
            cart.setTotalDiscountedPrice(0);
            cart.setDiscount(0);
            return cartRepository.save(cart);
        } catch (DataIntegrityViolationException e) {
            log.info("Cart already created for user {}", userId);
            return cartRepository.findByUserId(userId)
                    .orElseThrow(() -> new AppException(
                            "Failed to create cart",
                            HttpStatus.INTERNAL_SERVER_ERROR
                    ));
        }
    }
}

