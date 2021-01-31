package eu.ialbhost.mergecraft;

public class Experience {
    /*
    y = 100*float(x)**1.765776
    experience needed for level = (100*level)**
    */
    private final static double LEVEL_CONSTANT = 1.765776;
    private final static double MAX_LEVEL = 50.0;

    public Experience() {
    }

    public double calculateExpEarned(User user, Double matExp, Integer count) {
        double expGained;
        expGained = ((matExp * count) * (user.getMultiplier() + 0.01 * count));
        user.addExperience(expGained);
        return expGained;
    }

    public double calcExperienceNeeded(Double level) {
        if (level <= MAX_LEVEL) {
            return (100 * Math.pow(level, LEVEL_CONSTANT));
        } else {
            return 0.0;
        }
    }

    public Double getMaxLevel() {
        return MAX_LEVEL;
    }
}
