package pl.ecommerce.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.ecommerce.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}