package com.smartvn.product_service.service;

import com.smartvn.product_service.client.InventoryServiceClient;
import com.smartvn.product_service.dto.ProductDetailDTO;
import com.smartvn.product_service.dto.ProductListingDTO;
import com.smartvn.product_service.model.Product;
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
    private final InventoryServiceClient inventoryClient;

    // ============================================
    // 1. LẤY DANH SÁCH SẢN PHẨM (SỬ DỤNG CACHE)
    // ============================================

    /**
     * Lấy danh sách sản phẩm phân trang cho trang chủ hoặc trang danh mục.
     * Tốc độ nhanh do chỉ truy vấn từ bảng Product đã được cache sẵn thông tin giá và tồn kho.
     *
     * @param pageable Thông tin phân trang.
     * @return Một trang (Page) chứa các ProductListingDTO.
     */
    public Page<ProductListingDTO> getProducts(Pageable pageable) {
        log.info("Fetching product listing for page: {}", pageable.getPageNumber());
        return productRepository.findAllByIsActiveTrue(pageable)
                .map(this::toListingDTO);
    }

    /**
     * Tìm kiếm và lọc sản phẩm dựa trên nhiều tiêu chí.
     * Vẫn sử dụng các trường đã cache để đảm bảo hiệu năng.
     *
     * @param keyword    Từ khóa tìm kiếm (tiêu đề).
     * @param categoryId ID của danh mục.
     * @param minPrice   Giá tối thiểu.
     * @param maxPrice   Giá tối đa.
     * @param pageable   Thông tin phân trang.
     * @return Một trang (Page) chứa các ProductListingDTO phù hợp.
     */
    public Page<ProductListingDTO> searchProducts(
            String keyword,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {
        log.info("Searching products with keyword: {}, categoryId: {}, minPrice: {}, maxPrice: {}",
                keyword, categoryId, minPrice, maxPrice);
        // Giả sử productRepository có phương thức searchProducts đã được tối ưu
        return productRepository.searchProducts(
                keyword, categoryId, minPrice, maxPrice, pageable
        ).map(this::toListingDTO);
    }

    // ============================================
    // 2. LẤY CHI TIẾT SẢN PHẨM (GỌI INVENTORY SERVICE)
    // ============================================

    /**
     * Lấy thông tin chi tiết đầy đủ của một sản phẩm.
     * Sẽ thực hiện một cuộc gọi API đến Inventory Service để lấy dữ liệu giá và tồn kho real-time.
     *
     * @param productId ID của sản phẩm.
     * @param userLat   Vĩ độ của người dùng (để tìm cửa hàng gần nhất).
     * @param userLon   Kinh độ của người dùng.
     * @return ProductDetailDTO chứa toàn bộ thông tin.
     */
    public ProductDetailDTO getProductDetail(Long productId, Double userLat, Double userLon) {
        log.info("Fetching detail for product ID: {}", productId);
        Product product = productRepository.findByIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new RuntimeException("Product not found or is inactive")); // Thay bằng Exception cụ thể

        ProductDetailDTO dto = mapProductToDetailDTO(product);

        // --- GỌI INVENTORY SERVICE ---
        try {
            log.info("Calling Inventory Service to get price variants for product: {}", productId);
            List<ProductDetailDTO.PriceVariantDTO> variants =
                    inventoryClient.getProductPriceVariants(productId, userLat, userLon);
            dto.setPriceVariants(variants);

            // Tìm và set cửa hàng được đề xuất (gần nhất hoặc giá tốt nhất)
            variants.stream()
                    .filter(v -> v.getIsRecommended() != null && v.getIsRecommended())
                    .findFirst()
                    .ifPresent(v -> {
                        dto.setRecommendedStoreId(v.getStoreId());
                        dto.setRecommendedStoreName(v.getStoreName());
                    });

        } catch (Exception e) {
            log.error("Failed to fetch inventory data for product {}. Error: {}", productId, e.getMessage());
            // Fallback: Nếu Inventory Service lỗi, có thể không hiển thị giá hoặc hiển thị thông báo.
            // Ở đây ta trả về danh sách rỗng để UI có thể xử lý.
            dto.setPriceVariants(List.of());
            // Cân nhắc thêm một trường vào DTO để báo hiệu cho UI biết dữ liệu giá không có sẵn
        }

        // Lấy thông tin reviews (logic này có thể nằm trong ReviewService)
        // dto.setRecentReviews(...)

        return dto;
    }

    // ============================================
    // 3. ĐỒNG BỘ DỮ LIỆU TỪ INVENTORY SERVICE
    // ============================================

    /**
     * Đồng bộ hóa thông tin giá và tồn kho từ Inventory Service vào bảng Product.
     * Phương thức này nên được gọi bởi một Scheduled Job hoặc một Event Listener
     * khi có sự thay đổi về giá/tồn kho trong Inventory Service.
     *
     * @param productId ID của sản phẩm cần đồng bộ.
     */
    @Transactional
    public void syncPriceCache(Long productId) {
        log.info("Starting price cache sync for product ID: {}", productId);
        try {
            // Gọi Inventory Service để lấy tất cả các bản ghi tồn kho của sản phẩm
            List<Product.InventoryInfo> inventories = inventoryClient.getAllInventoriesByProduct(productId);

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found for sync: " + productId));

            if (inventories.isEmpty()) {
                // Nếu không có tồn kho, reset thông tin
                product.setMinPrice(null);
                product.setMaxPrice(null);
                product.setMinDiscountedPrice(null);
                product.setMaxDiscountedPrice(null);
                product.setHasDiscount(false);
                product.setMaxDiscountPercent(0);
                product.setTotalStock(0);
                productRepository.save(product);
                log.warn("No inventory found for product ID: {}. Price cache has been reset.", productId);
                return;
            }

            // Tính toán các giá trị min/max từ danh sách tồn kho
            BigDecimal minOriginalPrice = inventories.stream().map(Product.InventoryInfo::getPrice).min(BigDecimal::compareTo).orElse(null);
            BigDecimal maxOriginalPrice = inventories.stream().map(Product.InventoryInfo::getPrice).max(BigDecimal::compareTo).orElse(null);

            BigDecimal minDiscountedPrice = inventories.stream().map(inv -> inv.getDiscountedPrice() != null ? inv.getDiscountedPrice() : inv.getPrice()).min(BigDecimal::compareTo).orElse(null);
            BigDecimal maxDiscountedPrice = inventories.stream().map(inv -> inv.getDiscountedPrice() != null ? inv.getDiscountedPrice() : inv.getPrice()).max(BigDecimal::compareTo).orElse(null);

            Integer maxDiscount = inventories.stream().map(Product.InventoryInfo::getDiscountPercent).filter(d -> d != null && d > 0).max(Integer::compareTo).orElse(0);
            Integer totalStock = inventories.stream().mapToInt(Product.InventoryInfo::getQuantity).sum();

            // Cập nhật các trường cache trong Product
            product.setMinPrice(minOriginalPrice);
            product.setMaxPrice(maxOriginalPrice);
            product.setMinDiscountedPrice(minDiscountedPrice);
            product.setMaxDiscountedPrice(maxDiscountedPrice);
            product.setHasDiscount(maxDiscount > 0);
            product.setMaxDiscountPercent(maxDiscount);
            product.setTotalStock(totalStock);

            productRepository.save(product);
            log.info("Successfully synced price cache for product ID: {}", productId);

        } catch (Exception e) {
            log.error("Failed to sync price cache for product ID: {}. Error: {}", productId, e.getMessage(), e);
            // Có thể thêm logic retry hoặc gửi thông báo lỗi
        }
    }


    // ============================================
    // HELPER METHODS (PRIVATE)
    // ============================================

    /**
     * Chuyển đổi từ Product Entity sang ProductListingDTO.
     * Sử dụng dữ liệu cache.
     */
    private ProductListingDTO toListingDTO(Product product) {
        ProductListingDTO dto = new ProductListingDTO();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setBrand(product.getBrand());
        dto.setAverageRating(product.getAverageRating());
        dto.setNumRatings(product.getNumRatings());
        dto.setQuantitySold(product.getQuantitySold());

        // Lấy hình ảnh thumbnail (ảnh đầu tiên)
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            dto.setThumbnailUrl(product.getImages().get(0).getDownloadUrl());
        }

        // Sử dụng các trường giá đã cache
        dto.setMinPrice(product.getMinPrice());
        dto.setMaxPrice(product.getMaxPrice());
        dto.setMinDiscountedPrice(product.getMinDiscountedPrice());
        dto.setMaxDiscountedPrice(product.getMaxDiscountedPrice());
        dto.setHasDiscount(product.getHasDiscount());
        dto.setMaxDiscountPercent(product.getMaxDiscountPercent());

        // Format chuỗi hiển thị khoảng giá
        dto.setPriceRange(formatPriceRange(product.getMinPrice(), product.getMaxPrice()));
        if (product.getHasDiscount()) {
            dto.setDiscountedPriceRange(formatPriceRange(product.getMinDiscountedPrice(), product.getMaxDiscountedPrice()));
        }

        // Tình trạng tồn kho
        dto.setInStock(product.getTotalStock() > 0);

        return dto;
    }

    /**
     * Chuyển đổi từ Product Entity sang ProductDetailDTO (chỉ thông tin cơ bản).
     * Phần giá sẽ được điền sau khi gọi Inventory Service.
     */
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
        dto.setQuantitySold(product.getQuantitySold());
        dto.setIsActive(product.getIsActive());
        dto.setAverageRating(product.getAverageRating());
        dto.setNumRatings(product.getNumRatings());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        if (product.getImages() != null) {
            dto.setImageUrls(product.getImages().stream()
                    .map(img -> img.getDownloadUrl())
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    /**
     * Định dạng chuỗi hiển thị khoảng giá.
     */
    private String formatPriceRange(BigDecimal min, BigDecimal max) {
        if (min == null) {
            return "Liên hệ";
        }
        if (min.compareTo(max) == 0) {
            return String.format("%,.0fđ", min);
        }
        return String.format("%,.0fđ - %,.0fđ", min, max);
    }
}