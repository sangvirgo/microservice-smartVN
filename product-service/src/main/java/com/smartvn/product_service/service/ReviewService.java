package com.smartvn.product_service.service;

import com.smartvn.product_service.dto.ReviewRequest;
import com.smartvn.product_service.exceptions.AppException;
import com.smartvn.product_service.model.Product;
import com.smartvn.product_service.model.Review;
import com.smartvn.product_service.repository.ProductRepository;
import com.smartvn.product_service.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    /**
     * Tạo một bài đánh giá mới cho sản phẩm.
     *
     * @param userId        ID của người dùng viết đánh giá (lấy từ security context hoặc token).
     * @param productId     ID của sản phẩm được đánh giá.
     * @param reviewRequest DTO chứa thông tin đánh giá.
     * @return Entity Review đã được tạo.
     */
    @Transactional
    public Review createReview(Long userId, Long productId, ReviewRequest reviewRequest) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException("Product not found with id: " + productId, HttpStatus.NOT_FOUND));

        Review review = new Review();
        review.setUserId(userId);
        review.setProduct(product);
        review.setRating(reviewRequest.getRating());
        review.setReviewContent(reviewRequest.getContent());
        review.setStatus("PENDING"); // Mặc định là chờ duyệt

        Review savedReview = reviewRepository.save(review);
        log.info("Created a new review with id {} for product {}", savedReview.getId(), productId);

        // Sau khi lưu review, cập nhật lại rating cho sản phẩm
        updateProductRating(productId);

        return savedReview;
    }

    /**
     * Lấy danh sách các bài đánh giá của một sản phẩm (phân trang).
     *
     * @param productId ID của sản phẩm.
     * @param pageable  Thông tin phân trang.
     * @return Page chứa các bài đánh giá.
     */
    public Page<Review> getProductReviews(Long productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new AppException("Product not found with id: " + productId, HttpStatus.NOT_FOUND);
        }
        return reviewRepository.findByProductId(productId, pageable);
    }

    /**
     * [PRIVATE] Cập nhật lại số lượt đánh giá và điểm trung bình của sản phẩm.
     * Được gọi sau khi có review mới hoặc review được cập nhật/xóa.
     *
     * @param productId ID của sản phẩm cần cập nhật.
     */
    private void updateProductRating(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException("Product not found while updating rating: " + productId, HttpStatus.NOT_FOUND));

        List<Review> reviews = reviewRepository.findAllByProductId(productId);

        if (reviews.isEmpty()) {
            product.setNumRatings(0);
            product.setAverageRating(0.0);
        } else {
            double totalRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .sum();
            double average = totalRating / reviews.size();
            // Làm tròn đến 1 chữ số thập phân
            double roundedAverage = Math.round(average * 10.0) / 10.0;

            product.setNumRatings(reviews.size());
            product.setAverageRating(roundedAverage);
        }

        productRepository.save(product);
        log.info("Updated product {} rating: {} stars from {} reviews.",
                productId, product.getAverageRating(), product.getNumRatings());
    }
}