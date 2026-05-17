package com.greenhouse.smart_backend.modules.inventory.repository;

import com.greenhouse.smart_backend.modules.inventory.model.InventoryCategory;
import com.greenhouse.smart_backend.modules.inventory.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {

    List<InventoryItem> findAllByCategory(InventoryCategory category);

    /** Items cuya cantidad es menor o igual al stock mínimo configurado */
    @Query("SELECT i FROM InventoryItem i WHERE i.quantity <= i.minStock")
    List<InventoryItem> findLowStockItems();
}
