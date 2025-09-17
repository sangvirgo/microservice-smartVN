package com.webanhang.team_project.service.review;


import com.webanhang.team_project.dto.review.ReviewRequest;
import com.webanhang.team_project.model.Review;
import com.webanhang.team_project.model.User;

import java.util.List;

public interface ReviewService {
    public Review createReview(User user, ReviewRequest reviewRequest);
    public List<Review> getReviewsByProductId(Long productId);
    public Review updateReview(Long reviewId, ReviewRequest reviewRequest, User user);
    public void deleteReview(Long reviewId, User user);
    public Review getReviewById(Long reviewId);
    public boolean canUserReviewProduct(Long userId, Long productId);
}
