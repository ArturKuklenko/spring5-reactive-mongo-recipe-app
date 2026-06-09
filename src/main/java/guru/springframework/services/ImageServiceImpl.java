package guru.springframework.services;

import guru.springframework.domain.Recipe;
import guru.springframework.repositories.RecipeRepository;
import guru.springframework.repositories.reactive.RecipeReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;

/**
 * Created by jt on 7/3/17.
 */
@Slf4j
@Service
public class ImageServiceImpl implements ImageService {


    private final RecipeReactiveRepository recipeReactiveRepository;

    public ImageServiceImpl( RecipeReactiveRepository recipeReactiveRepository) {

        this.recipeReactiveRepository = recipeReactiveRepository;
    }

    @Override
    @Transactional
    public Mono<Recipe> saveImageFile(String recipeId, MultipartFile file) {
        try {
            Byte[] byteObjects = new Byte[file.getBytes().length];

            int i = 0;

            for (byte b : file.getBytes()){
                byteObjects[i++] = b;
            }
            Mono<Recipe> recipeMono = recipeReactiveRepository
                    .findById(recipeId)
                    .flatMap(recipe -> {
                        recipe.setImage(byteObjects);
                        return recipeReactiveRepository.save(recipe);
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        log.error("Recipe not found for id: " + recipeId);
                        return Mono.empty();
                    }));
            return recipeMono;
        } catch (IOException e) {
            //todo handle better
            log.error("Error occurred", e);

            e.printStackTrace();
        }
        return Mono.empty();
    }
}
