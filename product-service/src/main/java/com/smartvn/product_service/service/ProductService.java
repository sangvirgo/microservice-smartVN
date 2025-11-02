package com.smartvn.product_service.service;

import com.smartvn.product_service.client.OrderServiceClient;
import com.smartvn.product_service.dto.InventoryCheckRequest;
import com.smartvn.product_service.dto.ProductDetailDTO;
import com.smartvn.product_service.dto.ProductListingDTO;
import com.smartvn.product_service.dto.admin.CreateProductRequest;
import com.smartvn.product_service.dto.admin.UpdateProductRequest;
import com.smartvn.product_service.exceptions.AppException;
import com.smartvn.product_service.model.*;
import com.smartvn.product_service.repository.CategoryRepository;
import com.smartvn.product_service.repository.ImageRepository;
import com.smartvn.product_service.repository.InventoryRepository;
import com.smartvn.product_service.repository.ProductRepository;
import com.smartvn.product_service.specification.ProductSpecification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.module.ResolutionException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
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
    private final OrderServiceClient orderServiceClient;

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

    @Transactional
    public BulkImportResult createBulkProductsOptimized(
            List<CreateProductRequest> requests) {

        BulkImportResult result = new BulkImportResult();

//        Map<Long, Category> categoryMap = categoryRepository
//                .findAllById(categoryIds)
//                .stream()
//                .collect(Collectors.toMap(Category::getId, c -> c));

        // 2. Prepare products to save
        List<Product> productsToSave = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            CreateProductRequest req = requests.get(i);

            try {
                Category category = resolveCategoryFromRequest(req);
                // Validate
                boolean exists = productRepository.findAll().stream()
                        .anyMatch(p ->
                                p.getTitle().equalsIgnoreCase(req.getTitle()) &&
                                        p.getBrand().equalsIgnoreCase(req.getBrand())
                        );

                if (exists) {
                    result.addFailure(i, req.getTitle(), "Product already exists");
                    continue;
                }

                Product product = buildProductFromRequest(req, category);
                result.getSuccessProducts().add(product);

            } catch (Exception e) {
                result.addFailure(i, req.getTitle(), e.getMessage());
            }
        }

        // ✅ BATCH INSERT
        List<Product> saved = productRepository.saveAll(result.getSuccessProducts());
        result.getSuccessProducts().clear();
        result.getSuccessProducts().addAll(saved);

        // Create inventories & images
        for (Product product : saved) {
            CreateProductRequest req = findRequestForProduct(requests, product);
            createInventoriesForProduct(product, req.getVariants());
            createImagesForProduct(product, req.getImageUrls());
        }

        return result;
    }

    @Data
    public static class BulkImportResult {
        private List<Product> successProducts = new ArrayList<>();
        private List<FailureRecord> failures = new ArrayList<>();

        public void addSuccess(Product p) { successProducts.add(p); }
        public void addFailure(int index, String title, String error) {
            failures.add(new FailureRecord(index, title, error));
        }

        @Data
        @AllArgsConstructor
        public static class FailureRecord {
            private int index;
            private String productTitle;
            private String errorMessage;
        }
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
    public Product createSingleProduct(CreateProductRequest request) {
        log.info("📦 Creating single product: {}", request.getTitle());

        Category category = resolveCategoryFromRequest(request);

        // ✅ 2. CHECK DUPLICATE (case-insensitive)
        boolean exists = productRepository.findAll().stream()
                .anyMatch(p ->
                        p.getTitle().equalsIgnoreCase(request.getTitle()) &&
                                p.getBrand().equalsIgnoreCase(request.getBrand())
                );

        if (exists) {
            throw new AppException(
                    "Product with same title and brand already exists",
                    HttpStatus.CONFLICT
            );
        }

        // 2. VALIDATE VARIANTS
        if (request.getVariants() == null || request.getVariants().isEmpty()) {
            throw new AppException(
                    "Product must have at least one variant",
                    HttpStatus.BAD_REQUEST
            );
        }

        // 3. CHECK DUPLICATE SIZE
        List<String> sizes = request.getVariants().stream()
                .map(CreateProductRequest.CreateInventoryDTO::getSize)
                .toList();

        if (sizes.size() != sizes.stream().distinct().count()) {
            throw new AppException(
                    "Duplicate variant sizes found",
                    HttpStatus.BAD_REQUEST
            );
        }

        // 4. CREATE PRODUCT
        Product product = new Product();
        product.setTitle(request.getTitle());
        product.setBrand(request.getBrand());
        product.setDescription(request.getDescription());
        product.setCategory(category);

        // Specifications
        product.setColor(request.getColor());
        product.setWeight(request.getWeight());
        product.setDimension(request.getDimension());
        product.setBatteryType(request.getBatteryType());
        product.setBatteryCapacity(request.getBatteryCapacity());
        product.setRamCapacity(request.getRamCapacity());
        product.setRomCapacity(request.getRomCapacity());
        product.setScreenSize(request.getScreenSize());
        product.setConnectionPort(request.getConnectionPort());
        product.setDetailedReview(request.getDetailedReview());
        product.setPowerfulPerformance(request.getPowerfulPerformance());

        // Status
        product.setIsActive(true);
        product.setNumRatings(0);
        product.setAverageRating(0.0);
        product.setQuantitySold(0L);
        product.setWarningCount(0);
        product.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);
        log.info("✅ Product created with ID: {}", savedProduct.getId());

        // 5. CREATE VARIANTS
        createInventoriesForProduct(savedProduct, request.getVariants());

        // 6. CREATE IMAGES (if provided)
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            createImagesForProduct(savedProduct, request.getImageUrls());
        }

        return savedProduct;
    }

    /**
     * ✅ TẠO INVENTORY CHO PRODUCT
     */
    private void createInventoriesForProduct(
            Product product,
            List<CreateProductRequest.CreateInventoryDTO> variants
    ) {
        for (CreateProductRequest.CreateInventoryDTO variantDto : variants) {
            Inventory inventory = new Inventory();
            product.setUpdatedAt(LocalDateTime.now());
            inventory.setProduct(product);
            inventory.setSize(variantDto.getSize());
            inventory.setQuantity(variantDto.getQuantity());
            inventory.setPrice(variantDto.getPrice());

            int discount = variantDto.getDiscountPercent() != null
                    ? variantDto.getDiscountPercent()
                    : 0;
            inventory.setDiscountPercent(discount);
            inventory.setUpdatedAt(LocalDateTime.now());

            // ✅ TÍNH TOÁN DISCOUNTED PRICE
            inventory.calculateDiscountedPrice();

            inventoryRepository.save(inventory);
            log.debug("  ✓ Created variant: {} - {}đ ({}% off)",
                    variantDto.getSize(),
                    variantDto.getPrice(),
                    discount
            );
        }
    }

    /**
     * ✅ TẠO IMAGES CHO PRODUCT
     */
    private void createImagesForProduct(
            Product product,
            List<CreateProductRequest.ImageUrlDTO> imageUrls
    ) {
        for (CreateProductRequest.ImageUrlDTO imageDto : imageUrls) {
            Image image = new Image();
            image.setProduct(product);
            image.setDownloadUrl(imageDto.getDownloadUrl());
            image.setFileName(imageDto.getFileName());
            image.setFileType(imageDto.getFileType());

            imageRepository.save(image);
            log.debug("  ✓ Added image: {}", imageDto.getDownloadUrl());
        }
    }

    /**
     * ✅ CẬP NHẬT SẢN PHẨM (Admin)
     */
    @Transactional
    public Product updateProduct(Long productId, UpdateProductRequest request) {
        log.info("📝 Updating product ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(
                        "Product not found",
                        HttpStatus.NOT_FOUND
                ));

        // Update basic info
        if (request.getTitle() != null) {
            product.setTitle(request.getTitle());
        }
        if (request.getBrand() != null) {
            product.setBrand(request.getBrand());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }

        // Update specifications
        if (request.getColor() != null) product.setColor(request.getColor());
        if (request.getWeight() != null) product.setWeight(request.getWeight());
        if (request.getDimension() != null) product.setDimension(request.getDimension());
        if (request.getBatteryType() != null) product.setBatteryType(request.getBatteryType());
        if (request.getBatteryCapacity() != null) product.setBatteryCapacity(request.getBatteryCapacity());
        if (request.getRamCapacity() != null) product.setRamCapacity(request.getRamCapacity());
        if (request.getRomCapacity() != null) product.setRomCapacity(request.getRomCapacity());
        if (request.getScreenSize() != null) product.setScreenSize(request.getScreenSize());
        if (request.getConnectionPort() != null) product.setConnectionPort(request.getConnectionPort());
        if (request.getDetailedReview() != null) product.setDetailedReview(request.getDetailedReview());
        if (request.getPowerfulPerformance() != null) product.setPowerfulPerformance(request.getPowerfulPerformance());
        product.setUpdatedAt(LocalDateTime.now());

        Product updated = productRepository.save(product);
        log.info("✅ Product updated: {}", updated.getTitle());

        return updated;
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

    @Transactional
    public void toggleProductActive(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException("Product not found", HttpStatus.NOT_FOUND));
        product.setIsActive(!product.getIsActive());
        productRepository.save(product);
    }

    @Transactional
    public void softDeleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException("Product not found", HttpStatus.NOT_FOUND));
        product.setIsActive(false);
        productRepository.save(product);
    }

    public Page<Product> searchProductsForAdmin(
            String search,
            Long categoryId,
            Boolean isActive,
            Pageable pageable) {

        Specification<Product> spec = Specification.where(null);

        if (search != null) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%"));
        }

        // ✅ THAY ĐỔI DUY NHẤT Ở ĐÂY
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new AppException("Category not found", HttpStatus.NOT_FOUND));

            if (category.getLevel() == 1) {
                // Parent category → search trong tất cả subcategories
                List<Long> subCategoryIds = category.getSubCategories().stream()
                        .map(Category::getId)
                        .collect(Collectors.toList());

                spec = spec.and((root, query, cb) ->
                        root.get("category").get("id").in(subCategoryIds));
            } else {
                // Child category → search bình thường
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("category").get("id"), categoryId));
            }
        }

        if (isActive != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("isActive"), isActive));
        }

        return productRepository.findAll(spec, pageable);
    }

    @Transactional
    public void increaseQuantitySold(InventoryCheckRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException("Product not found", HttpStatus.NOT_FOUND));

        product.setQuantitySold(product.getQuantitySold() + request.getQuantity());
        productRepository.save(product);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        "Product not found",
                        HttpStatus.NOT_FOUND
                ));
    }

    private CreateProductRequest findRequestForProduct(
            List<CreateProductRequest> requests,
            Product product) {

        return requests.stream()
                .filter(req -> req.getTitle().equals(product.getTitle())
                        && req.getBrand().equals(product.getBrand()))
                .findFirst()
                .orElse(null);
    }


    private Product buildProductFromRequest(
            CreateProductRequest req,
            Category category) {

        if (req.getVariants() == null || req.getVariants().isEmpty()) {
            throw new AppException(
                    "Product must have at least one variant: " + req.getTitle(),
                    HttpStatus.BAD_REQUEST
            );
        }

        Product product = new Product();
        product.setTitle(req.getTitle());
        product.setBrand(req.getBrand());
        product.setDescription(req.getDescription());
        product.setCategory(category);

        // Specs
        product.setColor(req.getColor());
        product.setWeight(req.getWeight());
        product.setDimension(req.getDimension());
        product.setBatteryType(req.getBatteryType());
        product.setBatteryCapacity(req.getBatteryCapacity());
        product.setRamCapacity(req.getRamCapacity());
        product.setRomCapacity(req.getRomCapacity());
        product.setScreenSize(req.getScreenSize());
        product.setConnectionPort(req.getConnectionPort());
        product.setDetailedReview(req.getDetailedReview());
        product.setPowerfulPerformance(req.getPowerfulPerformance());

        // Defaults
        product.setIsActive(true);
        product.setNumRatings(0);
        product.setAverageRating(0.0);
        product.setQuantitySold(0L);
        product.setWarningCount(0);

        return product;
    }

    private Category getOrCreateCategoryIgnoreCase(String topLevelName, String secondLevelName) {
        // ✅ THÊM synchronized hoặc dùng findOrCreate atomic
        Category parentCategory;

        synchronized (this) {  // Simple lock
            parentCategory = categoryRepository
                    .findByNameAndLevelIgnoreCase(topLevelName, 1)
                    .orElseGet(() -> {
                        Category newParent = new Category();
                        newParent.setName(capitalize(topLevelName));
                        newParent.setLevel(1);
                        newParent.setIsParent(true);
                        return categoryRepository.save(newParent);
                    });
        }

        Category childCategory;
        synchronized (this) {
            childCategory = categoryRepository
                    .findByNameAndLevelIgnoreCase(secondLevelName, 2)
                    .filter(c -> c.getParentCategory() != null &&
                            c.getParentCategory().getId().equals(parentCategory.getId()))
                    .orElseGet(() -> {
                        Category newChild = new Category();
                        newChild.setName(capitalize(secondLevelName));
                        newChild.setLevel(2);
                        newChild.setIsParent(false);
                        newChild.setParentCategory(parentCategory);
                        return categoryRepository.save(newChild);
                    });
        }

        return childCategory;
    }

    /**
     * ✅ CAPITALIZE tên category (Google Pixel → Google Pixel)
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Arrays.stream(str.trim().split("\\s+"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    private Category resolveCategoryFromRequest(CreateProductRequest request) {
        // Option 1: Có categoryId → dùng trực tiếp
        if (request.getCategoryId() != null) {
            return categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(
                            "Category not found with id: " + request.getCategoryId(),
                            HttpStatus.NOT_FOUND
                    ));
        }

        // Option 2: Có category names → tìm/tạo
        if (request.getTopLevelCategory() != null &&
                request.getSecondLevelCategory() != null) {

            return getOrCreateCategoryIgnoreCase(
                    request.getTopLevelCategory().trim(),
                    request.getSecondLevelCategory().trim()
            );
        }

        // Không có thông tin category
        throw new AppException(
                "Either categoryId or (topLevelCategory + secondLevelCategory) must be provided",
                HttpStatus.BAD_REQUEST
        );
    }

    public boolean hasUserPurchasedProduct(Long userId, Long productId) {
        try {
            Boolean result = orderServiceClient.hasUserPurchasedProduct(userId, productId);
            return result != null && result;
        } catch (Exception e) {
            log.error("Failed to check purchase status for user {} and product {}", userId, productId, e);
            return false;
        }
    }
}