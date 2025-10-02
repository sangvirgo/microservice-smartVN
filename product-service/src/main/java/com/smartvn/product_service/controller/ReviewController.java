package com.smartvn.product_service.controller;


import com.smartvn.product_service.dto.product.ProductDTO;
import com.smartvn.product_service.dto.response.ApiResponse;
import com.smartvn.product_service.dto.review.ReviewDTO;
import com.smartvn.product_service.model.Product;
import com.smartvn.product_service.model.Review;
import com.smartvn.product_service.dto.review.ReviewRequest;
import com.smartvn.product_service.model.User;
import com.smartvn.product_service.repository.UserRepository;
import com.smartvn.product_service.service.product.ProductService;
import com.smartvn.product_service.service.review.ReviewService;
import com.smartvn.product_service.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    private final UserService userService;

    private final UserRepository userRepository;
    private final ProductService productService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createReview(@RequestHeader("Authorization") String jwt, @RequestBody ReviewRequest reviewRequest) {
        if (jwt == null || jwt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User user = userService.findUserByJwt(jwt);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Review res = reviewService.createReview(user, reviewRequest);
        if (res == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        ReviewDTO reviewDTO = new ReviewDTO(res);
        return ResponseEntity.ok(ApiResponse.success(reviewDTO, "Create Review Success!"));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse> getProductReview(@PathVariable Long productId) { // Sửa kiểu trả về thành ApiResponse
        // Lấy danh sách reviews
        List<Review> reviews = reviewService.getReviewsByProductId(productId);
        // Lấy thông tin product (đã chứa averageRating và numRatings cập nhật)
        Product product = productService.findProductById(productId); // Nên xử lý nếu product không tìm thấy

        // --- Tính toán Rating Distribution ---
        Map<Integer, Long> ratingDistribution = reviews.stream()
                .collect(Collectors.groupingBy(
                        Review::getRating,       // Nhóm theo rating
                        Collectors.counting()    // Đếm số lượng trong mỗi nhóm
                ));

        // Đảm bảo có đủ 5 mức sao, kể cả khi count = 0
        Map<String, Long> finalDistribution = new HashMap<>();
        for (int i = 5; i >= 1; i--) {
            finalDistribution.put(String.valueOf(i), ratingDistribution.getOrDefault(i, 0L)); // Dùng getOrDefault
        }
        // ---------------------------------

        // Tạo đối tượng kết quả
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("productId", productId);
        resultData.put("productName", product.getTitle());
        resultData.put("averageRating", product.getAverageRating()); // Lấy từ Product entity
        resultData.put("totalReviews", product.getNumRatings());   // Lấy từ Product entity
        resultData.put("ratingDistribution", finalDistribution); // Thêm distribution

        // Map reviews sang DTO
        List<ReviewDTO> reviewDTOs = reviews.stream()
                .map(ReviewDTO::new) // Giả sử ReviewDTO có constructor nhận Review
                .toList();
        resultData.put("reviews", reviewDTOs); // Thêm danh sách reviews

        // Trả về ApiResponse
        return ResponseEntity.ok(ApiResponse.success(resultData, "Find Review By Product Success!"));
    }

    @PutMapping("/update/{reviewId}")
    public ResponseEntity<?> updateReview(@PathVariable Long reviewId, @RequestBody ReviewRequest reviewRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication failed", "code", "AUTH_ERROR"));
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);

        Review res = reviewService.updateReview(reviewId, reviewRequest, user);

        if (res == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        ReviewDTO reviewDTO = new ReviewDTO(res);
        return ResponseEntity.ok(ApiResponse.success(reviewDTO, "Update Review Success!"));
    }

    @DeleteMapping("/delete/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication failed", "code", "AUTH_ERROR"));
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email);

        reviewService.deleteReview(reviewId, user);
        return ResponseEntity.ok(ApiResponse.success(null, "Delete Review Success!"));
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse> getReviewById(@PathVariable Long reviewId) {
        Review res = reviewService.getReviewById(reviewId);
        if (res == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        ReviewDTO reviewDTO = new ReviewDTO(res);
        return ResponseEntity.ok(ApiResponse.success(reviewDTO, "Get Review By Id Success!"));
    }

    @GetMapping("/can-review/{productId}")
    public ResponseEntity<?> canUserReviewProduct(@PathVariable Long productId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication failed", "code", "AUTH_ERROR"));
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email);

        boolean canReview = reviewService.canUserReviewProduct(user.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success(canReview, "Check Review Permission Success!"));
    }
}
