package guru.springframework.services;

import guru.springframework.commands.RecipeCommand;
import guru.springframework.converters.RecipeCommandToRecipe;
import guru.springframework.converters.RecipeToRecipeCommand;
import guru.springframework.domain.Recipe;
import guru.springframework.exceptions.NotFoundException;
import guru.springframework.repositories.RecipeRepository;
import guru.springframework.repositories.reactive.RecipeReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Created by jt on 6/13/17.
 */
@Slf4j
@Service
public class RecipeServiceImpl implements RecipeService {

    private final RecipeReactiveRepository recipeReactiveRepository;
    private final RecipeCommandToRecipe recipeCommandToRecipe;
    private final RecipeToRecipeCommand recipeToRecipeCommand;

    public RecipeServiceImpl(RecipeReactiveRepository recipeReactiveRepository, RecipeCommandToRecipe recipeCommandToRecipe, RecipeToRecipeCommand recipeToRecipeCommand) {
        this.recipeReactiveRepository = recipeReactiveRepository;
        this.recipeCommandToRecipe = recipeCommandToRecipe;
        this.recipeToRecipeCommand = recipeToRecipeCommand;
    }

    @Override
    public Flux<Recipe> getRecipes() {
        log.debug("I'm in the service");

        Flux<Recipe> recipesFlux = recipeReactiveRepository.findAll();
        return recipesFlux;
    }

    @Override
    public Mono<Recipe> findById(String id) {
        Mono<Recipe> recipeMono = recipeReactiveRepository.findById(id);
        return recipeMono;
    }

    @Override
    @Transactional
    public Mono<RecipeCommand> findCommandById(String id) {

        Mono<RecipeCommand> recipeCommandMono = findById(id)
                .map(recipeToRecipeCommand::convert)
                .map(recipeCommand -> {
                    if (recipeCommand.getIngredients() != null && !recipeCommand.getIngredients().isEmpty()) {
                        recipeCommand.getIngredients().forEach(rc -> {
                            rc.setRecipeId(recipeCommand.getId());
                        });
                    }
                    return recipeCommand;
                });

        return recipeCommandMono;
    }

    @Override
    @Transactional
    public Mono<RecipeCommand> saveRecipeCommand(RecipeCommand command) {
        Recipe detachedRecipe = recipeCommandToRecipe.convert(command);

        Mono<RecipeCommand> savedRecipeCommandMono = recipeReactiveRepository.save(detachedRecipe)
                .map(savedRecipe -> {
                    //log.debug("Saved RecipeId:" + savedRecipe.getId());
                    return recipeToRecipeCommand.convert(savedRecipe);
                });
        return savedRecipeCommandMono;
    }

    @Override
    public void deleteById(String idToDelete) {
        recipeReactiveRepository.deleteById(idToDelete);
    }
}
