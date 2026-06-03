package guru.springframework.repositories.reactive;

import guru.springframework.domain.Category;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CategoryReactiveRepository extends ReactiveMongoRepository<Category, String> {

}
