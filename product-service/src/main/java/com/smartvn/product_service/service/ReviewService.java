package com.smartvn.product_service.service.review;


import com.smartvn.product_service.dto.review.ReviewRequest;
import com.smartvn.product_service.model.Review;
import com.smartvn.product_service.model.User;

import java.util.List;

public interface ReviewService {
    public Review createReview(User user, ReviewRequest reviewRequest);
    public List<Review> getReviewsByProductId(Long productId);
    public Review updateReview(Long reviewId, ReviewRequest reviewRequest, User user);
    public void deleteReview(Long reviewId, User user);
    public Review getReviewById(Long reviewId);
    public boolean canUserReviewProduct(Long userId, Long productId);
}
