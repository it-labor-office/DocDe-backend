package com.docde.domain.review.repository;

import com.docde.domain.review.entity.Review;
import com.docde.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByUser(User user);
}
