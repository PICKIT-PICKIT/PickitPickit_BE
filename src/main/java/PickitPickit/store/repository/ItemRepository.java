package PickitPickit.store.repository;

import PickitPickit.store.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    boolean existsByName(String name);

    List<Item> findAllByOrderByNameAsc();

    List<Item> findByNameContainingIgnoreCaseOrderByNameAsc(String keyword);
}
