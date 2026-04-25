package tn.ecocycle.ecocycletn.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.ecocycle.ecocycletn.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
