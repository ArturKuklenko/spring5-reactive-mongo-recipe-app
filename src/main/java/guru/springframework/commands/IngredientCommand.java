package guru.springframework.commands;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Created by jt on 6/21/17.
 */
@Getter
@Setter
@NoArgsConstructor
public class IngredientCommand {
    private String id;
    private String recipeId;
    @NotBlank(message = "Description is required")
    private String description;
    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be at least 1")
    private BigDecimal amount;
    @NotNull(message = "Unit of Measure is required")
    @Valid
    private UnitOfMeasureCommand uom;
}
