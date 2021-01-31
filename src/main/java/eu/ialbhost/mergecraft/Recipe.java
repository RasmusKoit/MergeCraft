package eu.ialbhost.mergecraft;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Recipe {
    private final HashMap<String, Material> recipes;
    private final List<String> mergeAmounts;
    private final MergeCraft plugin;

    public Recipe(MergeCraft plugin) {
        this.plugin = plugin;
        this.recipes = new HashMap<>();
        this.mergeAmounts = new ArrayList<>();
        loadRecipes();
    }

    public void addRecipe(String key, Material value) {
        recipes.put(key, value);
    }

    public void loadRecipes() {
        FileConfiguration recipeConfig = plugin.getRecipesConfig();
        List<String> recList = recipeConfig.getStringList("recipes");
        mergeAmounts.addAll(recipeConfig.getStringList("amounts"));

        if (!recList.isEmpty()) {
            for (String rec : recList) {
                String[] splitByEqualsSign = rec.split("=");
                addRecipe(splitByEqualsSign[0], Material.valueOf(splitByEqualsSign[1]));
            }
        }
    }

    public Material getRecipe(String key) {
        return recipes.get(key);
    }

    // TODO parameter not used
    public double getMaterialExp(Material material) {
        return 1.0;
    }

    public boolean containsRecipe(String recipe) {
        return recipes.containsKey(recipe);
    }

    public List<String> getMergeAmounts() {
        return mergeAmounts;
    }
}
