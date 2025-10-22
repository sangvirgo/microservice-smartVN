package com.smartvn.product_service.service;

import com.smartvn.product_service.dto.BulkProductRequest;
import com.smartvn.product_service.dto.ProductDetailDTO;
import com.smartvn.product_service.dto.ProductListingDTO;
import com.smartvn.product_service.model.*;
import com.smartvn.product_service.repository.CategoryRepository;
import com.smartvn.product_service.repository.ImageRepository;
import com.smartvn.product_service.repository.InventoryRepository;
import com.smartvn.product_service.repository.ProductRepository;
import com.smartvn.product_service.specification.ProductSpecification;
import jakarta.persistence.criteria.Join;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.module.ResolutionException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
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
        log.info("🔍 Searching - keyword: {}, topLevel: {}, secondLevel: {}, price: {}-{}",
                keyword, topLevelCategory, secondLevelCategory, minPrice, maxPrice);

        // 1. Resolve category IDs
        List<Long> categoryIds = resolveCategoryIds(topLevelCategory, secondLevelCategory);

        // 2. Nếu có filter category nhưng không tìm thấy -> trả về empty
        boolean hasCategoryFilter = (topLevelCategory != null && !topLevelCategory.trim().isEmpty())
                || (secondLevelCategory != null && !secondLevelCategory.trim().isEmpty());

        if (hasCategoryFilter && (categoryIds == null || categoryIds.isEmpty())) {
            log.warn("⚠️ Category not found");
            return Page.empty(pageable);
        }

        // 3. ✅ SỬ DỤNG SPECIFICATION để query (bao gồm cả price filter)
        Specification<Product> spec = ProductSpecification.searchProducts(
                keyword,
                categoryIds.isEmpty() ? null : categoryIds,
                minPrice,
                maxPrice
        );

        Page<Product> productPage = productRepository.findAll(spec, pageable);

        // 4. ✅ Chỉ cần map sang DTO, không cần filter nữa
        return productPage.map(this::toListingDTO);
    }
    /**
     * Resolve categoryId từ tên category
     * Logic:
     * 1. Nếu có secondLevelCategory -> tìm category cấp 2 với tên đó
     * 2. Nếu chỉ có topLevelCategory -> tìm category cấp 1 với tên đó
     * 3. Nếu không có cả hai -> return null (không filter theo category)
     */
    private List<Long> resolveCategoryIds(String topLevelCategory, String secondLevelCategory) {

        // Case 1: Có secondLevel → chỉ lấy category cấp 2 đó
        if (secondLevelCategory != null && !secondLevelCategory.trim().isEmpty()) {
            log.debug("🔎 Looking for second level category: {}", secondLevelCategory);
            return categoryRepository.findByName(secondLevelCategory.trim())
                    .filter(cat -> cat.getLevel() == 2)
                    .map(cat -> {
                        log.info("✅ Found category ID: {} ({})", cat.getId(), cat.getName());
                        return Collections.singletonList(cat.getId());
                    })
                    .orElseGet(() -> {
                        log.warn("⚠️ Second level category '{}' not found", secondLevelCategory);
                        return Collections.emptyList();
                    });
        }

        // Case 2: Chỉ có topLevel → lấy TẤT CẢ category cấp 2 là con của nó
        if (topLevelCategory != null && !topLevelCategory.trim().isEmpty()) {
            log.debug("🔎 Looking for top level category: {}", topLevelCategory);
            return categoryRepository.findByName(topLevelCategory.trim())
                    .filter(cat -> cat.getLevel() == 1)
                    .map(parent -> {
                        // ✅ BỎ KIỂM TRA EMPTY - NẾU KHÔNG CÓ CHILDREN THÌ TÌM TRONG CHÍNH PARENT
                        List<Long> childIds = parent.getSubCategories() != null && !parent.getSubCategories().isEmpty()
                                ? parent.getSubCategories().stream()
                                .map(Category::getId)
                                .collect(Collectors.toList())
                                : Collections.emptyList();

                        // ✅ NẾU KHÔNG CÓ CHILDREN, DÙNG CHÍNH PARENT ID
                        if (childIds.isEmpty()) {
                            log.info("ℹ️ No sub-categories for '{}', using parent ID: {}",
                                    topLevelCategory, parent.getId());
                            return Collections.singletonList(parent.getId());
                        }

                        log.info("✅ Found {} sub-categories for '{}': {}",
                                childIds.size(), topLevelCategory, childIds);
                        return childIds;
                    })
                    .orElseGet(() -> {
                        log.warn("⚠️ Top level category '{}' not found", topLevelCategory);
                        return Collections.emptyList();
                    });
        }

        // Case 3: Không filter category
        log.debug("ℹ️ No category filter applied");
        return Collections.emptyList();
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
        List<Inventory> inventories = product.getInventories();
        if (!inventories.isEmpty()) {
            BigDecimal minPrice = inventories.stream().map(Inventory::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal maxPrice = inventories.stream().map(Inventory::getPrice).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal minDiscountedPrice = inventories.stream().map(Inventory::getDiscountedPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal maxDiscountedPrice = inventories.stream().map(Inventory::getDiscountedPrice).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            int totalStock = inventories.stream().mapToInt(Inventory::getQuantity).sum();

            dto.setPriceRange(formatPriceRange(minPrice, maxPrice));
            dto.setDiscountedPriceRange(formatPriceRange(minDiscountedPrice, maxDiscountedPrice));
            dto.setInStock(totalStock > 0);

            boolean hasAnyDiscount = inventories.stream()
                    .anyMatch(inv -> inv.getDiscountPercent() != null && inv.getDiscountPercent() > 0);
            dto.setHasDiscount(hasAnyDiscount);

            dto.setVariantCount(inventories.size());

            List<String> badges = new ArrayList<>();
            if (product.getQuantitySold() != null && product.getQuantitySold() > 50) {
                badges.add("Bán chạy");
            }
            if (hasAnyDiscount) {
                badges.add("Giảm giá");
            }
            if (product.getAverageRating() != null && product.getAverageRating() >= 4.5) {
                badges.add("Đánh giá cao");
            }
            dto.setBadges(badges);
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