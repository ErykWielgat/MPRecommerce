package pl.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.ecommerce.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}