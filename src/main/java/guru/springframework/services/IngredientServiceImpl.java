package guru.springframework.services;

import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import guru.springframework.commands.IngredientCommand;
import guru.springframework.converters.IngredientCommandToIngredient;
import guru.springframework.converters.IngredientToIngredientCommand;
import guru.springframework.domain.Ingredient;
import guru.springframework.domain.Recipe;
import guru.springframework.domain.UnitOfMeasure;
import guru.springframework.repositories.RecipeRepository;
import guru.springframework.repositories.UnitOfMeasureRepository;
import guru.springframework.repositories.reactive.RecipeReactiveRepository;
import guru.springframework.repositories.reactive.UnitOfMeasureReactiveRepository;
import guru.springframework.utils.RecipeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Created by jt on 6/28/17.
 */
@Slf4j
@Service
public class IngredientServiceImpl implements IngredientService {

    private final IngredientToIngredientCommand ingredientToIngredientCommand;
    private final IngredientCommandToIngredient ingredientCommandToIngredient;
    private final RecipeReactiveRepository recipeReactiveRepository;
    private final UnitOfMeasureReactiveRepository unitOfMeasureReactiveRepository;

    public IngredientServiceImpl(IngredientToIngredientCommand ingredientToIngredientCommand,
                                 IngredientCommandToIngredient ingredientCommandToIngredient,
                                 RecipeReactiveRepository recipeReactiveRepository,
                                 UnitOfMeasureReactiveRepository unitOfMeasureReactiveRepository) {
        this.ingredientToIngredientCommand = ingredientToIngredientCommand;
        this.ingredientCommandToIngredient = ingredientCommandToIngredient;
        this.recipeReactiveRepository = recipeReactiveRepository;
        this.unitOfMeasureReactiveRepository = unitOfMeasureReactiveRepository;
    }

    @Override
    public Mono<IngredientCommand> findByRecipeIdAndIngredientId(String recipeId, String ingredientId) {

        Mono<Recipe> recipeMono = recipeReactiveRepository.findById(recipeId);
        Mono<IngredientCommand> ingredientCommandMono = getIngredientCommandMono(recipeMono, recipeId, ingredientId);
        /*
        Mono<IngredientCommand> IngredientCommandMono = recipeMono.map(recipe -> {
            return recipe
                    .getIngredients()
                    .stream()
                    .filter(ing -> ing.getId().equals(ingredientId))
                    .map( ingredient -> ingredientToIngredientCommand.convert(ingredient))
                    .findFirst()
                    .orElseGet(IngredientCommand::new);
        }).switchIfEmpty(Mono.defer(() -> {
            log.error("recipe id not found. Id: " + recipeId);
            return Mono.just(new IngredientCommand());
        }));*/

        //ingredientCommand.setRecipeId(recipe.getId());

        return ingredientCommandMono;
    }

    @Override
    public Mono<IngredientCommand> saveIngredientCommand(IngredientCommand command) {
        Mono<Recipe> recipeMono = recipeReactiveRepository.findById(command.getRecipeId());
        Mono<Ingredient> ingredientMono = getNeededIngredient(recipeMono, command);
        Mono<UnitOfMeasure> unitOfMeasureMono = unitOfMeasureReactiveRepository.findById(command.getUom().getId());

        Ingredient ingredientWithNewParameters = setNewParametersForIngredient(ingredientMono, command, unitOfMeasureMono);

        Recipe recipe = recipeMono.block();

        RecipeUtils.replaceIngredient(recipe, ingredientWithNewParameters);
        Mono<Recipe> savedRecipeMono = recipeReactiveRepository.save(recipe);
        Mono<IngredientCommand> savedIngredientCommandMono = getIngredientCommandMono(savedRecipeMono, command.getRecipeId(), ingredientWithNewParameters.getId());

        return savedIngredientCommandMono;
    }

    @Override
    public Mono<IngredientCommand> updateIngredientCommand(IngredientCommand command) {
        return null;
    }

    public Mono<Ingredient> getNeededIngredient(Mono<Recipe> recipeMono, IngredientCommand command) {
        Mono<Ingredient> ingredientMono = recipeMono.map(recipe -> {
            return recipe
                    .getIngredients()
                    .stream()
                    .filter(ing -> ing.getId().equals(command.getId()))
                    .findFirst()
                    .orElseGet(Ingredient::new);
        }).switchIfEmpty(Mono.defer(() -> {
            log.error("recipe id not found. Id: " + command.getRecipeId());
            return Mono.just(new Ingredient());
        }));
        return ingredientMono;
    }

    public Ingredient setNewParametersForIngredient(Mono<Ingredient> ingredientMono,
                                                    IngredientCommand command,
                                                    Mono<UnitOfMeasure> unitOfMeasureMono) {
        UnitOfMeasure uom = unitOfMeasureMono.block();
        Ingredient ingredientFound = ingredientMono.block();
        ingredientFound.setDescription(command.getDescription());
        ingredientFound.setAmount(command.getAmount());
        ingredientFound.setUom(uom);
        return ingredientFound;
    }

    public Mono<IngredientCommand> getIngredientCommandMono(Mono<Recipe> recipeMono, String recipeId, String ingredientId) {
        Mono<IngredientCommand> ingredientCommandMono = recipeMono.map(recipe -> {
            IngredientCommand command = recipe
                    .getIngredients()
                    .stream()
                    .filter(ing -> ing.getId().equals(ingredientId))
                    .map(ingredient -> ingredientToIngredientCommand.convert(ingredient))
                    .findFirst()
                    .orElseGet(IngredientCommand::new);

            command.setRecipeId(recipe.getId());
            return command;
        }).switchIfEmpty(Mono.defer(() -> {
            log.error("recipe id not found. Id: " + recipeId);
            IngredientCommand errorCommand = new IngredientCommand();
            errorCommand.setRecipeId(recipeId);
            return Mono.just(errorCommand);
        }));

        return ingredientCommandMono;
    }

    @Override
    public void deleteById(String recipeId, String ingredientIdToDelete) {

        log.debug("Deleting ingredient: " + recipeId + ":" + ingredientIdToDelete);

        Mono<Recipe> recipeMono = recipeReactiveRepository.findById(recipeId);
        Recipe recipeWithoutIngredient = recipeMono
                .map(recipe -> {
                    boolean removed = recipe.getIngredients().removeIf(ing -> ing.getId().equals(ingredientIdToDelete));
                    return recipe;
                })
                .flatMap(recipeReactiveRepository::save) // 2. Зберігаємо оновлений рецепт у MongoDB
                .block();
        //System.out.println();
    }
}
