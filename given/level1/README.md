# given/level1: the POST search

The instructor pastes these pieces into the editor one at a time, and explains each one. Students paste the same pieces. The order is the order of the stops in the script: the controller, the request class, the service, the query, the security line.

| What | Where it goes |
|---|---|
| `SearchRequestDto.java` | `dtos/` |
| `SearchService.java` | `services/` |
| `SearchController.java` | `controllers/` |
| `ProductRepo-add-this-method.txt` | paste the method into `ProductRepo` |
| `SecurityConfig-add-this-line.txt` | paste the line into `SecurityConfig`, above `anyRequest()` |

After the last piece, `POST /products/search` works with a word and a maximum price. The GET search is typed live in class.
