package com.pharmacy.service;

import com.pharmacy.model.*;
import com.pharmacy.repository.CustomerRepository;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BillingService {

    private final MedicineRepository medicineRepository;
    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

    public BillingService(MedicineRepository medicineRepository, CustomerRepository customerRepository, SaleRepository saleRepository) {
        this.medicineRepository = medicineRepository;
        this.customerRepository = customerRepository;
        this.saleRepository = saleRepository;
    }

    @Transactional
    public Sale processSale(Customer customer, List<Map<String, Object>> cartItems) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required.");
        }

        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart cannot be empty.");
        }

        Customer savedCustomer = customerRepository.save(customer);
        Sale sale = new Sale();
        sale.setCustomer(savedCustomer);
        sale.setSaleDate(LocalDateTime.now());

        List<SaleItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Map<String, Object> cartItem : cartItems) {
            Long medicineId = Long.valueOf(cartItem.get("medicineId").toString());
            Integer quantity = Integer.valueOf(cartItem.get("quantity").toString());

            Medicine medicine = medicineRepository.findById(medicineId)
                    .orElseThrow(() -> new IllegalArgumentException("Medicine not found: " + medicineId));

            if (medicine.getExpiryDate().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Medicine expired: " + medicine.getName());
            }

            if (quantity > medicine.getStockQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for: " + medicine.getName());
            }

            if (quantity <= 0) {
                throw new IllegalArgumentException("Invalid quantity for: " + medicine.getName());
            }

            medicine.setStockQuantity(medicine.getStockQuantity() - quantity);
            medicineRepository.save(medicine);

            SaleItem saleItem = new SaleItem();
            saleItem.setMedicine(medicine);
            saleItem.setSale(sale);
            saleItem.setQuantity(quantity);
            BigDecimal subtotal = medicine.getUnitPrice().multiply(BigDecimal.valueOf(quantity));
            saleItem.setSubtotal(subtotal);
            items.add(saleItem);

            total = total.add(subtotal);
        }

        BigDecimal tax = total.multiply(TAX_RATE);
        BigDecimal grandTotal = total.add(tax);
        sale.setTotalAmount(grandTotal);
        sale.setSaleItems(items);

        for (SaleItem item : items) {
            item.setSale(sale);
        }

        return saleRepository.save(sale);
    }

    public List<Sale> getAllSales() {
        return saleRepository.findAllByOrderBySaleDateDesc();
    }

    public Long getTodaySalesCount() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return saleRepository.countSalesForToday(startOfDay, endOfDay);
    }

    public BigDecimal getTodaySalesAmount() {
        return getAllSales().stream()
                .filter(sale -> sale.getSaleDate().toLocalDate().isEqual(LocalDate.now()))
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
