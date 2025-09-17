package com.webanhang.team_project.service.seller;

import com.webanhang.team_project.dto.product.CreateProductRequest;
import com.webanhang.team_project.dto.product.FilterProduct;
import com.webanhang.team_project.dto.product.ProductDTO;
import com.webanhang.team_project.dto.product.UpdateProductRequest;
import com.webanhang.team_project.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ISellerProductService {
    ProductDTO createProduct(CreateProductRequest request);

    ProductDTO updateProduct(Long productId, UpdateProductRequest product);

    void deleteProduct(Long productId);

    Page<ProductDTO> getSellerProducts(Long sellerId, Pageable pageable);

    ProductDTO getProductDetail(Long productId);

    Map<String, Object> getProductStatOfSeller(Long sellerId);

    List<ProductDTO> createMultipleProducts(List<CreateProductRequest> requests);

    // Enhanced method with filter support using existing FilterProduct
    Page<ProductDTO> getSellerProductsWithFilter(Long sellerId, Pageable pageable, FilterProduct filter, String status);

    // Filter-related methods for two-level categories
    Map<String, Object> getSellerCategories(Long sellerId);

    Map<String, Object> getFilterStatistics(Long sellerId);
}
