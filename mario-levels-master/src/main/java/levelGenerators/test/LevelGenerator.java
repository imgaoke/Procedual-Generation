package levelGenerators.test;

import engine.core.MarioLevelGenerator;
import engine.core.MarioLevelModel;
import engine.core.MarioTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static engine.core.MarioLevelModel.*;

public class LevelGenerator implements MarioLevelGenerator {
    private final int GROUND_Y_LOCATION = 13;
    private final float GROUND_PROB = 0.4f;
    private final int OBSTACLES_LOCATION = 10;
    private final float OBSTACLES_PROB = 0.1f;
    private final int COLLECTIBLE_LOCATION = 3;
    private final float COLLECTIBLE_PROB = 0.05f;
    private final float ENMEY_PROB = 0.01f;
    private final int FLOOR_PADDING = 3;

    private Random rnd;

    abstract class Template {
        // starting position in the left upper corner of the template
        public int x;
        public int y;
        public int width;
        public int height;
        public int level;
        public boolean lastLevel;
        public List<Template> listOfNextLevelTemplate;
        //x, y, width, height
        ArrayList<Integer> slots = new ArrayList<Integer>();
        abstract void generate(MarioLevelModel model, ArrayList<Integer> slots);
        abstract void searchSlots();
        abstract void execute(MarioLevelModel model, ArrayList<Integer> slots);
        Template(int x,int y, int width,int height, boolean ll) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.lastLevel = ll;
        }
    }

    class ContinuousHill extends Template {
        public ContinuousHill(int x, int y, int width, int height) {
            super(x, y, width, height, false);
            this.level = 1;
        }

        void setGround(MarioLevelModel model, int x, int currentHeight, int maxHeight){
            for (int i = currentHeight; i <= maxHeight; i++){
                model.setBlock(x, i, MarioLevelModel.GROUND);
            }
        }
        @Override
        void generate(MarioLevelModel model, ArrayList<Integer> slots) {
            int maxHeight = model.getHeight() - 1;
            Random rnd = new Random();
            int from = maxHeight - rnd.nextInt(3);
            setGround(model, 0, from, maxHeight);
            for (int x = this.x; x < this.x + this.width; x++) {
                //System.out.println(x);
                int sign = rnd.nextBoolean() == true ? 1 : -1;
                int previusFrom = from;
                from += sign * rnd.nextInt(3);
                if (from > maxHeight){
                    from = maxHeight;
                }
                else if (from <= maxHeight - 5){
                    from = previusFrom;
                }
                //x, y, width, height
                this.slots.add(x);
                this.slots.add(0);
                this.slots.add(1);
                this.slots.add(from);
                //System.out.println(from + 1);
                setGround(model, x, from, maxHeight);
            }
            //System.out.println(this.slots.size());
        }
        @Override
        void execute(MarioLevelModel model, ArrayList<Integer> slots) {
            this.generate(model, slots);

            Random rnd = new Random();
            int numberOfSubtemplateToBeUsed = 2 + rnd.nextInt(3);
            int widthOfSubtemplate = this.width / numberOfSubtemplateToBeUsed;

            ArrayList<Template> list = new ArrayList<Template>();
            for (int i = 0; i < numberOfSubtemplateToBeUsed; i++){
                // height is -1 means the value here does not matter, real height should be found in the slots
                Template temp = randomTemplate(this.level + 1, this.x + i * widthOfSubtemplate + widthOfSubtemplate / 2, 0, widthOfSubtemplate / 3, -1);
                list.add(temp);
            }

            for(Template template: list){
                template.execute(model, this.slots);
            }


        }
        void searchSlots() {

        }
    }


    class MildGap extends Template {
        public MildGap(int x, int y, int width, int height) {
            super(x, y, width, height, false);
            this.level = 1;
        }
        int totalGroundLength;
        int numberOfGrounds;

        @Override
        void generate(MarioLevelModel model, ArrayList<Integer> slots) {
            int maxHeight = model.getHeight();
            Random rnd = new Random();
            int groundMaxLength = 10;
            int gapMaxLength = 4;
            int groundLength = groundMaxLength / 2 + rnd.nextInt(groundMaxLength / 2);
            int gapLength = 0;
            totalGroundLength = groundLength;
            numberOfGrounds += 1;

            double gapProbability = 0.3;
            //System.out.println(this.width);
            for (int x = this.x; x < this.x + this.width; x++) {
                if (groundLength > 0){
                    model.setBlock(x, model.getHeight() - 1, MarioLevelModel.GROUND);
                    model.setBlock(x, model.getHeight() - 2, MarioLevelModel.GROUND);

                    //x, y, width, height
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
            //System.out.println(this.slots.size());
            /*
            for (int i = 0; i < this.width; i++){
                if (this.slots.get(4 * i + 2) == 0){
                    System.out.println("bug?");
                }
                //System.out.println(this.slots.get(i));
            }
            */

        }

        @Override
        void searchSlots() {

        }

        @Override
        void execute(MarioLevelModel model, ArrayList<Integer> slots) {
            this.generate(model, slots);

            Random rnd = new Random();
            int numberOfSubtemplateToBeUsed = 1 + rnd.nextInt(3);
            int widthOfSubtemplate = totalGroundLength / numberOfGrounds / 2;

            ArrayList<Template> list = new ArrayList<Template>();
            int j = 0;
            //System.out.println("here0");
            //System.out.println(numberOfSubtemplateToBeUsed);
            //System.out.println(widthOfSubtemplate);
            for (int i = 0; i < numberOfSubtemplateToBeUsed; i++){
                    /*
                    if (j * 4 + 2 >= this.width * 4){
                        break;
                    }
                    */

                    //System.out.println(j * 4 + 2);
                    //System.out.println(slots.size());
                    //for (int k = 0; k < slots.size(); k ++){
                    //    System.out.println(slots.get(k));
                    //}
                    while(j < this.width){
                        /*
                        if (j * 4 + 2 >= this.width * 4){
                            break;
                        }

                         */
                        if (this.slots.get(j * 4 + 2) >= widthOfSubtemplate && this.slots.get(j * 4 + 3) != -1){
                            //System.out.println("here1");
                            int x = this.x + j + this.slots.get(j * 4 + 2) / 2;
                            int width = this.slots.get(j * 4 + 2) / 3;
                            //System.out.println();
                            //System.out.println(x);
                            //System.out.println(width);
                            Template temp = randomTemplate(this.level + 1, x, 0, width, -1);
                            list.add(temp);
                            //System.out.println(this.slots.get(j * 4 + 2));
                            j += this.slots.get(j * 4 + 2);
                            j += this.slots.get(j * 4 + 2);

                            /*
                            if (j >= this.width){
                                break;
                            }
                            if (this.slots.get(j * 4 + 3) != -1){
                                j += this.slots.get(j * 4 + 2);
                                if (j >= this.width){
                                    break;
                                }
                            }

                             */
                            break;
                        }
                        else{
                            //System.out.println("here2");
                            //System.out.println(j);
                            //System.out.println(this.slots.get(j * 4 + 2));
                            j += this.slots.get(j * 4 + 2);
                            j += this.slots.get(j * 4 + 2);
                            /*
                            if (j >= this.width || j * 4 + 3 > this.width * 4){
                                break;
                            }
                            if (this.slots.get(j * 4 + 3) != -1){
                                j += this.slots.get(j * 4 + 2);
                                if (j >= this.width){
                                    break;
                                }
                            }

                             */
                        }
                    }



            }

            for(Template template: list){
                //System.out.println("here0");
                template.execute(model, this.slots);
            }
        }
    }

    interface GroundPatternTemplate{
        public void execute(int startX);
    }


    class GroundSandwichPattern extends Template {

        public GroundSandwichPattern(int x, int y, int width, int height) {
            super(x, y, width, height, true);
            this.level = 2;
        }

        void setPillar(MarioLevelModel model, int x, int from, int to, char type){
            for (int i = from; i <= to; i++){
                model.setBlock(x, i, type);
            }
        }

        void generate(MarioLevelModel model, ArrayList<Integer> slots) {
            Random rnd = new Random();
            char leftPillar = getBumpableTiles()[rnd.nextInt(getBumpableTiles().length)];
            char rightPillar = getBumpableTiles()[rnd.nextInt(getBumpableTiles().length)];
            //System.out.println(this.width / 2);
            int leftPIllarX = this.x - this.width / 2;
            int rightPIllarX = this.x + this.width / 2;
            //this.width / 2();
            //System.out.println(leftPIllarX);
            //System.out.println(rightPIllarX);
            Integer leftPillarHeight = slots.get((leftPIllarX % 50) * 4 + 3) / 4;
            Integer rightPillarHeight = slots.get((rightPIllarX % 50) * 4 + 3) / 4;
            //System.out.println();
            //System.out.println(leftPillarHeight);
            //System.out.println(rightPillarHeight);
            setPillar(model, leftPIllarX, slots.get((leftPIllarX % 50) * 4 + 3) - 1 - (leftPillarHeight - 1),slots.get((leftPIllarX % 50) * 4 + 3) - 1, PIPE);
            setPillar(model, rightPIllarX, slots.get((rightPIllarX % 50) * 4 + 3) - 1 - (rightPillarHeight - 1), slots.get((rightPIllarX % 50) * 4 + 3) - 1, PIPE);
            //System.out.println();
            //System.out.println(slots.get((leftPIllarX % 50) * 4 + 3) - 1);
            //System.out.println(slots.get((leftPIllarX % 50) * 4 + 3) - 1 - (leftPillarHeight - 1));

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
            //int tempp = this.width / 2;

            //System.out.println(tempp);
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

        @Override
        void searchSlots() {

        }

    }

    class EnemyPattern extends Template {

        public EnemyPattern(int x, int y, int width, int height) {
            super(x, y, width, height, true);
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
            this.generate(model, slots);

        };

        @Override
        void searchSlots() {

        }

    }





    public Template randomTemplate(int level, int x, int y, int width, int height){
        Random rnd = new Random();
        int templateNumber = rnd.nextInt(2);
        //int templateNumber = 1;
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
                    //placeholder
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
                    //placeholder
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

        /*
        Random random = new Random();
        model.clearMap();
        for (int x = 0; x < model.getWidth(); x++) {
            for (int y = 0; y < model.getHeight(); y++) {
                model.setBlock(x, y, EMPTY);
                if (y > GROUND_Y_LOCATION){
                    model.setBlock(x, y, PLATFORM);
                }else if (y > OBSTACLES_LOCATION) {
                    if (random.nextDouble() < ENMEY_PROB) {
                        model.setBlock(x, y, SPIKY_WINGED);
                    }
                }else if (y > COLLECTIBLE_LOCATION) {
                    if (random.nextDouble() < COLLECTIBLE_PROB) {
                        model.setBlock(x, y, LIFE_HIDDEN_BLOCK);
                    }
                }
            }
        }
        */
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
