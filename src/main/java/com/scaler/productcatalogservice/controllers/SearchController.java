package com.scaler.productcatalogservice.controllers;

import com.scaler.productcatalogservice.dtos.MeaningHitDto;
import com.scaler.productcatalogservice.dtos.ProductDto;
import com.scaler.productcatalogservice.dtos.SearchRequestDto;
import com.scaler.productcatalogservice.mappers.ProductMapper;
import com.scaler.productcatalogservice.services.MeaningSearchService;
import com.scaler.productcatalogservice.services.SearchService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final ProductMapper productMapper;
    private final MeaningSearchService meaningSearchService;

    // The search words travel in the body.
    @PostMapping
    public PagedModel<ProductDto> search(@RequestBody SearchRequestDto request) {
        return new PagedModel<>(searchService.search(request).map(productMapper::toDto));
    }

    // The same search words travel in the address. Spring fills the same class from the address.
    @GetMapping
    public PagedModel<ProductDto> searchByAddress(SearchRequestDto request) {
        return new PagedModel<>(searchService.search(request).map(productMapper::toDto));
    }

    // No matching words needed. The closest products by meaning come back, with their score.
    @GetMapping("/meaning")
    public List<MeaningHitDto> meaning(@RequestParam String query) {
        return meaningSearchService.search(query, 5);
    }
}
