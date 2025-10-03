package com.smartvn.product_service.service;

import com.smartvn.product_service.client.UserServiceClient;
import com.smartvn.product_service.dto.ReviewDTO;
import com.smartvn.product_service.dto.ReviewRequest;
import com.smartvn.product_service.dto.UserInfoDTO;
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
    private final UserServiceClient userServiceClient; // ✅ Thêm

    @Transactional
    public Review createReview(Long userId, Long productId, ReviewRequest reviewRequest) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException("Product not found with id: " + productId, HttpStatus.NOT_FOUND));

        Review review = new Review();
        review.setUserId(userId);
        review.setProduct(product);
        review.setRating(reviewRequest.getRating());
        review.setReviewContent(reviewRequest.getContent());
        review.setStatus("PENDING");

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

            dto.setUserFirstName(userInfo.getFirstName());
            dto.setUserLastName(userInfo.getLastName());
            dto.setUserAvatar(userInfo.getAvatar());

        } catch (Exception e) {
            log.error("Failed to fetch user info for userId: {}. Error: {}",
                    review.getUserId(), e.getMessage());
            // Fallback: set giá trị mặc định
            dto.setUserFirstName("Anonymous");
            dto.setUserLastName("");
            dto.setUserAvatar(null);
        }

        return dto;
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
}