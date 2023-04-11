package metrics;

import engine.core.*;
import engine.helper.EventType;
import org.jetbrains.annotations.NotNull;

public class MyMetricJava extends AbstractMetric {
    @NotNull
    @Override
    public String getName() {
        return "leniency";
    }

    @NotNull
    @Override
    public String getValue(@NotNull String level) {

        MarioResult results = runBaumgartenAgent(level);

        MarioWorld world = new MarioWorld(new MarioEvent[] {});
        world.initializeLevel(level, 200);
        int numberOfEnemies = 0;
        for (int i = 0; i < level.length(); i++){
            for (int j = 0; j < MarioLevelModel.getEnemyCharacters().length; j++){
                if (level.charAt(i) == MarioLevelModel.getEnemyCharacters()[j]){
                    numberOfEnemies += 1;
                    break;
                }
            }

        }
        //int numberOfEnemies = world.getEnemies().size();
        int numberOfEnemiesKilled = results.getKillsTotal();
        double leniencyPart1 = (double)numberOfEnemiesKilled / (double)numberOfEnemies;

        //int numberOfHurts = results.getMarioNumHurts();
        //int numberOfJumps = results.getNumJumps();
        //double leniencyPart2 = (double)numberOfHurts / (double)numberOfJumps;

        double maxXJump = results.getMaxXJump();
        int maxXWidth = world.level.width;
        double leniencyPart3 = maxXJump / (double)maxXWidth * 10;
        //System.out.println(leniencyPart3);
        double leniency = leniencyPart1 + leniencyPart3;

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