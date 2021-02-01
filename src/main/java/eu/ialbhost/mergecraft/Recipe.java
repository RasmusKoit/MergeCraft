package eu.ialbhost.mergecraft;

import org.bukkit.Material;

public class Recipe {
    private final Material merge_from;
    private final Material merge_to;
    private final Double exp;

    public Recipe(Material material, Material mergeTo, Double exp) {
        this.merge_from = material;
        this.merge_to = mergeTo;
        this.exp = exp;
    }


    public Material getMerge_from() {
        return merge_from;
    }

    public Material getMerge_to() {
        return merge_to;
    }

    public Double getExp() {
        return exp;
    }


}
