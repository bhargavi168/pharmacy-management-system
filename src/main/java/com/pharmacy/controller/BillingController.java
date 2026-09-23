package com.pharmacy.controller;

import com.pharmacy.model.Customer;
import com.pharmacy.model.Medicine;
import com.pharmacy.model.Sale;
import com.pharmacy.service.BillingService;
import com.pharmacy.service.MedicineService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class BillingController {

    private final MedicineService medicineService;
    private final BillingService billingService;

    public BillingController(MedicineService medicineService, BillingService billingService) {
        this.medicineService = medicineService;
        this.billingService = billingService;
    }

    @GetMapping("/billing")
    public String billingPage(Model model) {
        model.addAttribute("medicines", medicineService.getAllMedicines());
        model.addAttribute("customer", new Customer());
        model.addAttribute("cart", new ArrayList<Map<String, Object>>());
        model.addAttribute("selectedSearch", "");
        return "billing";
    }

    @PostMapping("/billing/search")
    @ResponseBody
    public List<Medicine> searchMedicine(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return medicineService.getAllMedicines();
        }
        return medicineService.searchMedicines(keyword, "");
    }

    @PostMapping("/billing/checkout")
    public String checkout(@RequestParam String customerName,
                           @RequestParam String customerPhone,
                           @RequestParam(value = "medicineId[]", required = false) List<Long> medicineIds,
                           @RequestParam(value = "quantity[]", required = false) List<Integer> quantities,
                           Model model) {

        try {
            Customer customer = new Customer();
            customer.setName(customerName);
            customer.setPhone(customerPhone);

            List<Map<String, Object>> cartItems = new ArrayList<>();
            if (medicineIds != null && quantities != null) {
                for (int i = 0; i < medicineIds.size(); i++) {
                    if (medicineIds.get(i) != null && quantities.get(i) != null && quantities.get(i) > 0) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("medicineId", medicineIds.get(i));
                        item.put("quantity", quantities.get(i));
                        cartItems.add(item);
                    }
                }
            }

            Sale sale = billingService.processSale(customer, cartItems);
            model.addAttribute("sale", sale);
            return "invoice";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("medicines", medicineService.getAllMedicines());
            model.addAttribute("customer", new Customer());
            model.addAttribute("cart", new ArrayList<Map<String, Object>>());
            return "billing";
        }
    }

    @GetMapping("/sales")
    public String salesList(Model model) {
        model.addAttribute("sales", billingService.getAllSales());
        return "sales";
    }

    @GetMapping("/invoice/{id}")
    public String invoice(@PathVariable Long id, Model model) {
        Sale sale = billingService.getAllSales().stream()
                .filter(s -> s.getSaleId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + id));
        model.addAttribute("sale", sale);
        return "invoice";
    }
}
