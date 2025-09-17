package com.webanhang.team_project.service.seller;

import com.webanhang.team_project.dto.product.CreateProductRequest;
import com.webanhang.team_project.dto.product.FilterProduct;
import com.webanhang.team_project.dto.product.ProductDTO;
import com.webanhang.team_project.dto.product.UpdateProductRequest;
import com.webanhang.team_project.enums.OrderStatus;
import com.webanhang.team_project.enums.PaymentStatus;
import com.webanhang.team_project.model.*;
import com.webanhang.team_project.repository.*;
import com.webanhang.team_project.service.cart.CartService;
import com.webanhang.team_project.service.image.ImageService;
import com.webanhang.team_project.service.product.IProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerProductService implements ISellerProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final IProductService productService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ReviewRepository reviewRepository;
    private final ImageService imageService;

    @Override
    @Transactional
    public ProductDTO createProduct(CreateProductRequest req) {
        // Process two-level categories (giữ nguyên code category)
        Category parentCategory = null;
        Category category = null;

        // Handle top level category
        if (req.getTopLevelCategory() != null && !req.getTopLevelCategory().isEmpty()) {
            parentCategory = categoryRepository.findByName(req.getTopLevelCategory());
            if (parentCategory == null) {
                parentCategory = new Category();
                parentCategory.setName(req.getTopLevelCategory());
                parentCategory.setLevel(1);
                parentCategory.setParent(true);
                parentCategory = categoryRepository.save(parentCategory);
            } else if (parentCategory.getLevel() != 1) {
                throw new IllegalArgumentException("Top level category must have level 1");
            }

            // Handle second level category if provided
            if (req.getSecondLevelCategory() != null && !req.getSecondLevelCategory().isEmpty()) {
                category = categoryRepository.findByName(req.getSecondLevelCategory());
                if (category == null) {
                    category = new Category();
                    category.setName(req.getSecondLevelCategory());
                    category.setLevel(2);
                    category.setParent(false);
                    category.setParentCategory(parentCategory);
                    category = categoryRepository.save(category);
                } else if (category.getLevel() != 2) {
                    throw new IllegalArgumentException("Second level category must have level 2");
                }
            } else {
                // If no second level, use top level
                category = parentCategory;
            }
        }

        // Create the product
        Product product = new Product();
        product.setTitle(req.getTitle());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setDiscountPersent(req.getDiscountPersent());
        product.setBrand(req.getBrand());
        product.setColor(req.getColor());
        product.setCreatedAt(LocalDateTime.now());
        product.setQuantity(req.getQuantity());

        // Initialize quantitySold to 0 for new products
        product.setQuantitySold(0L);

        // Set product specifications from request
        product.setWeight(req.getWeight());
        product.setDimension(req.getDimension());
        product.setBatteryType(req.getBatteryType());
        product.setBatteryCapacity(req.getBatteryCapacity());
        product.setRamCapacity(req.getRamCapacity());
        product.setRomCapacity(req.getRomCapacity());
        product.setScreenSize(req.getScreenSize());
        product.setDetailedReview(req.getDetailedReview());
        product.setPowerfulPerformance(req.getPowerfulPerformance());
        product.setConnectionPort(req.getConnectionPort());

        if (req.getSellerId() != null) {
            product.setSellerId(req.getSellerId());
        }

        product.updateDiscountedPrice();
        product.setCategory(category);

        // Handle sizes
        if (req.getSizes() != null) {
            for (ProductSize size : req.getSizes()) {
                size.setProduct(product);
            }
            product.setSizes(req.getSizes());
        }

        // Handle images
        if (req.getImageUrls() != null && !req.getImageUrls().isEmpty()) {
            for (Image imageUrl : req.getImageUrls()) {
                imageUrl.setProduct(product);
            }
            product.setImages(req.getImageUrls());
        }

        product = productRepository.save(product);
        return new ProductDTO(product);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long productId, UpdateProductRequest request) { // Change parameter type
        Product existingProduct = productRepository.getProductById(productId);

        // Update basic properties
        if (request.getTitle() != null) {
            existingProduct.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            existingProduct.setDescription(request.getDescription());
        }
        if (request.getBrand() != null) {
            existingProduct.setBrand(request.getBrand());
        }
        if (request.getColor() != null) {
            existingProduct.setColor(request.getColor());
        }

        // Handle category update using the same logic as createProduct
        if (request.getTopLevelCategory() != null && !request.getTopLevelCategory().isEmpty()) {
            Category parentCategory = null;
            Category category = null;

            // Find or create top level category
            parentCategory = categoryRepository.findByName(request.getTopLevelCategory());
            if (parentCategory == null) {
                parentCategory = new Category();
                parentCategory.setName(request.getTopLevelCategory());
                parentCategory.setLevel(1);
                parentCategory.setParent(true);
                parentCategory = categoryRepository.save(parentCategory);
            } else if (parentCategory.getLevel() != 1) {
                throw new IllegalArgumentException("Top level category must have level 1");
            }

            // Handle second level category if provided
            if (request.getSecondLevelCategory() != null && !request.getSecondLevelCategory().isEmpty()) {
                category = categoryRepository.findByName(request.getSecondLevelCategory());
                if (category == null) {
                    category = new Category();
                    category.setName(request.getSecondLevelCategory());
                    category.setLevel(2);
                    category.setParent(false);
                    category.setParentCategory(parentCategory);
                    category = categoryRepository.save(category);
                } else if (category.getLevel() != 2) {
                    throw new IllegalArgumentException("Second level category must have level 2");
                }
            } else {
                // If no second level, use top level
                category = parentCategory;
            }

            existingProduct.setCategory(category);
        }

        // Update specifications
        if (request.getWeight() != null) {
            existingProduct.setWeight(request.getWeight());
        }
        if (request.getDimension() != null) {
            existingProduct.setDimension(request.getDimension());
        }
        if (request.getBatteryType() != null) {
            existingProduct.setBatteryType(request.getBatteryType());
        }
        if (request.getBatteryCapacity() != null) {
            existingProduct.setBatteryCapacity(request.getBatteryCapacity());
        }
        if (request.getRamCapacity() != null) {
            existingProduct.setRamCapacity(request.getRamCapacity());
        }
        if (request.getRomCapacity() != null) {
            existingProduct.setRomCapacity(request.getRomCapacity());
        }
        if (request.getScreenSize() != null) {
            existingProduct.setScreenSize(request.getScreenSize());
        }
        if (request.getDetailedReview() != null) {
            existingProduct.setDetailedReview(request.getDetailedReview());
        }
        if (request.getPowerfulPerformance() != null) {
            existingProduct.setPowerfulPerformance(request.getPowerfulPerformance());
        }
        if (request.getConnectionPort() != null) {
            existingProduct.setConnectionPort(request.getConnectionPort());
        }

        // Update price and discount
        if (request.getPrice() != null && request.getPrice() > 0) {
            existingProduct.setPrice(request.getPrice());
        }
        if (request.getDiscountPersent() != null && request.getDiscountPersent() >= 0) {
            existingProduct.setDiscountPersent(request.getDiscountPersent());
        }
        existingProduct.updateDiscountedPrice();

        // Update quantity
        if (request.getQuantity() != null && request.getQuantity() >= 0) {
            existingProduct.setQuantity(request.getQuantity());
        }

        Product res = productRepository.save(existingProduct);
        return new ProductDTO(res);
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        // Check if product belongs to this seller
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        // Check if product is referenced in any orders
        List<OrderItem> orderItems = orderItemRepository.findByProductId(productId);
        if (!orderItems.isEmpty()) {
            throw new RuntimeException("Cannot delete product that has been ordered. Found in " +
                    orderItems.size() + " order(s)");
        }

        // Check if product is in any active carts
        List<CartItem> cartItems = cartItemRepository.findByProductId(productId);
        if (!cartItems.isEmpty()) {
            // Remove from carts first
            cartItemRepository.deleteAll(cartItems);
        }

        // Check if product has reviews
        List<Review> reviews = reviewRepository.findAllByProductId(productId);
        if (!reviews.isEmpty()) {
            // Delete reviews first
            reviewRepository.deleteAll(reviews);
        }

        // Delete product images from cloudinary and database
        imageService.deleteAllProductImages(productId);

        // Finally delete the product
        productRepository.delete(product);
    }


    @Override
    public Page<ProductDTO> getSellerProducts(Long sellerId, Pageable pageable) {
        Page<Product> productPage = productRepository.findBySellerIdWithPagination(sellerId, pageable);

        return productPage.map(ProductDTO::new);
    }

    @Override
    public ProductDTO getProductDetail(Long productId) {
        Product product = productService.findProductById(productId);
        return new ProductDTO(product);
    }

    @Override
    @Transactional
    public Map<String, Object> getProductStatOfSeller(Long sellerId) {
        List<Product> products = productRepository.findBySellerId(sellerId);

        Map<String, Object> stats = new HashMap<>();

        // Total products
        stats.put("totalProducts", products.size());

        // Total sold
        long totalSold = products.stream()
                .filter(p -> p.getQuantitySold() != null)
                .mapToLong(Product::getQuantitySold)
                .sum();
        stats.put("totalSold", totalSold);

        // Total revenue
        int totalRevenue = products.stream()
                .filter(p -> p.getQuantitySold() != null)
                .mapToInt(p -> p.getDiscountedPrice() * p.getQuantitySold().intValue())
                .sum();
        stats.put("totalRevenue", totalRevenue);

        // Best seller
        Product bestSeller = products.stream()
                .filter(p -> p.getQuantitySold() != null && p.getQuantitySold() > 0)
                .max((p1, p2) -> p1.getQuantitySold().compareTo(p2.getQuantitySold()))
                .orElse(null);

        if (bestSeller != null) {
            Map<String, Object> bestSellerInfo = new HashMap<>();
            bestSellerInfo.put("id", bestSeller.getId());
            bestSellerInfo.put("title", bestSeller.getTitle());
            bestSellerInfo.put("sold", bestSeller.getQuantitySold());
            bestSellerInfo.put("revenue", bestSeller.getDiscountedPrice() * bestSeller.getQuantitySold());
            stats.put("bestSeller", bestSellerInfo);
        }
        return stats;
    }

    @Override
    @Transactional
    public List<ProductDTO> createMultipleProducts(List<CreateProductRequest> requests) {
        return requests.stream()
                .map(req -> productService.createProduct(req))
                .map(product -> new ProductDTO(product))
                .toList();
    }

    @Override
    public Page<ProductDTO> getSellerProductsWithFilter(Long sellerId, Pageable pageable, FilterProduct filter, String status) {
        // Convert status to boolean for query
        Boolean inStock = null;
        if (status != null && !status.equals("all")) {
            switch (status) {
                case "inStock":
                    inStock = true;
                    break;
                case "outOfStock":
                    inStock = false;
                    break;
            }
        }

        // Add debug logging
        System.out.println("Filter params:");
        System.out.println("- sellerId: " + sellerId);
        System.out.println("- status: " + status);
        System.out.println("- inStock: " + inStock);
        if (filter != null) {
            System.out.println("- keyword: " + filter.getKeyword());
            System.out.println("- topLevelCategory: " + filter.getTopLevelCategory());
        }

        // Apply custom sorting if specified in filter
        Pageable finalPageable = pageable;
        if (filter != null && filter.getSort() != null && !filter.getSort().isEmpty()) {
            finalPageable = applySorting(pageable, filter.getSort());
        }

        Page<Product> productPage = productRepository.findBySellerIdWithFilters(
                sellerId,
                filter != null ? filter.getKeyword() : null,
                filter != null ? filter.getTopLevelCategory() : null,
                filter != null ? filter.getSecondLevelCategory() : null,
                filter != null ? filter.getColor() : null,
                filter != null ? filter.getMinPrice() : null,
                filter != null ? filter.getMaxPrice() : null,
                inStock,
                finalPageable
        );

        System.out.println("Query returned " + productPage.getTotalElements() + " products");

        return productPage.map(ProductDTO::new);
    }

    // Get available categories for seller's products (two-level structure)
    @Override
    public Map<String, Object> getSellerCategories(Long sellerId) {
        Map<String, Object> categoriesMap = new HashMap<>();

        // Get top-level categories
        List<String> topLevelCategories = productRepository.findDistinctTopLevelCategoriesBySellerId(sellerId);
        categoriesMap.put("topLevel", topLevelCategories);

        // Get second-level categories grouped by top-level
        Map<String, List<String>> secondLevelByTopLevel = new HashMap<>();
        for (String topLevel : topLevelCategories) {
            List<String> secondLevel = productRepository.findDistinctSecondLevelCategoriesBySellerIdAndTopLevel(sellerId, topLevel);
            if (!secondLevel.isEmpty()) {
                secondLevelByTopLevel.put(topLevel, secondLevel);
            }
        }
        categoriesMap.put("secondLevel", secondLevelByTopLevel);

        return categoriesMap;
    }

    // Get filter statistics for seller
    @Override
    public Map<String, Object> getFilterStatistics(Long sellerId) {
        List<Product> allProducts = productRepository.findBySellerId(sellerId);

        Map<String, Object> stats = new HashMap<>();

        // Price range
        OptionalInt minPrice = allProducts.stream()
                .filter(p -> p.getDiscountedPrice() > 0)
                .mapToInt(Product::getDiscountedPrice)
                .min();
        OptionalInt maxPrice = allProducts.stream()
                .mapToInt(Product::getDiscountedPrice)
                .max();

        stats.put("priceRange", Map.of(
                "min", minPrice.orElse(0),
                "max", maxPrice.orElse(0)
        ));

        // Stock status counts
        long inStockCount = allProducts.stream().filter(p -> p.getQuantity() > 0).count();
        long outOfStockCount = allProducts.size() - inStockCount;

        stats.put("stockStatus", Map.of(
                "inStock", inStockCount,
                "outOfStock", outOfStockCount,
                "total", allProducts.size()
        ));

        // Available colors
        List<String> colors = productRepository.findDistinctColorsBySellerId(sellerId);
        stats.put("colors", colors);

        // Categories
        stats.put("categories", getSellerCategories(sellerId));

        return stats;
    }

    // Helper method to apply custom sorting
    private Pageable applySorting(Pageable pageable, String sortType) {
        Sort sort;
        switch (sortType) {
            case "price_low":
                sort = Sort.by(Sort.Direction.ASC, "discountedPrice");
                break;
            case "price_high":
                sort = Sort.by(Sort.Direction.DESC, "discountedPrice");
                break;
            case "discount":
                sort = Sort.by(Sort.Direction.DESC, "discountPersent");
                break;
            case "newest":
                sort = Sort.by(Sort.Direction.DESC, "createdAt");
                break;
            case "name_asc":
                sort = Sort.by(Sort.Direction.ASC, "title");
                break;
            case "name_desc":
                sort = Sort.by(Sort.Direction.DESC, "title");
                break;
            default:
                return pageable; // Keep original sorting
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}


