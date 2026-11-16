package com.scaler.productcatalogservice.services;

import com.scaler.productcatalogservice.dtos.SearchRequestDto;
import com.scaler.productcatalogservice.models.Product;
import com.scaler.productcatalogservice.models.State;
import com.scaler.productcatalogservice.repositories.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ProductRepo productRepo;

    public Page<Product> search(SearchRequestDto request) {
        String word = StringUtils.hasText(request.getQuery()) ? "%" + request.getQuery().trim() + "%" : null;
        return productRepo.search(State.ACTIVE, word, request.getMaxPrice(), PageRequest.of(0, 20));
    }
}
