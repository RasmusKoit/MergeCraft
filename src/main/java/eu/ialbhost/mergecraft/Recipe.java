package eu.ialbhost.mergecraft;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;

public class Recipe {
    private HashMap<String, Material> recipes;
    private final MergeCraft plugin;

    public Recipe(MergeCraft plugin) {
        this.plugin = plugin;
        loadRecipes();
    }

    public void addRecipe(String key, Material value) {
        this.recipes.put(key, value);
    }


    public void loadRecipes() {
        FileConfiguration recipeConfig = plugin.getRecipesConfig();
        List<String> recipeList =  recipeConfig.getStringList("recipes");
        for (String recipe : recipeList){
            String key = recipe.split("=")[0];
            Material value = Material.valueOf(recipe.split("=")[1]);
            addRecipe(key, value);
        }
    }

    public HashMap<String, Material> getRecipes() {
        return recipes;
    }

    public boolean containsRecipe(String recipe) {
        return recipes.containsKey(recipe);
    }





}
