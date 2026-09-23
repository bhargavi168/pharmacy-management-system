package com.pharmacy.repository;

import com.pharmacy.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    List<Medicine> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String keyword);

    List<Medicine> findByCategoryContainingIgnoreCaseOrderByCreatedAtDesc(String category);

    @Query("SELECT m FROM Medicine m WHERE (:name IS NULL OR :name = '' OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:category IS NULL OR :category = '' OR LOWER(m.category) LIKE LOWER(CONCAT('%', :category, '%'))) " +
            "ORDER BY m.createdAt DESC")
    List<Medicine> searchMedicines(@Param("name") String name, @Param("category") String category);

    @Query("SELECT m FROM Medicine m WHERE m.stockQuantity < 10 ORDER BY m.stockQuantity ASC, m.name ASC")
    List<Medicine> findLowStockMedicines();

    @Query("SELECT m FROM Medicine m WHERE m.expiryDate < CURRENT_DATE ORDER BY m.expiryDate ASC")
    List<Medicine> findExpiredMedicines();
}
