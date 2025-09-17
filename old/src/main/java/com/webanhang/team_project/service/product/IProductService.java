package com.webanhang.team_project.service.product;



import com.webanhang.team_project.dto.product.FilterProduct;
import com.webanhang.team_project.dto.product.ProductDTO;
import com.webanhang.team_project.dto.product.CreateProductRequest;
import com.webanhang.team_project.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface IProductService {
    public Product createProduct(CreateProductRequest req) ;

    public String deleteProduct(Long productId) ;

    public Product updateProduct(Long productId, Product product) ;

    ProductDTO updateProductByID(Long productId, Product product);

    public Product findProductById(Long id) ;

    public List<Product> findProductByCategory(String category) ;

    List<Product> findAllProductsByFilter(FilterProduct filterProduct) ;

    public List<Product> findAllProducts() ;

    List<ProductDTO> getAllProducts(String search, String category, String sort, String order);

    public List<Product> searchProducts(String keyword);

    public List<Map<String, Object>> getTopSellingProducts(int limit);
    
    public Map<String, Object> getRevenueByCateogry();

    public List<Product> findByCategoryTopAndSecond(String topCategory, String secondCategory);

    Page<ProductDTO> getProductsWithFilter(Pageable pageable, FilterProduct filter, String status);

    Map<String, Object> getAdminFilterStatistics();

    Map<String, Object> getAllCategories();

    public void adminDeleteProduct(Long productId);
}
