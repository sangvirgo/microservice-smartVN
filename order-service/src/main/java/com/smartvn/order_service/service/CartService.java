package com.smartvn.order_service.service;

import com.smartvn.order_service.client.ProductServiceClient;
import com.smartvn.order_service.client.UserServiceClient;
import com.smartvn.order_service.dto.cart.AddItemRequest;
import com.smartvn.order_service.dto.product.InventoryCheckRequest;
import com.smartvn.order_service.dto.product.InventoryDTO;
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
    public Cart addCartItem(Long userId, AddItemRequest req) {
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

        Optional<CartItem> existingItem = cartRepository
                .findByCartIdAndProductIdAndSize(cart.getId(), req.getProductId(), req.getSize());

        if(existingItem.isPresent()) {
            CartItem ci = existingItem.get();
            int newTotalQuantity = ci.getQuantity() + req.getQuantity();

            InventoryCheckRequest recheckRequest = new InventoryCheckRequest(
                    req.getProductId(),
                    req.getSize(),
                    newTotalQuantity  // ← Quan trọng!
            );

            Boolean hasEnoughStock = productServiceClient.checkInventoryAvailability(recheckRequest);
            if (!hasEnoughStock) {
                throw new AppException(
                        "Không đủ hàng. Tồn kho hiện tại không đủ cho số lượng yêu cầu.",
                        HttpStatus.BAD_REQUEST
                );
            }

            ci.setQuantity(newTotalQuantity);
            cartItemRepository.save(ci);
        } else {
            CartItem ci = new CartItem();
            ci.setCart(cart);
            ci.setProductId(req.getProductId());
            ci.setSize(req.getSize());
            ci.setQuantity(req.getQuantity());
            ci.setPrice(inventoryDTO.getPrice());
            ci.setDiscountedPrice(inventoryDTO.getDiscountedPrice());
            cartItemRepository.save(ci);
        }

        reCalculateCart(cart);
        return cartRepository.save(cart);
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

        int totalItems = items.stream()
                .mapToInt(CartItem::getQuantity).sum();

        int originalPrice = items.stream()
                .mapToInt(item -> item.getPrice().intValue() * item.getQuantity()).sum();

        int discountPrice = items.stream()
                .mapToInt(item -> item.getDiscountedPrice().intValue() * item.getQuantity()).sum();

        cart.setTotalItems(totalItems);
        cart.setTotalDiscountedPrice(discountPrice);
        cart.setOriginalPrice(originalPrice);
        cart.setDiscount(originalPrice-discountPrice);

    }

    @CircuitBreaker(name = "userService", fallbackMethod = "validateUserFallback")
    @Retry(name = "userService")
    private void validateUser(Long userId) {
        try {
            UserDTO user = userServiceClient.getUserById(userId);
            if(user == null) {
                throw new AppException("User not found", HttpStatus.NOT_FOUND);
            }
            if(user.getIsBanned()) {
                throw new AppException("User is already banned", HttpStatus.FORBIDDEN);
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

