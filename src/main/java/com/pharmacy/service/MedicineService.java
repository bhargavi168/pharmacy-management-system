package com.pharmacy.service;

import com.pharmacy.model.Medicine;
import com.pharmacy.repository.MedicineRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedicineService {

    private final MedicineRepository medicineRepository;

    public MedicineService(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    public List<Medicine> searchMedicines(String name, String category) {
        return medicineRepository.searchMedicines(name, category);
    }

    public List<Medicine> getLowStockMedicines() {
        return medicineRepository.findLowStockMedicines();
    }

    public List<Medicine> getExpiredMedicines() {
        return medicineRepository.findExpiredMedicines();
    }

    public Medicine save(Medicine medicine) {
        return medicineRepository.save(medicine);
    }

    public Optional<Medicine> findById(Long id) {
        return medicineRepository.findById(id);
    }

    public void deleteById(Long id) {
        medicineRepository.deleteById(id);
    }

    public long countAll() {
        return medicineRepository.count();
    }

    public long countLowStock() {
        return medicineRepository.findLowStockMedicines().size();
    }

    public long countExpired() {
        return medicineRepository.findExpiredMedicines().size();
    }
}
