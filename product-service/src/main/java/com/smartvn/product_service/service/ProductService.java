package com.smartvn.product_service.service;

import com.smartvn.product_service.dto.BulkProductRequest;
import com.smartvn.product_service.dto.ProductDetailDTO;
import com.smartvn.product_service.dto.ProductListingDTO;
import com.smartvn.product_service.model.*;
import com.smartvn.product_service.repository.CategoryRepository;
import com.smartvn.product_service.repository.ImageRepository;
import com.smartvn.product_service.repository.InventoryRepository;
import com.smartvn.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.module.ResolutionException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;
    private final ImageRepository imageRepository;

    /**
     * Tìm kiếm sản phẩm với khả năng lọc theo tên category (level 1 hoặc level 2)
     */
    public Page<ProductListingDTO> searchProducts(
            String keyword,
            String topLevelCategory,
            String secondLevelCategory,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {
        log.info("Searching products - keyword: {}, topLevel: {}, secondLevel: {}, price: {}-{}",
                keyword, topLevelCategory, secondLevelCategory, minPrice, maxPrice);

        // Xác định categoryId dựa trên tên category
        Long categoryId = resolveCategoryId(topLevelCategory, secondLevelCategory);

        if (categoryId == null && (topLevelCategory != null || secondLevelCategory != null)) {
            log.warn("⚠️ Category not found for topLevel: {}, secondLevel: {}",
                    topLevelCategory, secondLevelCategory);
            // Trả về page rỗng nếu category không tồn tại
            return Page.empty(pageable);
        }

        return productRepository.searchProducts(
                keyword, categoryId, minPrice, maxPrice, pageable
        ).map(this::toListingDTO);
    }

    /**
     * Resolve categoryId từ tên category
     * Logic:
     * 1. Nếu có secondLevelCategory -> tìm category cấp 2 với tên đó
     * 2. Nếu chỉ có topLevelCategory -> tìm category cấp 1 với tên đó
     * 3. Nếu không có cả hai -> return null (không filter theo category)
     */
    private Long resolveCategoryId(String topLevelCategory, String secondLevelCategory) {
        // Ưu tiên secondLevelCategory nếu có
        if (secondLevelCategory != null && !secondLevelCategory.trim().isEmpty()) {
            return categoryRepository.findByName(secondLevelCategory.trim())
                    .filter(cat -> cat.getLevel() == 2) // Đảm bảo là level 2
                    .map(Category::getId)
                    .orElseGet(() -> {
                        log.warn("Second-level category '{}' not found", secondLevelCategory);
                        return null;
                    });
        }

        // Nếu chỉ có topLevelCategory
        if (topLevelCategory != null && !topLevelCategory.trim().isEmpty()) {
            Category topCategory = categoryRepository.findByName(topLevelCategory.trim())
                    .filter(cat -> cat.getLevel() == 1)
                    .orElse(null);

            if (topCategory == null) {
                log.warn("Top-level category '{}' not found", topLevelCategory);
                return null;
            }

            // Nếu là category level 1, trả về tất cả sản phẩm thuộc category đó và các sub-categories
            // Để làm điều này, bạn cần modify query hoặc lấy tất cả sub-category IDs
            // Hiện tại, tạm trả về ID của category level 1
            return topCategory.getId();
        }

        return null; // Không filter theo category
    }

    public ProductDetailDTO getProductDetail(Long productId) {
        log.info("Fetching detail for product ID: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResolutionException("Product not found with id: " + productId));

        ProductDetailDTO dto = new ProductDetailDTO();

        dto.setId(product.getId());
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setBrand(product.getBrand());
        dto.setDescription(product.getDescription());
        dto.setDetailedReview(product.getDetailedReview());
        dto.setPowerfulPerformance(product.getPowerfulPerformance());

        // Specs
        dto.setColor(product.getColor());
        dto.setWeight(product.getWeight());
        dto.setDimension(product.getDimension());
        dto.setBatteryType(product.getBatteryType());
        dto.setBatteryCapacity(product.getBatteryCapacity());
        dto.setRamCapacity(product.getRamCapacity());
        dto.setRomCapacity(product.getRomCapacity());
        dto.setScreenSize(product.getScreenSize());
        dto.setConnectionPort(product.getConnectionPort());

        dto.setImageUrls(product.getImages().stream()
                .map(Image::getDownloadUrl)
                .collect(Collectors.toList()));

        if(product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        List<Inventory> inventories = inventoryRepository.findAllByProductId(productId);
        dto.setPriceVariants(inventories.stream()
                .map(inv -> new ProductDetailDTO.PriceVariantDTO(
                        inv.getId(),
                        inv.getSize(),
                        inv.getPrice(),
                        inv.getDiscountPercent(),
                        inv.getDiscountedPrice(),
                        inv.getQuantity(),
                        inv.isInStock()
                ))
                .collect(Collectors.toList()));

        dto.setAverageRating(product.getAverageRating());
        dto.setNumRatings(product.getNumRatings());
        dto.setQuantitySold(product.getQuantitySold());
        dto.setIsActive(product.getIsActive());

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
            dto.setImageUrls(product.getImages().stream().map(Image::getDownloadUrl).collect(Collectors.toList()));
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

    @Transactional
    public List<Product> createBulkProducts(List<BulkProductRequest.ProductItemDTO> productItems) {
        List<Product> createdProducts = new ArrayList<>();

        for (BulkProductRequest.ProductItemDTO item : productItems) {
            try {
                Product product = createProductFromDTO(item);
                createdProducts.add(product);
                log.info("✅ Successfully created product: {} (ID: {})", product.getTitle(), product.getId());
            } catch (Exception e) {
                log.error("❌ Failed to create product '{}'. Error: {}", item.getTitle(), e.getMessage(), e);
            }
        }

        log.info("📦 Bulk import completed: {}/{} products created successfully",
                createdProducts.size(), productItems.size());

        return createdProducts;
    }

    private Product createProductFromDTO(BulkProductRequest.ProductItemDTO dto) {
        Category category = getOrCreateCategory(dto.getTopLevelCategory(), dto.getSecondLevelCategory());

        Product product = new Product();
        product.setTitle(dto.getTitle());
        product.setBrand(dto.getBrand());
        product.setColor(dto.getColor());
        product.setWeight(dto.getWeight());
        product.setDimension(dto.getDimension());
        product.setBatteryType(dto.getBatteryType());
        product.setBatteryCapacity(dto.getBatteryCapacity());
        product.setRamCapacity(dto.getRamCapacity());
        product.setRomCapacity(dto.getRomCapacity());
        product.setScreenSize(dto.getScreenSize());
        product.setDetailedReview(dto.getDetailedReview());
        product.setPowerfulPerformance(dto.getPowerfulPerformance());
        product.setConnectionPort(dto.getConnectionPort());
        product.setDescription(dto.getDescription());
        product.setCategory(category);
        product.setIsActive(true);
        product.setNumRatings(0);
        product.setAverageRating(0.0);
        product.setQuantitySold(0L);
        product.setWarningCount(0);

        Product savedProduct = productRepository.save(product);

        if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
            for (BulkProductRequest.ImageUrlDTO imgDto : dto.getImageUrls()) {
                Image image = new Image();
                image.setFileName(imgDto.getFileName());
                image.setFileType(imgDto.getFileType());
                image.setDownloadUrl(imgDto.getDownloadUrl());
                image.setProduct(savedProduct);
                imageRepository.save(image);
            }
        }

        if (dto.getVariants() != null && !dto.getVariants().isEmpty()) {
            for (BulkProductRequest.InventoryItemDTO variantDto : dto.getVariants()) {
                Inventory inventory = new Inventory();
                inventory.setProduct(savedProduct);
                inventory.setSize(variantDto.getSize());
                inventory.setQuantity(variantDto.getQuantity());
                inventory.setPrice(variantDto.getPrice());
                inventory.setDiscountPercent(variantDto.getDiscountPercent() != null ? variantDto.getDiscountPercent() : 0);
                inventoryRepository.save(inventory);
            }
        }

        return savedProduct;
    }

    private Category getOrCreateCategory(String topLevelName, String secondLevelName) {
        Category parentCategory = categoryRepository.findByName(topLevelName)
                .orElseGet(() -> {
                    Category newParent = new Category();
                    newParent.setName(topLevelName);
                    newParent.setLevel(1);
                    newParent.setIsParent(true);
                    Category saved = categoryRepository.save(newParent);
                    log.info("Created new parent category: {}", topLevelName);
                    return saved;
                });

        Category childCategory = categoryRepository.findByName(secondLevelName)
                .orElseGet(() -> {
                    Category newChild = new Category();
                    newChild.setName(secondLevelName);
                    newChild.setLevel(2);
                    newChild.setIsParent(false);
                    newChild.setParentCategory(parentCategory);
                    Category saved = categoryRepository.save(newChild);
                    log.info("Created new child category: {} under {}", secondLevelName, topLevelName);
                    return saved;
                });

        return childCategory;
    }
}