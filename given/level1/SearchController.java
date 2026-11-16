package com.scaler.productcatalogservice.controllers;

import com.scaler.productcatalogservice.dtos.ProductDto;
import com.scaler.productcatalogservice.dtos.SearchRequestDto;
import com.scaler.productcatalogservice.mappers.ProductMapper;
import com.scaler.productcatalogservice.services.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final ProductMapper productMapper;

    @PostMapping
    public PagedModel<ProductDto> search(@RequestBody SearchRequestDto request) {
        return new PagedModel<>(searchService.search(request).map(productMapper::toDto));
    }
}
