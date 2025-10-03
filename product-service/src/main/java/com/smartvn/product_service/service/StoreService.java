package com.smartvn.product_service.service;

import com.smartvn.product_service.model.Store;
import com.smartvn.product_service.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }

    public Store getStoreById(Long id) {
        return storeRepository.findById(id).orElseThrow(() -> new RuntimeException("Store not found"));
    }

    public Store createStore(String name) {
        Store store = new Store();
        store.setName(name);
        return storeRepository.save(store);
    }
}