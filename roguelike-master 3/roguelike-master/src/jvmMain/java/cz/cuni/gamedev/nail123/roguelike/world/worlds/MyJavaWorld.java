package cz.cuni.gamedev.nail123.roguelike.world.worlds;

import cz.cuni.gamedev.nail123.roguelike.blocks.*;
import cz.cuni.gamedev.nail123.roguelike.entities.enemies.Rat;
import cz.cuni.gamedev.nail123.roguelike.entities.objects.Stairs;
import cz.cuni.gamedev.nail123.roguelike.events.LoggedEvent;
import cz.cuni.gamedev.nail123.roguelike.mechanics.Pathfinding;
import cz.cuni.gamedev.nail123.roguelike.world.Area;
import cz.cuni.gamedev.nail123.roguelike.world.World;
import cz.cuni.gamedev.nail123.roguelike.world.builders.AreaBuilder;
import cz.cuni.gamedev.nail123.roguelike.world.builders.EmptyAreaBuilder;
import org.hexworks.zircon.api.data.Position3D;
import org.hexworks.zircon.api.data.Size3D;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MyJavaWorld extends World {
    int currentLevel = 0;

    public MyJavaWorld() {
    }

    @NotNull
    @Override
    public Area buildStartingArea() {
        return buildLevel();
    }

    public class Room{
        Position3D center;
        int halfWidth; // halfWidth and halfHeight does not include the middle floor of the room, the wall is included in width or height so the smallest value is 1
        int halfHeight;
        int wallNumber; // 1 = up, 2 = down, 3 = right, 4 = left
        int brickNumber;
        int componentNumber;

        public Room(Position3D center, int halfWidth, int halfHeight, int wallNumber, int brickNumber, int componentNumber){
            this.center = center;
            this.halfWidth = halfWidth;
            this.halfHeight = halfHeight;
            this.wallNumber = wallNumber;
            this.brickNumber = brickNumber;
            this.componentNumber = componentNumber;
        }

        public Room createRandomRoom(AreaBuilder areaBuilder){
            // minHalfWidth and minHalfHeight are set to 2 to make the room not like a corridor
            int minHalfWidth = 2;
            int maxHalfWidth = areaBuilder.getWidth() / 10;
            int minHalfHeight = 2;
            int maxHalfHeight = areaBuilder.getHeight() / 10;
            int minCenterX = 2;
            int maxCenterX = areaBuilder.getWidth() - 2;
            int minCenterY = 2;
            int maxCenterY = areaBuilder.getHeight() - 2;

            Random random = new Random();
            int halfWidth = random.nextInt(maxHalfWidth + 1 - minHalfWidth) + minHalfWidth;
            int halfHeight = random.nextInt(maxHalfHeight + 1 - minHalfHeight) + minHalfHeight;
            int centerX = random.nextInt(maxCenterX + 1 - minCenterX) + minCenterX;
            int centerY = random.nextInt(maxCenterY + 1 - minCenterY) + minCenterY;
            Position3D center = Position3D.create(centerX, centerY, 0);
            int wallNumber = random.nextInt(4) + 1;
            int brickNumber;
            if (wallNumber == 1 || wallNumber == 2){
                brickNumber = random.nextInt(2 * halfWidth - 1) - (halfWidth - 1);
            }
            else{
                brickNumber = random.nextInt(2 * halfHeight - 1) - (halfHeight - 1);
            }

            return new Room(center, halfWidth, halfHeight, wallNumber, brickNumber, 0);

        }
    }

    List<Room> rooms;

    boolean[][] corridorAvailability;
    boolean[][] corridorFit;

    boolean AABBRoomCollisionDetection(Room a, Room b){
        int aMinX = a.center.getX() - a.halfWidth;
        int aMaxX = a.center.getX() + a.halfWidth;
        int bMinX = b.center.getX() - b.halfWidth;
        int bMaxX = b.center.getX() + b.halfWidth;

        int aMinY = a.center.getY() - a.halfHeight;
        int aMaxY = a.center.getY() + a.halfHeight;
        int bMinY = b.center.getY() - b.halfHeight;
        int bMaxY = b.center.getY() + b.halfHeight;

        int d1x = bMinX - aMaxX;
        int d1y = bMinY - aMaxY;
        int d2x = aMinX - bMaxX;
        int d2y = aMinY - bMaxY;

        // 1 is set for d1x, d1y, d2x, d2y such that rooms are at least 1 distance from each other(if == 1, the near walls of two rooms is next to each other)
        if (d1x > 1 || d1y > 1) return false;
        if (d2x > 1 || d2y > 1) return false;
        return true;
    }

    void createRoom(Room room, AreaBuilder areaBuilder){
        int centerX = room.center.getX();
        int centerY = room.center.getY();
        for (int i = -room.halfWidth; i <= room.halfWidth; i++){
            areaBuilder.getBlocks().put(Position3D.create(centerX + i, centerY - room.halfHeight, 0), new Wall());
            areaBuilder.getBlocks().put(Position3D.create(centerX + i, centerY + room.halfHeight, 0), new Wall());
        }

        for (int i = -room.halfHeight; i < room.halfHeight; i++){
            areaBuilder.getBlocks().put(Position3D.create(centerX - room.halfWidth, centerY + i, 0), new Wall());
            areaBuilder.getBlocks().put(Position3D.create(centerX + room.halfWidth, centerY + i, 0), new Wall());
        }

        for (int i = -room.halfWidth; i <= room.halfWidth; i++){
            for (int j = -room.halfHeight; j <= room.halfHeight; j++){
                this.corridorAvailability[centerX + i][centerY + j] = false;
                this.boardArray[centerX + i][centerY + j].room = room;
            }
        }

        /*
        switch (room.wallNumber) {
            case 1:
                areaBuilder.getBlocks().put(Position3D.create(centerX + room.brickNumber, centerY - room.halfHeight, 0), new Floor());
                break;
            case 2:
                areaBuilder.getBlocks().put(Position3D.create(centerX + room.brickNumber, centerY + room.halfHeight, 0), new Floor());
                break;
            case 3:
                areaBuilder.getBlocks().put(Position3D.create(centerX - room.halfWidth, centerY + room.brickNumber, 0), new Floor());
                break;
            case 4:
                areaBuilder.getBlocks().put(Position3D.create(centerX + room.halfWidth, centerY + room.brickNumber, 0), new Floor());
                break;
        }
        */

    }

    boolean roomFitDetection(Room room, AreaBuilder areaBuilder){
        return (room.center.getX() - room.halfWidth >= 2 && room.center.getX() + room.halfWidth <= areaBuilder.getWidth() - 3) &&
                (room.center.getY() - room.halfHeight >= 2 && room.center.getY() + room.halfHeight <= areaBuilder.getHeight() - 3);
    }

    void placeEnemiesInRooms(AreaBuilder areaBuilder, List<Room> rooms){
        Random random = new Random();
        for (int i = 0; i < rooms.size(); i++){
            int centerX = rooms.get(i).center.getX();
            int centerY = rooms.get(i).center.getY();
            int maxNumberOfEnemies = (2 * rooms.get(i).halfWidth - 2 + 1) * (2 * rooms.get(i).halfHeight - 2 + 1);
            int numberOfEnemies = random.nextInt(maxNumberOfEnemies / 25 + 1);
            //int maxX = random.nextInt(2 * rooms.get(i).halfWidth - 1) - (rooms.get(i).halfWidth - 1);
            //int maxY = random.nextInt(2 * rooms.get(i).halfHeight - 1) - (rooms.get(i).halfHeight - 1);
            //System.out.print("here-2");
            //System.out.println("halfwidth: " + (rooms.get(i).halfWidth - 1));
            //System.out.println("numberOfEnemies: " + numberOfEnemies);
            //int[] xPositions = random.ints(-(rooms.get(i).halfWidth - 1), (rooms.get(i).halfWidth - 1)).distinct().limit(numberOfEnemies).toArray();
            //int[] yPositions = random.ints(-(rooms.get(i).halfHeight - 1), (rooms.get(i).halfHeight - 1)).distinct().limit(numberOfEnemies).toArray();
            for (int j = 0; j < numberOfEnemies; j++){
                boolean currentEnemyPlaced = false;
                while (currentEnemyPlaced == false){
                    int x = random.nextInt(2 * rooms.get(i).halfWidth - 2 + 1) - (rooms.get(i).halfWidth - 1);
                    int y = random.nextInt(2 * rooms.get(i).halfHeight - 2 + 1) - (rooms.get(i).halfHeight - 1);
                    if (!areaBuilder.getBlocks().get(Position3D.create(centerX + x, centerY + y, 0)).getBlocksMovement()){
                        areaBuilder.addEntity(new Rat(), Position3D.create(centerX + x, centerY + y, 0));
                        currentEnemyPlaced = true;
                    }
                }
                //areaBuilder.addEntity(new Rat(), Position3D.create(xPositions[j] + centerX, yPositions[j] + centerY, 0));
                //System.out.print("here1");
            }
        }
    }

    void placePlayerInARoom(AreaBuilder areaBuilder, List<Room> rooms){
        Random random = new Random();
        int randomRoomIndex = random.nextInt(rooms.size());
        Room room = rooms.get(randomRoomIndex);
        int centerX = room.center.getX();
        int centerY = room.center.getY();
        int roomPotentialAvailableSpaces = (2 * room.halfWidth - 2 + 1) * (2 * room.halfHeight - 2 + 1);
        boolean currentEnemyPlaced = false;
        while (currentEnemyPlaced == false){
            int x = random.nextInt(2 * room.halfWidth - 2 + 1) - (room.halfWidth - 1);
            int y = random.nextInt(2 * room.halfHeight - 2 + 1) - (room.halfHeight - 1);
            if (!areaBuilder.getBlocks().get(Position3D.create(centerX + x, centerY + y, 0)).getBlocksMovement()){
                areaBuilder.addEntity(areaBuilder.getPlayer(), Position3D.create(centerX + x, centerY + y, 0));
                currentEnemyPlaced = true;
            }
        }
    }

    public class Cell{
        public boolean visited = false;
        public Cell predecessor = null;
        public Cell child = null;
        public int x = 0;
        public int y = 0;
        public int componentNumber = 0;
        public boolean deadEnd = false;
        public boolean isParent = false;
        public int numberOfChildren = 0;
        // for room cells: componentNumber = 0, room != null
        // for corridor cells: componentNumber !=0, room == null
        public Room room = null;
        public int direction = 0;
        public int offset = 0;


    }

    // selecting start position of DFS
    /*
    public boolean ifEmptyAround(boolean[][] corridorAvailability, int x, int y){
        return ((corridorAvailability[x - 1][y] && corridorAvailability[x + 1][y]) && (corridorAvailability[x][y - 1] || corridorAvailability[x][y + 1])) ||
                ((corridorAvailability[x][y - 1] && corridorAvailability[x][y + 1]) && (corridorAvailability[x - 1][y] || corridorAvailability[x + 1][y]));
    }
    */


    public boolean ifEmptyAround(boolean[][] corridorAvailability, int x, int y){
        boolean ifEmpty = true;
        for (int i = -1; i <= 1; i++){
            for(int j = -1; j <= 1; j++){
                if (!corridorAvailability[x + i][y + j]){
                    ifEmpty = false;
                    break;
                }
            }
        }
        return ifEmpty;
        //return corridorAvailability[x - 1][y] && corridorAvailability[x + 1][y] && corridorAvailability[x][y - 1] && corridorAvailability[x][y + 1];
    }

    public boolean ifFitNeighbour(boolean[][] corridorAvailability, int x, int y, int originDirection){
        switch(originDirection){
            case 1:
                return corridorAvailability[x][y] && corridorAvailability[x - 1][y] && corridorAvailability[x + 1][y] && corridorAvailability[x][y - 1] && corridorAvailability[x - 1][y - 1] && corridorAvailability[x + 1][y - 1];
            case 2:
                return corridorAvailability[x][y] && corridorAvailability[x - 1][y] && corridorAvailability[x + 1][y] && corridorAvailability[x][y + 1] && corridorAvailability[x - 1][y + 1] && corridorAvailability[x + 1][y + 1];
            case 3:
                return corridorAvailability[x][y] && corridorAvailability[x][y + 1] && corridorAvailability[x][y - 1] && corridorAvailability[x + 1][y] && corridorAvailability[x + 1][y - 1] && corridorAvailability[x + 1][y + 1];
            case 4:
                return corridorAvailability[x][y] && corridorAvailability[x][y + 1] && corridorAvailability[x][y - 1] && corridorAvailability[x - 1][y] && corridorAvailability[x - 1][y - 1] && corridorAvailability[x - 1][y + 1];
        }
        System.out.println("something wrong here");
        return false;
    }

    // get fit neighbours
    public List<Integer> fitNeighbours(boolean[][] corridorAvailability, int x, int y, AreaBuilder areaBuilder){
        List<Integer> fitNeighbours = new ArrayList<Integer>();
        if (ifFitNeighbour(corridorAvailability, x, y - 1, 1)){
            fitNeighbours.add(1);
        }
        if (ifFitNeighbour(corridorAvailability, x, y + 1, 2)){
            fitNeighbours.add(2);
        }
        if (ifFitNeighbour(corridorAvailability, x + 1, y, 3)){
            fitNeighbours.add(3);
        }
        if (ifFitNeighbour(corridorAvailability, x - 1, y, 4)){
            fitNeighbours.add(4);
        }
        return fitNeighbours;
    }
    List<Cell> deadEnds;
    List<Cell> startEnds;
    int currentComponentNumber = 0;
    List<List<Cell>> regions;
    public void DFS(boolean[][] corridorAvailability, Cell[][] boardArray, int x, int y, AreaBuilder areaBuilder){
        List<Cell> currentRegion = new ArrayList<>();

        if (ifEmptyAround(corridorAvailability, x, y)){
            boardArray[x][y].visited = true;
            this.currentComponentNumber += 1;
            boardArray[x][y].componentNumber = this.currentComponentNumber;
            areaBuilder.getBlocks().put(Position3D.create(x, y, 0), new Dummy2());
            corridorAvailability[x][y] = false;
            System.out.println(x + ":::" + y);
            currentRegion.add(boardArray[x][y]);
            this.startEnds.add(boardArray[x][y]);
        }
        else{
            return;
        }




        int k = 0;
        boolean deadendFirstEncounter = true;
        Stack<Cell> path = new Stack<Cell>();

        int currentX = x;
        int currentY = y;
        Cell currentCell = boardArray[currentX][currentY];
        while (k < 1000){
            k += 1;
            List<Integer> fitNeighbours = fitNeighbours(this.corridorAvailability, currentX, currentY, areaBuilder);
            if (fitNeighbours.size() == 0){
                if (deadendFirstEncounter){
                    this.deadEnds.add(boardArray[currentX][currentY]);
                    deadendFirstEncounter = false;
                }
                if (path.size() == 0){
                    this.regions.add(currentRegion);
                    break;
                }
                else{
                    currentCell = path.pop();
                    currentX = currentCell.x;
                    currentY = currentCell.y;
                }
            }
            else{
                /*
                if (fitNeighbours.size() > 1){
                    currentCell.isParent = true;
                }
                */


                deadendFirstEncounter = true;
                path.push(currentCell);
                Random random = new Random();
                int randomDirectionIndex = random.nextInt(fitNeighbours.size());
                Integer randomFitNeighbour = fitNeighbours.get(randomDirectionIndex);
                Cell newCell = null;
                switch(randomFitNeighbour){
                    case 1:
                        newCell = boardArray[currentX][currentY - 1];
                        break;
                    case 2:
                        newCell = boardArray[currentX][currentY + 1];
                        break;
                    case 3:
                        newCell = boardArray[currentX + 1][currentY];
                        break;
                    case 4:
                        newCell = boardArray[currentX - 1][currentY];
                        break;
                }
                newCell.predecessor = currentCell;
                currentCell.child = newCell;
                currentCell.numberOfChildren += 1;
                currentCell = newCell;
                currentCell.componentNumber = this.currentComponentNumber;
                currentRegion.add(currentCell);
                currentX = currentCell.x;
                currentY = currentCell.y;
                System.out.println(currentX + "," + currentY);
                areaBuilder.getBlocks().put(Position3D.create(currentX, currentY, 0), new Dummy());
                this.corridorAvailability[currentX][currentY] = false;
            }
        }
    }

    public boolean cellProbe(Cell cell, List<Room> rooms, AreaBuilder areaBuilder){
        int x = cell.x;
        int y = cell.y;

        GameBlock block = areaBuilder.get(Position3D.create(x, y, 0));
        if (block instanceof Dummy || block instanceof Dummy2){
            return true;
        }

        for (int i = 0; i < rooms.size(); i++){
            int centerX = rooms.get(i).center.getX();
            int centerY = rooms.get(i).center.getY();
            if (x >= centerX - rooms.get(i).halfWidth + 1 && x <= centerX + rooms.get(i).halfWidth - 1 && y >= centerY - rooms.get(i).halfHeight + 1 && y <= centerY + rooms.get(i).halfHeight - 1){
                return true;
            }
        }
        return false;
    }


    public List<Cell> roomProbe(Room room, List<Room> rooms, AreaBuilder areaBuilder){
        int mapWidth = areaBuilder.getWidth();
        int mapHeight = areaBuilder.getHeight();
        int centerX = room.center.getX();
        int centerY = room.center.getY();
        int halfWidth = room.halfWidth;
        int halfHeight = room.halfHeight;
        List<Cell> connectors = new ArrayList<>();

        if (centerY - halfHeight - 2 >= 1){
            for (int i = -(halfWidth - 1); i <= (halfWidth - 1); i++) {
                Cell currentCell = this.boardArray[centerX + i][centerY - halfHeight - 2];
                if (cellProbe(currentCell, rooms, areaBuilder)) {
                    currentCell.direction = 1;
                    currentCell.offset = 2;
                    connectors.add(currentCell);
                }
                else if (centerY - halfHeight - 3 >= 1){
                    currentCell = this.boardArray[centerX + i][centerY - halfHeight - 3];
                    if (cellProbe(currentCell, rooms, areaBuilder)) {
                        currentCell.direction = 1;
                        currentCell.offset = 3;
                        connectors.add(currentCell);
                    }
                }
            }
        }

        if (centerY + halfHeight + 2 <= mapHeight - 2){
            for (int i = -(halfWidth - 1); i <= (halfWidth - 1); i++) {
                Cell currentCell = this.boardArray[centerX + i][centerY + halfHeight + 2];
                if (cellProbe(currentCell, rooms, areaBuilder)) {
                    currentCell.direction = 2;
                    currentCell.offset = 2;
                    connectors.add(currentCell);
                }
                else if (centerY + halfHeight + 3 <= mapHeight - 2){
                    currentCell = this.boardArray[centerX + i][centerY + halfHeight + 3];
                    if (cellProbe(currentCell, rooms, areaBuilder)) {
                        currentCell.direction = 2;
                        currentCell.offset = 3;
                        connectors.add(currentCell);
                    }
                }
            }
        }

        if (centerX + halfWidth + 2 <= mapWidth - 2){
            for (int i = -(halfHeight - 1); i <= (halfHeight - 1); i++) {
                Cell currentCell = this.boardArray[centerX + halfWidth + 2][centerY + i];
                if (cellProbe(currentCell, rooms, areaBuilder)) {
                    currentCell.direction = 3;
                    currentCell.offset = 2;
                    connectors.add(currentCell);
                }
                else if (centerX + halfWidth + 3 <= mapWidth - 2){
                    currentCell = this.boardArray[centerX + halfWidth + 3][centerY + i];
                    if (cellProbe(currentCell, rooms, areaBuilder)) {
                        currentCell.direction = 3;
                        currentCell.offset = 3;
                        connectors.add(currentCell);
                    }
                }
            }
        }

        if (centerX - halfWidth - 2 >= 1){
            for (int i = -(halfHeight - 1); i <= (halfHeight - 1); i++) {
                Cell currentCell = this.boardArray[centerX - halfWidth - 2][centerY + i];
                if (cellProbe(currentCell, rooms, areaBuilder)) {
                    currentCell.direction = 4;
                    currentCell.offset = 2;
                    connectors.add(currentCell);
                }
                else if (centerX - halfWidth - 3 >= 1){
                    currentCell = this.boardArray[centerX - halfWidth - 3][centerY + i];
                    if (cellProbe(currentCell, rooms, areaBuilder)) {
                        currentCell.direction = 4;
                        currentCell.offset = 3;
                        connectors.add(currentCell);
                    }
                }
            }
        }
        return connectors;
    }

    public void connectRoomAndSurround(Cell cell, AreaBuilder areaBuilder){
        int offset = cell.offset;
        int direction = cell.direction;
        cell.numberOfChildren += 2;
        switch(direction){
            case 1:
                for (int i = 1; i <= offset; i++){
                    areaBuilder.getBlocks().put(Position3D.create(cell.x, cell.y + i, 0), new Dummy3());
                }
                // mark it until the last penetrating block with a non-zero number so these offset blocks other than the last one can be surrounded
                for (int i = 1; i < offset; i++){
                    this.boardArray[cell.x][cell.y + i].componentNumber = 1;
                }
                break;
            case 2:
                for (int i = 1; i <= offset; i++){
                    areaBuilder.getBlocks().put(Position3D.create(cell.x, cell.y - i, 0), new Dummy3());
                }
                for (int i = 1; i < offset; i++){
                    this.boardArray[cell.x][cell.y - i].componentNumber = 1;
                }
                break;
            case 3:
                for (int i = 1; i <= offset; i++){
                    areaBuilder.getBlocks().put(Position3D.create(cell.x - i, cell.y, 0), new Dummy3());
                }
                for (int i = 1; i < offset; i++){
                    this.boardArray[cell.x - i][cell.y].componentNumber = 1;
                }
                break;
            case 4:
                for (int i = 1; i <= offset; i++){
                    areaBuilder.getBlocks().put(Position3D.create(cell.x + i, cell.y, 0), new Dummy3());
                }
                for (int i = 1; i < offset; i++){
                    this.boardArray[cell.x + i][cell.y].componentNumber = 1;
                }
                break;
        }

    }

    public void changeRoomWallComponentNumber(Cell cell, int componentNumber){
        Room room = cell.room;
        int centerX = room.center.getX();
        int centerY = room.center.getY();
        int halfWidth = room.halfWidth;
        int halfHeight = room.halfHeight;
        for (int i = -(halfWidth - 1); i <= (halfWidth - 1); i++){
            this.boardArray[centerX + i][centerY - halfHeight].componentNumber = componentNumber;
            this.boardArray[centerX + i][centerY + halfHeight].componentNumber = componentNumber;
        }

        for (int i = -(halfHeight - 1); i <= (halfHeight - 1); i++) {
            this.boardArray[centerX - halfWidth][centerY + i].componentNumber = componentNumber;
            this.boardArray[centerX + halfWidth][centerY + i].componentNumber = componentNumber;
        }
    }

    public void surroundTheCorridorCellWithWall(Cell cell, AreaBuilder areaBuilder){
        System.out.println("surrouding");
        for (int i = -1; i <= 1; i++){
            for(int j = -1; j <= 1; j++){
                GameBlock currentBlock = areaBuilder.get(Position3D.create(cell.x + i, cell.y + j, 0));
                if (!(currentBlock instanceof Dummy) && !(currentBlock instanceof Dummy2) && !(currentBlock instanceof Dummy3) && !(currentBlock instanceof Wall)){
                    areaBuilder.getBlocks().put(Position3D.create(cell.x + i, cell.y + j, 0), new CorridorWall());
                }
            }
        }
    }


    Cell[][] boardArray;
    Area buildLevel() {
        // Start with an empty area
        AreaBuilder areaBuilder = (new EmptyAreaBuilder()).create();

        int arrayWidth = areaBuilder.getWidth();
        int arrayHeight = areaBuilder.getHeight();
        this.corridorAvailability = new boolean[arrayWidth][arrayHeight];
        //this.corridorFit = new boolean[arrayWidth][arrayHeight];

        this.boardArray = new Cell[arrayWidth][arrayHeight];


        // the most outside side are walls so only inner side is reinitialized to true;
        for (int i = 1; i < arrayWidth - 1; i++){
            for (int j = 1; j < arrayHeight - 1; j++){
                this.corridorAvailability[i][j] = true;
                this.boardArray[i][j] = new Cell();
                this.boardArray[i][j].x = i;
                this.boardArray[i][j].y = j;
            }
        }



        //System.out.println("this.boardArray test: " + this.boardArray[0][0].visited);


        System.out.println("width: " + areaBuilder.getWidth());
        System.out.println("height: " + areaBuilder.getHeight());
        System.out.println("test availability: " + corridorAvailability[10][10]);

        int iteration = 0;
        this.rooms = new ArrayList<Room>();

        Room roomFactory = new Room(Position3D.create(0, 0, 0),0,0,0,0, 0);

        while (iteration < 200){
            Room room = roomFactory.createRandomRoom(areaBuilder);
            boolean roomCollision = false;
            for (Room r : this.rooms){
                if (AABBRoomCollisionDetection(r, room)){
                    roomCollision = true;
                    break;
                }
            }

            if (!roomCollision && roomFitDetection(room, areaBuilder)){
                createRoom(room, areaBuilder);
                this.rooms.add(room);
            }
            iteration += 1;
        }

        System.out.println("availability after room creation: " + corridorAvailability[10][10]);

        List<Integer> fitNeighbours = new ArrayList<Integer>();

        System.out.println("null or what: " + fitNeighbours == null);


        this.regions = new ArrayList<>();
        this.deadEnds = new ArrayList<Cell>();
        this.startEnds = new ArrayList<Cell>();
        // run DFS for each region of the map other than the rooms

        for (int i = 2; i < arrayWidth - 2; i++){
            for (int j = 2; j < arrayHeight - 2; j++){
                DFS(this.corridorAvailability, this.boardArray, i, j, areaBuilder);
            }
        }




        /*
        for (int i = 2; i < arrayWidth - 2; i++){
                DFS(this.corridorAvailability, this.boardArray, i, 2, areaBuilder);
        }
        */


        //DFS(this.corridorAvailability, this.boardArray, 2, 2, areaBuilder);


        for (int i = 0; i < this.rooms.size(); i++){
            List<Cell> connectors = roomProbe(this.rooms.get(i), this.rooms, areaBuilder);
            int roomComponentNumber = this.rooms.get(i).componentNumber;
            for (int j = 0; j < connectors.size(); j++){
                int connectorComponentNumber;
                if (connectors.get(j).room == null){
                    connectorComponentNumber = connectors.get(j).componentNumber;
                }
                else{
                    connectorComponentNumber = connectors.get(j).room.componentNumber;
                }

                // in the following ifs, there's a possibility that a room is connected twice to the same region which creates a non-perfect loop
                // but on each wall of the room, there's at most one connection
                if (roomComponentNumber == 0 && connectorComponentNumber == 0){
                    this.currentComponentNumber += 1;
                    this.rooms.get(i).componentNumber = this.currentComponentNumber;
                    roomComponentNumber = this.currentComponentNumber;
                    connectors.get(j).room.componentNumber = this.currentComponentNumber;
                    connectRoomAndSurround(connectors.get(j), areaBuilder);
                }
                else if (roomComponentNumber == 0 && roomComponentNumber != connectorComponentNumber){
                    this.rooms.get(i).componentNumber = connectorComponentNumber;
                    roomComponentNumber = connectorComponentNumber;
                    connectRoomAndSurround(connectors.get(j), areaBuilder);
                }
                else if (roomComponentNumber != 0 && roomComponentNumber != connectorComponentNumber && connectorComponentNumber == 0){
                    connectRoomAndSurround(connectors.get(j), areaBuilder);
                    connectors.get(j).room.componentNumber = roomComponentNumber;
                }
                else if (roomComponentNumber != 0 && roomComponentNumber != connectorComponentNumber && connectorComponentNumber != 0){
                    connectRoomAndSurround(connectors.get(j), areaBuilder);
                    roomComponentNumber = connectorComponentNumber;
                }
            }
        }

        // truncate the deadends
        for (int i = 0; i < this.deadEnds.size(); i++){
            Cell currentCell = this.deadEnds.get(i);
            while (currentCell != null && currentCell.numberOfChildren < 1){
                int x = currentCell.x;
                int y = currentCell.y;
                areaBuilder.getBlocks().put(Position3D.create(x, y, 0), new Floor());
                currentCell.componentNumber = 0;
                currentCell = currentCell.predecessor;

                if (currentCell != null){
                    currentCell.numberOfChildren -= 1;
                }


                System.out.println("removing deadends: " + x + "," + y);
                //System.out.println("numberOfChildren: " + currentCell.numberOfChildren);
            }
        }

        // truncate the startends
        for (int i = 0; i < this.startEnds.size(); i++){
            Cell currentCell = this.startEnds.get(i);
            while (currentCell != null && currentCell.numberOfChildren < 2){
                int x = currentCell.x;
                int y = currentCell.y;
                areaBuilder.getBlocks().put(Position3D.create(x, y, 0), new Floor());
                currentCell.componentNumber = 0;
                currentCell = currentCell.child;
            }
        }

        // surround the corridors with walls
        for (int i = 2; i < arrayWidth - 2; i++){
            for (int j = 2; j < arrayHeight - 2; j++){
                if(this.boardArray[i][j].room == null && this.boardArray[i][j].componentNumber != 0){
                    surroundTheCorridorCellWithWall(this.boardArray[i][j], areaBuilder);
                }
            }
        }


        /*
        // Place the player at an empty location in the top-left quarter
        int width = areaBuilder.getWidth() / 2 - 2;
        int height = areaBuilder.getHeight() / 2 - 2;
        areaBuilder.addAtEmptyPosition(
                areaBuilder.getPlayer(),
                Position3D.create(1, 1, 0),
                Size3D.create(width, height, 1)
        );
        */

        /*
        for (int i = 0; i <= currentLevel; ++i) {
            areaBuilder.addAtEmptyPosition(new Rat(), Position3D.defaultPosition(), areaBuilder.getSize());
        }
        */



        //place player in a random room
        placePlayerInARoom(areaBuilder, this.rooms);


        // put the stair far away from the player
        Map<Position3D, Integer> floodFill = Pathfinding.floodFill(areaBuilder.getPlayer().getPosition(), areaBuilder, Pathfinding.INSTANCE.getEightDirectional(), Pathfinding.INSTANCE.getDoorOpening());
        Object[] floodFillArray = floodFill.keySet().toArray();
        Random random = new Random();
        Position3D farPosition = (Position3D) floodFillArray[floodFillArray.length - random.nextInt(floodFillArray.length / 9)];
        areaBuilder.addEntity(new Stairs(), farPosition);


        // place enemies in rooms
        placeEnemiesInRooms(areaBuilder, this.rooms);

        // Build it into a full Area
        return areaBuilder.build();
    }

    /**
     * Moving down - goes to a brand new level.
     */
    @Override
    public void moveDown() {
        ++currentLevel;
        (new LoggedEvent(this, "Descended to level " + (currentLevel + 1))).emit();
        if (currentLevel >= getAreas().getSize()) getAreas().add(buildLevel());
        goToArea(getAreas().get(currentLevel));
    }

    /**
     * Moving up would be for revisiting past levels, we do not need that. Check [DungeonWorld] for an implementation.
     */
    @Override
    public void moveUp() {
        // Not implemented
    }
}
