package com.smartvn.product_service.service;

import com.smartvn.product_service.dto.ProductDetailDTO;
import com.smartvn.product_service.dto.ProductListingDTO;
import com.smartvn.product_service.model.Inventory;
import com.smartvn.product_service.model.Product;
import com.smartvn.product_service.model.Store;
import com.smartvn.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final StoreService storeService;

    public Page<ProductListingDTO> searchProducts(
            String keyword,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {
        log.info("Searching products with keyword: {}, categoryId: {}, minPrice: {}, maxPrice: {}",
                keyword, categoryId, minPrice, maxPrice);
        return productRepository.searchProducts(
                keyword, categoryId, minPrice, maxPrice, pageable
        ).map(this::toListingDTO);
    }

    public ProductDetailDTO getProductDetail(Long productId) {
        log.info("Fetching detail for product ID: {}", productId);
        Product product = productRepository.findByIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new RuntimeException("Product not found or is inactive"));

        ProductDetailDTO dto = mapProductToDetailDTO(product);

        List<Inventory> inventories = inventoryService.getInventoriesByProduct(productId);
        List<ProductDetailDTO.PriceVariantDTO> priceVariants = inventories.stream().map(inventory -> {
            ProductDetailDTO.PriceVariantDTO variantDTO = new ProductDetailDTO.PriceVariantDTO();
            variantDTO.setInventoryId(inventory.getId());
            variantDTO.setStoreId(inventory.getStoreId());
            // Lấy tên cửa hàng
            Store store = storeService.getStoreById(inventory.getStoreId());
            variantDTO.setStoreName(store.getName());
            variantDTO.setSize(inventory.getSize());
            variantDTO.setPrice(inventory.getPrice());
            variantDTO.setDiscountPercent(inventory.getDiscountPercent());
            variantDTO.setDiscountedPrice(inventory.getDiscountedPrice());
            variantDTO.setQuantity(inventory.getQuantity());
            variantDTO.setInStock(inventory.getQuantity() > 0);
            return variantDTO;
        }).collect(Collectors.toList());
        dto.setPriceVariants(priceVariants);

        return dto;
    }

    private ProductListingDTO toListingDTO(Product product) {
        ProductListingDTO dto = new ProductListingDTO();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setBrand(product.getBrand());
        dto.setAverageRating(product.getAverageRating());
        dto.setNumRatings(product.getNumRatings());
        dto.setQuantitySold(product.getQuantitySold());

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            dto.setThumbnailUrl(product.getImages().get(0).getDownloadUrl());
        }

        // Tính toán giá và tình trạng kho từ inventory
        List<Inventory> inventories = inventoryService.getInventoriesByProduct(product.getId());
        if (!inventories.isEmpty()) {
            BigDecimal minPrice = inventories.stream().map(Inventory::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal maxPrice = inventories.stream().map(Inventory::getPrice).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal minDiscountedPrice = inventories.stream().map(Inventory::getDiscountedPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal maxDiscountedPrice = inventories.stream().map(Inventory::getDiscountedPrice).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            int totalStock = inventories.stream().mapToInt(Inventory::getQuantity).sum();

            dto.setPriceRange(formatPriceRange(minPrice, maxPrice));
            dto.setDiscountedPriceRange(formatPriceRange(minDiscountedPrice, maxDiscountedPrice));
            dto.setInStock(totalStock > 0);
        } else {
            // ⚠️ Nên set giá trị mặc định
            dto.setInStock(false);
            dto.setPriceRange("N/A");
            dto.setDiscountedPriceRange("N/A");
        }

        return dto;
    }

    private ProductDetailDTO mapProductToDetailDTO(Product product) {
        ProductDetailDTO dto = new ProductDetailDTO();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setBrand(product.getBrand());
        dto.setDescription(product.getDescription());
        dto.setDetailedReview(product.getDetailedReview());
        dto.setPowerfulPerformance(product.getPowerfulPerformance());
        dto.setColor(product.getColor());
        dto.setWeight(product.getWeight());
        dto.setDimension(product.getDimension());
        dto.setBatteryType(product.getBatteryType());
        dto.setBatteryCapacity(product.getBatteryCapacity());
        dto.setRamCapacity(product.getRamCapacity());
        dto.setRomCapacity(product.getRomCapacity());
        dto.setScreenSize(product.getScreenSize());
        dto.setConnectionPort(product.getConnectionPort());
        if (product.getImages() != null) {
            dto.setImageUrls(product.getImages().stream().map(com.smartvn.product_service.model.Image::getDownloadUrl).collect(Collectors.toList()));
        }
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        dto.setAverageRating(product.getAverageRating());
        dto.setNumRatings(product.getNumRatings());
        dto.setQuantitySold(product.getQuantitySold());
        dto.setIsActive(product.getIsActive());
        return dto;
    }

    private String formatPriceRange(BigDecimal min, BigDecimal max) {
        if (min == null || max == null) {
            return "";
        }
        if (min.compareTo(max) == 0) {
            return String.format("%,.0fđ", min);
        }
        return String.format("%,.0fđ - %,.0fđ", min, max);
    }
}