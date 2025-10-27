package com.smartvn.product_service.service;

import com.smartvn.product_service.client.OrderServiceClient;
import com.smartvn.product_service.client.UserServiceClient;
import com.smartvn.product_service.dto.ReviewDTO;
import com.smartvn.product_service.dto.ReviewRequest;
import com.smartvn.product_service.dto.UserInfoDTO;
import com.smartvn.product_service.dto.admin.UserDTO;
import com.smartvn.product_service.exceptions.AppException;
import com.smartvn.product_service.model.Product;
import com.smartvn.product_service.model.Review;
import com.smartvn.product_service.repository.ProductRepository;
import com.smartvn.product_service.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    private final UserServiceClient userServiceClient;
    private final OrderServiceClient  orderServiceClient;

    @Transactional
    public Review createReview(Long userId, Long productId, ReviewRequest reviewRequest) {
        Boolean hasPurchased= orderServiceClient.hasUserPurchasedProduct(userId, productId);
        if(!hasPurchased){
            throw new AppException(
                    "Bạn phải mua sản phẩm này trước khi đánh giá",
                    HttpStatus.FORBIDDEN
                    );
        }

        if (reviewRepository.findByProductIdAndUserId(productId, userId).isPresent()) {
            throw new AppException("You have already reviewed this product.", HttpStatus.BAD_REQUEST);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException("Product not found with id: " + productId, HttpStatus.NOT_FOUND));

        Review review = new Review();
        review.setUserId(userId);
        review.setProduct(product);
        review.setRating(reviewRequest.getRating());
        review.setReviewContent(reviewRequest.getReviewContent());
        review.setStatus("APPROVED"); // Auto-approve for now

        Review savedReview = reviewRepository.save(review);
        log.info("Created a new review with id {} for product {}", savedReview.getId(), productId);

        updateProductRating(productId);

        return savedReview;
    }

    // ✅ Thêm method mới để convert Review sang ReviewDTO có thông tin User
    public ReviewDTO getReviewDTO(Review review) {
        ReviewDTO dto = new ReviewDTO(review);

        try {
            log.debug("Fetching user info for userId: {}", review.getUserId());
            UserInfoDTO userInfo = userServiceClient.getUserInfo(review.getUserId());
            if (userInfo != null) {
                dto.setUserFirstName(userInfo.getFirstName());
                dto.setUserLastName(userInfo.getLastName());
                dto.setUserAvatar(userInfo.getAvatar());
            } else {
                // SỬA ĐỔI: Thêm log khi userInfo trả về là null
                log.warn("User info for userId: {} was null.", review.getUserId());
                setFallbackUserInfo(dto);
            }
        } catch (Exception e) {
            // SỬA ĐỔI: Ghi log lỗi chi tiết hơn
            log.error("Failed to fetch user info for userId: {}. Error type: {}. Message: {}",
                    review.getUserId(), e.getClass().getName(), e.getMessage());
            setFallbackUserInfo(dto);
        }

        return dto;
    }

    // Phương thức helper để tránh lặp code
    private void setFallbackUserInfo(ReviewDTO dto) {
        dto.setUserFirstName("Anonymous");
        dto.setUserLastName("User");
        dto.setUserAvatar(null); // Hoặc một URL avatar mặc định
    }

    // ✅ Update method này để trả về Page<ReviewDTO> thay vì Page<Review>
    public Page<ReviewDTO> getProductReviews(Long productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new AppException("Product not found with id: " + productId, HttpStatus.NOT_FOUND);
        }

        Page<Review> reviews = reviewRepository.findByProductId(productId, pageable);
        return reviews.map(this::getReviewDTO);
    }

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
            double roundedAverage = Math.round(average * 10.0) / 10.0;

            product.setNumRatings(reviews.size());
            product.setAverageRating(roundedAverage);
        }

        productRepository.save(product);
        log.info("Updated product {} rating: {} stars from {} reviews.",
                productId, product.getAverageRating(), product.getNumRatings());
    }


    public Page<Review> searchReviewsForAdmin(String status, Long productId, Long userId, Pageable pageable) {
        Specification<Review> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        if (productId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("product").get("id"), productId));
        }

        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("userId"), userId));
        }

        return reviewRepository.findAll(spec, pageable);
    }

    @Transactional
    public void deleteReviewByAdmin(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException("Review not found", HttpStatus.NOT_FOUND));

        // ✅ LƯU THÔNG TIN TRƯỚC KHI XÓA
        Long userId = review.getUserId();
        Long productId = review.getProduct().getId();

        // ✅ 1. TĂNG WARNING COUNT TRƯỚC
        try {
            UserDTO userDTO = userServiceClient.getUserById(userId);

            // Call user service để tăng warning count
            userServiceClient.warnUser(userId);

            // Check nếu đủ 3 warning thì ban
            if(userDTO.getWarningCount() + 1 >= 3) {
                userServiceClient.banUser(userId);
                log.warn("User {} has been banned after 3 warnings", userId);
            }
        } catch (Exception e) {
            log.error("Failed to update user warning count for userId: {}", userId, e);
            // ⚠️ Tiếp tục xóa review dù user service lỗi
        }

        // ✅ 2. XÓA REVIEW
        reviewRepository.delete(review);
        log.info("Deleted review {} by admin", reviewId);

        // ✅ 3. CẬP NHẬT RATING
        try {
            updateProductRating(productId);
        } catch (Exception e) {
            log.error("Failed to update product rating after review deletion", e);
            // Không throw exception để transaction vẫn commit
        }
    }
}