package com.scaler.productcatalogservice.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.scaler.productcatalogservice.config.SecurityConfig;
import com.scaler.productcatalogservice.dtos.MeaningHitDto;
import com.scaler.productcatalogservice.dtos.SearchRequestDto;
import com.scaler.productcatalogservice.mappers.ProductMapper;
import com.scaler.productcatalogservice.models.Product;
import com.scaler.productcatalogservice.services.MeaningSearchService;
import com.scaler.productcatalogservice.services.SearchService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(SearchController.class)
@Import({ProductMapper.class, SecurityConfig.class})
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private SearchService searchService;
    @MockitoBean
    private MeaningSearchService meaningSearchService;

    @Test
    void postSearch_withoutToken_returns200AndReadsTheBody() throws Exception {
        Product mascara = new Product();
        mascara.setName("Mascara");
        when(searchService.search(any())).thenReturn(new PageImpl<>(List.of(mascara)));

        mockMvc.perform(post("/products/search").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"mascara\",\"maxPrice\":20,\"sortBy\":\"price\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Mascara"));

        ArgumentCaptor<SearchRequestDto> request = ArgumentCaptor.forClass(SearchRequestDto.class);
        verify(searchService).search(request.capture());
        org.junit.jupiter.api.Assertions.assertEquals("mascara", request.getValue().getQuery());
        org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("20"), request.getValue().getMaxPrice());
        org.junit.jupiter.api.Assertions.assertEquals("price", request.getValue().getSortBy());
    }

    @Test
    void getSearch_wordsInTheAddress_arriveInTheSameClass() throws Exception {
        when(searchService.search(any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/products/search?query=mascara&maxPrice=20&page=2&size=3&sortBy=price"))
                .andExpect(status().isOk());

        ArgumentCaptor<SearchRequestDto> request = ArgumentCaptor.forClass(SearchRequestDto.class);
        verify(searchService).search(request.capture());
        org.junit.jupiter.api.Assertions.assertEquals("mascara", request.getValue().getQuery());
        org.junit.jupiter.api.Assertions.assertEquals(2, request.getValue().getPage());
        org.junit.jupiter.api.Assertions.assertEquals(3, request.getValue().getSize());
        org.junit.jupiter.api.Assertions.assertEquals("price", request.getValue().getSortBy());
    }

    @Test
    void search_whenTheServiceRefusesTheSortField_returns400WithTheReason() throws Exception {
        when(searchService.search(any())).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can sort by name or price, not by 'secret'."));

        mockMvc.perform(post("/products/search").contentType(MediaType.APPLICATION_JSON).content("{\"sortBy\":\"secret\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMeaning_returnsTheClosestProductsWithTheirScores() throws Exception {
        when(meaningSearchService.search("eyes look bigger", 5)).thenReturn(List.of(new MeaningHitDto("Mascara", 0.66)));

        mockMvc.perform(get("/products/search/meaning").param("query", "eyes look bigger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Mascara"))
                .andExpect(jsonPath("$[0].score").value(0.66));
    }

    @Test
    void getMeaning_withoutTheQueryName_returns400() throws Exception {
        mockMvc.perform(get("/products/search/meaning"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSearch_withAPriceThatIsNotANumber_returns400() throws Exception {
        mockMvc.perform(get("/products/search?maxPrice=abc"))
                .andExpect(status().isBadRequest());
    }
}
