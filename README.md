# productcatalogservice (ShopNest): the project at the END of Class 22

The catalog with a real search box: words, an optional price box, pages, sort, and a search by meaning. Spring Boot 4.1.1, Java 17 (also runs on 21), MySQL 8 or newer, port **8080**. The POST search is built before class (`given/level1`) and only walked through. The GET search, paging and the meaning search are typed live. The plumbing is in the folder `given/`.

## What Class 22 changed

| File | How it is made in class |
|---|---|
| `dtos/SearchRequestDto` | Given for the POST search (`given/level1`): two boxes (`query`, `maxPrice`). Then three more (`page`, `size`, `sortBy`) are typed. |
| `repositories/ProductRepo` | Given for the POST search (`given/level1`, a method to paste): one query, `search`. An empty box is `null`, and `:word is null or ...` ignores it. `@EntityGraph` loads the category in the same query (no N plus 1). |
| `services/SearchService` | Given for the POST search: the first 20 products. Then `pageable(...)` is typed: a safe list of sort fields, a maximum size, a page that is never negative, and the id as the last sort field. |
| `controllers/SearchController` | Given for the POST (`given/level1`): the instructor builds it before class and only walks through it. The GET door (the one ShopNest keeps) and `GET /products/search/meaning?query=...` are typed live. |
| `config/SecurityConfig` | Given for the POST (`given/level1`, a line to paste): one line. A GET needs no line, because every GET is open already. `POST /products/search/**` is open (it only reads). |
| `application.properties` | Typed: `spring.mvc.problemdetails.enabled=true` (a refused request shows its reason) and `embedding.api-key=${GEMINI_API_KEY:}`. |
| `db/migration/V3__product_embedding.sql`, `models/Product` | Typed: one column and one field for the saved numbers. |
| `clients/EmbeddingClient`, `dtos/GeminiEmbeddingResponse`, `dtos/MeaningHitDto`, `services/ProductEmbedder` | Given (`given/level4`). The call to Google's numbers service, and the one-time job (100 products at a time, waits 62 seconds and tries again on a 429). |
| `services/MeaningSearchService` | Given without the `cosine` method (`given/level4`). The `cosine` method is typed. |
| `seed/scale-200k.sql` | Prepared. Makes a big database `shopnest_scale` with 200,000 products (20 of them are a telescope). The word index and keyset are shown at the MySQL prompt. |
| `tools/walk.py` | Prepared. Reads every page, and counts the products shown twice and never shown. |

The longer version of the project (with `Specification` filters, the FULLTEXT search and keyset paging in Java, and a table for the saved numbers) is kept in `Class 22 - catalog (full version, answers for homework)`.

## Run it

```bash
export DB_PASSWORD=your-password GROQ_API_KEY=any-value
mvn spring-boot:run
```

The key for the meaning search is only needed for the last part: `export GEMINI_API_KEY=...` (never write it in a file of the project). To make the numbers once: `mvn spring-boot:run -Dspring-boot.run.arguments=--search.embed-on-start=true`. This takes about 75 seconds with a free key (100 texts a minute).

## What was tested (JDK 21, Boot 4.1.1, MySQL 8.4.11)

- `mvn verify` with a test schema (the integration test refuses a URL without `_test`): **75 tests, 0 failures** (the first run of 73 covered 276 of 328 lines, 84.1 percent). The rule is 0.70. One test runs the optional-box query on the real MySQL.
- **Every stage of the class was built from the code boxes of the script** (the pre-built POST search from `given/level1`, then the typed GET door, Section 4 break and fix, Section 6 given files plus the typed blocks; the code boxes the script shows in its walkthrough were also checked to be inside the given files) and its commands were run as written. Section 5 was run as written at the MySQL prompt on 200,000 products.
- The break: sorting by price only and reading page by page shows some products twice and the same number never (7 and 7 on one database, 14 and 14 on another). With the id as the last sort field: 0 and 0.
- At the MySQL prompt, 200,000 products: the plain `LIKE` search 0.06 seconds; the FULLTEXT index (2.5 seconds to build) 0.00 seconds; page 9,500 by offset 0.11 seconds, by keyset 0.00 seconds. `EXPLAIN` shows `type: ALL` for the `LIKE` search, `type: fulltext` for the word index, and `type: range` on `idx_product_price_id` for keyset.
- **The real Google service, with a free AI Studio key:** 768 numbers for each text; the free key allows **100 texts a minute**, so the job (100 + 94 products) waits once, about 75 seconds in all. Real meaning searches take about half a second. "something to make my eyes look bigger" gives Essence Mascara Lash Princess (0.66), Eyeshadow Palette with Mirror (0.65), Red Lipstick (0.61), and the plain search finds nothing. "phone that is not an iPhone" gives three iPhones (0.68): the meaning search does not understand "not". A wrong key gives Google's 400, and the search answers with the plain search (`score` is null). The simplified client sends the same request as the one in the real run; it was checked against a pretend service with the same URL, header and JSON, and by `EmbeddingClientTest`.
