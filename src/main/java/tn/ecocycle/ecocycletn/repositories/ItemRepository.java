package tn.ecocycle.ecocycletn.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.ecocycle.ecocycletn.entities.Category;
import tn.ecocycle.ecocycletn.entities.RecyclableItem;

public interface ItemRepository extends JpaRepository<RecyclableItem, Long> {

    List<RecyclableItem> findByCategory(Category category);
}
