package com.scaler.productcatalogservice.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.scaler.productcatalogservice.clients.EmbeddingClient;
import com.scaler.productcatalogservice.dtos.MeaningHitDto;
import com.scaler.productcatalogservice.dtos.SearchRequestDto;
import com.scaler.productcatalogservice.models.Product;
import com.scaler.productcatalogservice.models.State;
import com.scaler.productcatalogservice.repositories.ProductRepo;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class MeaningSearchServiceTest {

    @Mock
    private ProductRepo productRepo;
    @Mock
    private EmbeddingClient embeddingClient;
    @Mock
    private SearchService searchService;
    @InjectMocks
    private MeaningSearchService meaningSearchService;

    @Test
    void cosine_sameDirectionIs1_nothingInCommonIs0() {
        assertEquals(1.0, MeaningSearchService.cosine(new float[]{1, 0, 0}, new float[]{5, 0, 0}), 1e-9);
        assertEquals(0.0, MeaningSearchService.cosine(new float[]{1, 0, 0}, new float[]{0, 1, 0}), 1e-9);
        assertEquals(Math.sqrt(0.5), MeaningSearchService.cosine(new float[]{1, 0}, new float[]{1, 1}), 1e-9);
    }

    @Test
    void search_returnsTheClosestProductsFirstWithTheirScores_andSkipsProductsWithoutNumbers() {
        when(embeddingClient.embed(any(), eq("RETRIEVAL_QUERY"))).thenReturn(List.of(new float[]{1, 0}));
        when(productRepo.findAllByState(State.ACTIVE)).thenReturn(List.of(
                product("Far", "0,1"), product("Near", "1,0"), product("Middle", "0.8,0.6"), product("NoNumbers", null)));

        List<MeaningHitDto> hits = meaningSearchService.search("anything", 2);

        assertEquals(List.of("Near", "Middle"), hits.stream().map(MeaningHitDto::name).toList());
        assertEquals(1.0, hits.get(0).score(), 1e-6);
        assertEquals(0.8, hits.get(1).score(), 1e-6);
    }

    @Test
    void search_whenTheNumbersServiceFails_usesThePlainSearch() {
        when(embeddingClient.embed(any(), any())).thenThrow(new RestClientException("down"));
        when(searchService.search(any(SearchRequestDto.class))).thenReturn(new PageImpl<>(List.of(product("Red Lipstick", null))));

        List<MeaningHitDto> hits = meaningSearchService.search("lipstick", 5);

        assertEquals(1, hits.size());
        assertEquals("Red Lipstick", hits.get(0).name());
        assertNull(hits.get(0).score());
    }

    private Product product(String name, String embedding) {
        Product product = new Product();
        product.setName(name);
        product.setEmbedding(embedding);
        return product;
    }
}
