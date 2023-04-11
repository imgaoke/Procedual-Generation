package metrics;

import engine.core.*;
import org.jetbrains.annotations.NotNull;

public class LeniencyMetricJava extends AbstractMetric {
    @NotNull
    @Override
    public String getName() {
        return "leniency";
    }

    @NotNull
    @Override
    public String getValue(@NotNull String level) {

        MarioResult results = runBaumgartenAgent(level);

        int numberOfEnemies = 0;
        for (int i = 0; i < level.length(); i++){
            for (int j = 0; j < MarioLevelModel.getEnemyCharacters().length; j++){
                if (level.charAt(i) == MarioLevelModel.getEnemyCharacters()[j]){
                    numberOfEnemies += 1;
                    break;
                }
            }

        }
        int numberOfEnemiesKilled = results.getKillsTotal();
        double leniencyPart1 = (double)numberOfEnemiesKilled / (double)numberOfEnemies;

        MarioWorld world = new MarioWorld(new MarioEvent[] {});
        world.initializeLevel(level, 200);
        double maxXJump = results.getMaxXJump();
        int maxXWidth = world.level.width;
        double leniencyPart2 = maxXJump / (double)maxXWidth * 10;

        double leniency = leniencyPart1 + leniencyPart2;

        return Double.toString(leniency);
    }

    private MarioResult runBaumgartenAgent(String level) {
        MarioGame game = new MarioGame();
        return game.runGame(
            new agents.robinBaumgarten.Agent(),
            level,
            200,
            0
        );
    }
}