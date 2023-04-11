package levelGenerators.template;

import engine.core.MarioLevelGenerator;
import engine.core.MarioLevelModel;
import engine.core.MarioTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static engine.core.MarioLevelModel.*;

public class LevelGenerator implements MarioLevelGenerator {
    private final int FLOOR_PADDING = 3;
    abstract class Template {
        // x and y are starting positions in the left upper corner of the template
        public int x;
        public int y;
        public int width;
        public int height;
        public int level;
        // slots are used to save the free spaces in the current template
        ArrayList<Integer> slots = new ArrayList<Integer>();
        // generate the content for the current template and fill in the slots if possible
        abstract void generate(MarioLevelModel model, ArrayList<Integer> slots);
        // if it's not convenient to generate free spaces in the generate function,
        // searchSlots is used to search free spaces after the current template is filled(not used in the current project)
        abstract void searchSlots();
        // select the free spaces for the next level template and call the next level templates to fill the free spaces
        abstract void execute(MarioLevelModel model, ArrayList<Integer> slots);


        Template(int x,int y, int width,int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    class ContinuousHill extends Template {
        public ContinuousHill(int x, int y, int width, int height) {
            super(x, y, width, height);
            this.level = 1;
        }
        // set ground from currentHeight to the maxHeight
        void setGround(MarioLevelModel model, int x, int currentHeight, int maxHeight){
            for (int i = currentHeight; i <= maxHeight; i++){
                model.setBlock(x, i, MarioLevelModel.GROUND);
            }
        }
        void generate(MarioLevelModel model, ArrayList<Integer> slots) {
            int maxHeight = model.getHeight() - 1;
            Random rnd = new Random();
            int from = maxHeight - rnd.nextInt(3);
            setGround(model, 0, from, maxHeight);
            for (int x = this.x; x < this.x + this.width; x++) {
                // keep the height of the ContinuousHill in the range [maxHeight -4, maxHeight]
                int sign = rnd.nextBoolean() == true ? 1 : -1;
                int previusFrom = from;
                from += sign * rnd.nextInt(3);
                if (from > maxHeight){
                    from = maxHeight;
                }
                else if (from <= maxHeight - 5){
                    from = previusFrom;
                }
                //record the free spaces in the current template
                // x, y, width, height
                this.slots.add(x);
                this.slots.add(0);
                this.slots.add(1);
                this.slots.add(from);
                setGround(model, x, from, maxHeight);
            }
        }
        void execute(MarioLevelModel model, ArrayList<Integer> slots) {
            this.generate(model, slots);

            Random rnd = new Random();
            int numberOfSubtemplateToBeUsed = 2 + rnd.nextInt(3);
            int widthOfSubtemplate = this.width / numberOfSubtemplateToBeUsed;

            ArrayList<Template> list = new ArrayList<Template>();
            for (int i = 0; i < numberOfSubtemplateToBeUsed; i++){
                // height is -1 means the value here does not matter, because the next template height is undetermined, and will be determined randomly based on the values of slots of the current template
                Template temp = randomTemplate(this.level + 1, this.x + i * widthOfSubtemplate + widthOfSubtemplate / 2, 0, widthOfSubtemplate / 3, -1);
                list.add(temp);
            }
            // see the comment on line 86
            for(Template template: list){
                template.execute(model, this.slots);
            }
        }
        void searchSlots() {
        }
    }


    class MildGap extends Template {
        public MildGap(int x, int y, int width, int height) {
            super(x, y, width, height);
            this.level = 1;
        }
        int totalGroundLength = 0;
        int numberOfGrounds = 0;

        void generate(MarioLevelModel model, ArrayList<Integer> slots) {
            int maxHeight = model.getHeight();
            Random rnd = new Random();
            int groundMaxLength = 10;
            int gapMaxLength = 4;
            int groundLength = groundMaxLength / 2 + rnd.nextInt(groundMaxLength / 2);
            int gapLength = 0;
            totalGroundLength = groundLength;
            numberOfGrounds += 1;

            for (int x = this.x; x < this.x + this.width; x++) {
                if (groundLength > 0){
                    model.setBlock(x, model.getHeight() - 1, MarioLevelModel.GROUND);
                    model.setBlock(x, model.getHeight() - 2, MarioLevelModel.GROUND);

                    //record the free spaces in the current template; only spaces above the ground(opposite to gap) is recorded
                    // x, y, width, height
                    this.slots.add(x);
                    this.slots.add(0);
                    this.slots.add(groundLength);
                    this.slots.add(maxHeight - 2);

                    groundLength -= 1;
                    if (groundLength <= 0) {
                        gapLength = gapMaxLength / 2 + rnd.nextInt(gapMaxLength / 2);
                    }
                }
                else {
                    //record the free spaces in the current template
                    //x, y, width, height
                    // -1 here means gap
                    this.slots.add(x);
                    this.slots.add(0);
                    this.slots.add(gapLength);
                    this.slots.add(-1);

                    gapLength -= 1;
                    if (gapLength <= 0) {
                        groundLength = groundMaxLength / 2 + rnd.nextInt(groundMaxLength / 2);
                        totalGroundLength += groundLength;
                        numberOfGrounds += 1;
                    }
                }
            }
        }
        void searchSlots() {
        }
        void execute(MarioLevelModel model, ArrayList<Integer> slots) {
            this.generate(model, slots);

            Random rnd = new Random();
            int numberOfSubtemplateToBeUsed = 1 + rnd.nextInt(3);
            int widthOfSubtemplate = totalGroundLength / numberOfGrounds / 2;

            ArrayList<Template> list = new ArrayList<Template>();
            // j is used to mark the current horizontal position in the map
            int j = 0;
            for (int i = 0; i < numberOfSubtemplateToBeUsed; i++){
                    while(j < this.width){
                        // if the current land is not gap and if the width of the current ground is enough to hold the width of the subtemplate
                        if (this.slots.get(j * 4 + 3) != -1 && this.slots.get(j * 4 + 2) >= widthOfSubtemplate){
                            int x = this.x + j + this.slots.get(j * 4 + 2) / 2;
                            int width = this.slots.get(j * 4 + 2) / 3;
                            Template temp = randomTemplate(this.level + 1, x, 0, width, -1);
                            list.add(temp);
                            // move j to the next ground(opposite to gap)
                            j += this.slots.get(j * 4 + 2);
                            j += this.slots.get(j * 4 + 2);
                            break;
                        }
                        else{
                            // move j to the next ground(opposite to gap)
                            j += this.slots.get(j * 4 + 2);
                            j += this.slots.get(j * 4 + 2);
                        }
                    }
            }
            for(Template template: list){
                template.execute(model, this.slots);
            }
        }
    }

    class GroundSandwichPattern extends Template {
        public GroundSandwichPattern(int x, int y, int width, int height) {
            super(x, y, width, height);
            this.level = 2;
        }
        // set a pillar in the range at horizontal position x and [from, to] in y
        void setPillar(MarioLevelModel model, int x, int from, int to, char type){
            for (int i = from; i <= to; i++){
                model.setBlock(x, i, type);
            }
        }
        void generate(MarioLevelModel model, ArrayList<Integer> slots) {
            Random rnd = new Random();
            int leftPIllarX = this.x - this.width / 2;
            int rightPIllarX = this.x + this.width / 2;
            // because width of level 1 template is 50
            Integer leftPillarHeight = slots.get((leftPIllarX % 50) * 4 + 3) / 4;
            Integer rightPillarHeight = slots.get((rightPIllarX % 50) * 4 + 3) / 4;
            setPillar(model, leftPIllarX, slots.get((leftPIllarX % 50) * 4 + 3) - 1 - (leftPillarHeight - 1),slots.get((leftPIllarX % 50) * 4 + 3) - 1, PIPE);
            setPillar(model, rightPIllarX, slots.get((rightPIllarX % 50) * 4 + 3) - 1 - (rightPillarHeight - 1), slots.get((rightPIllarX % 50) * 4 + 3) - 1, PIPE);

            // record the positions just on top of the current template; only current x position and the height is recorded
            this.slots.add(leftPIllarX);
            this.slots.add(slots.get((leftPIllarX % 50) * 4 + 3) - 1 - (leftPillarHeight - 1) - 1);

            for (int i = leftPIllarX + 1; i < rightPIllarX; i++){
                this.slots.add(i);
                this.slots.add(slots.get((i % 50) * 4 + 3) - 1);
            }
            this.slots.add(rightPIllarX);
            this.slots.add(slots.get((rightPIllarX % 50) * 4 + 3) - 1 - (rightPillarHeight - 1) - 1);
        }
        public void execute(MarioLevelModel model, ArrayList<Integer> slots){
            this.generate(model, slots);
            Random rnd = new Random();
            int numberOfSubtemplateToBeUsed = 1 + rnd.nextInt(Math.max(1, this.width / 2));
            double enemyProbability = 0.5;
            ArrayList<Template> list = new ArrayList<Template>();
            for (int i = 0; i < numberOfSubtemplateToBeUsed; i++){
                if (rnd.nextDouble() <= enemyProbability){
                    Template temp = randomTemplate(this.level + 1, this.slots.get(i * 2), this.slots.get(i * 2 + 1), 1, 1);
                    list.add(temp);
                }
            }
            for(Template template: list){
                template.execute(model, this.slots);
            }
        };
        void searchSlots() {
        }
    }

    class EnemyPattern extends Template {

        public EnemyPattern(int x, int y, int width, int height) {
            super(x, y, width, height);
            this.level = 3;
        }
        void generate(MarioLevelModel model, ArrayList<Integer> slots) {
            Random rnd = new Random();
            boolean winged = rnd.nextDouble() < 0.1;
            char enemyWithin = MarioLevelModel.getEnemyCharacters(false)[rnd.nextInt(MarioLevelModel.getEnemyCharacters(false).length)];
            enemyWithin = MarioLevelModel.getWingedEnemyVersion(enemyWithin, winged);
            model.setBlock(this.x, this.y, enemyWithin);
        }
        public void execute(MarioLevelModel model, ArrayList<Integer> slots){
            // since there's only one line of code, this will represent the end of the templating
            this.generate(model, slots);
        };
        void searchSlots() {
        }
    }



    // function used to connect all the templates together
    public Template randomTemplate(int level, int x, int y, int width, int height){
        Random rnd = new Random();
        int templateNumber = rnd.nextInt(2);
        Template result = null;
        if (level == 1){
            switch (templateNumber) {
                case 0:
                    result = new ContinuousHill(x, y, width, height);
                    break;
                case 1:
                    result = new MildGap(x, y, width, height);
                    break;
            }
        }
        else if (level == 2){
            switch (templateNumber) {
                case 0:
                    result = new GroundSandwichPattern(x, y, width, height);
                    break;
                case 1:
                    //fake random option
                    result = new GroundSandwichPattern(x, y, width, height);
                    break;
            }
        }
        else if (level == 3){
            switch (templateNumber) {
                case 0:
                    result = new EnemyPattern(x, y, width, height);
                    break;
                case 1:
                    //fake random option
                    result = new EnemyPattern(x, y, width, height);
                    break;
            }
        }
        return result;
    }



    @Override
    public String getGeneratedLevel(MarioLevelModel model, MarioTimer timer) {
        model.clearMap();
        int templateWidth = model.getWidth() / 3;
        int templateHeight = model.getHeight();
        ArrayList<Template> list = new ArrayList<Template>();
        for (int i = 0; i < 3; i++){
            Template temp = randomTemplate(1, i * templateWidth, 0, templateWidth, templateHeight);
            list.add(temp);
        }
        for(Template template: list){
            template.execute(model, null);
        }

        // padding the start and end of the map and set our main character and the goal
        model.setRectangle(0, 13, FLOOR_PADDING, 2, GROUND);
        model.setRectangle(model.getWidth() - 1 - FLOOR_PADDING, 13, FLOOR_PADDING, 2, GROUND);
        model.setBlock(FLOOR_PADDING / 2, 8, MARIO_START);
        model.setBlock(model.getWidth() - 1 - FLOOR_PADDING / 2, 13, MARIO_EXIT);
        return model.getMap();
    }

    @Override
    public String getGeneratorName() {
        return "RandomLevelGenerator";
    }

}
