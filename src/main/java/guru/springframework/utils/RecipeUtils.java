package guru.springframework.utils;

import guru.springframework.domain.Ingredient;
import guru.springframework.domain.Recipe;

import java.util.Optional;
import java.util.Set;

public class RecipeUtils {

    public static Recipe replaceIngredient(Recipe recipe, Ingredient ingredientForReplacement) {
        Set<Ingredient> ingredients = recipe.getIngredients();
        Optional<Ingredient> oldIngredientOpt = ingredients.stream()
                .filter(ing -> ing.getId().equals(ingredientForReplacement.getId()))
                .findFirst();
        if (oldIngredientOpt.isPresent()) {
            ingredients.remove(oldIngredientOpt.get());
        }
        ingredients.add(ingredientForReplacement);
        return recipe;
    }
}
