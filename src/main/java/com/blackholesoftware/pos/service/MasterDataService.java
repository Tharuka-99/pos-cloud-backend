package com.blackholesoftware.pos.service;

import com.blackholesoftware.pos.entity.*;
import com.blackholesoftware.pos.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MasterDataService {

    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final UnitRepository unitRepository;
    private final SupplierRepository supplierRepository;

    public MasterDataService(CategoryRepository categoryRepository,
                             BrandRepository brandRepository,
                             UnitRepository unitRepository,
                             SupplierRepository supplierRepository) {
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.unitRepository = unitRepository;
        this.supplierRepository = supplierRepository;
    }

    // --- CATEGORY SERVICES ---
    public List<Category> getAllCategories() { return categoryRepository.findAll(); }
    public Category saveCategory(Category category) { return categoryRepository.save(category); }
    public Category updateCategory(String id, Category category) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        existing.setName(category.getName());
        existing.setDescription(category.getDescription());
        return categoryRepository.save(existing);
    }
    public void deleteCategory(String id) { categoryRepository.deleteById(id); }

    // --- BRAND SERVICES ---
    public List<Brand> getAllBrands() { return brandRepository.findAll(); }
    public Brand saveBrand(Brand brand) { return brandRepository.save(brand); }
    public Brand updateBrand(String id, Brand brand) {
        Brand existing = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));
        existing.setName(brand.getName());
        return brandRepository.save(existing);
    }
    public void deleteBrand(String id) { brandRepository.deleteById(id); }

    // --- UNIT SERVICES ---
    public List<Unit> getAllUnits() { return unitRepository.findAll(); }
    public Unit saveUnit(Unit unit) { return unitRepository.save(unit); }
    public Unit updateUnit(String id, Unit unit) {
        Unit existing = unitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unit not found with id: " + id));
        existing.setName(unit.getName());
        existing.setShortCode(unit.getShortCode());
        return unitRepository.save(existing);
    }
    public void deleteUnit(String id) { unitRepository.deleteById(id); }

    // --- SUPPLIER SERVICES ---
    public List<Supplier> getAllSuppliers() { return supplierRepository.findAll(); }
    public Supplier saveSupplier(Supplier supplier) { return supplierRepository.save(supplier); }
    public Supplier updateSupplier(String id, Supplier supplier) {
        Supplier existing = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + id));
        existing.setName(supplier.getName());
        existing.setCompanyName(supplier.getCompanyName());
        existing.setPhone(supplier.getPhone());
        existing.setEmail(supplier.getEmail());
        existing.setAddress(supplier.getAddress());
        return supplierRepository.save(existing);
    }
    public void deleteSupplier(String id) { supplierRepository.deleteById(id); }
}