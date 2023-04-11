package cz.cuni.gamedev.nail123.roguelike.world.worlds;

import cz.cuni.gamedev.nail123.roguelike.blocks.Floor;
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

import cz.cuni.gamedev.nail123.roguelike.blocks.Wall;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SampleJavaWorld extends World {
    int currentLevel = 0;

    public SampleJavaWorld() {
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

        public Room(Position3D center, int halfWidth, int halfHeight, int wallNumber, int brickNumber){
            this.center = center;
            this.halfWidth = halfWidth;
            this.halfHeight = halfHeight;
            this.wallNumber = wallNumber;
            this.brickNumber = brickNumber;
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

            return new Room(center, halfWidth, halfHeight, wallNumber, brickNumber);

        }
    }

    List<Room> rooms = new ArrayList<Room>();

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
    }

    boolean roomFitDetection(Room room, AreaBuilder areaBuilder){
        return (room.center.getX() - room.halfWidth >= 2 && room.center.getX() + room.halfWidth <= areaBuilder.getWidth() - 3) &&
                (room.center.getY() - room.halfHeight >= 2 && room.center.getY() + room.halfHeight <= areaBuilder.getHeight() - 3);
    }

    Area buildLevel() {
        // Start with an empty area
        AreaBuilder areaBuilder = (new EmptyAreaBuilder()).create();

        //areaBuilder.getBlocks().put(Position3D.create(1, 2, 0), new Wall());
        //areaBuilder.getBlocks().put(Position3D.create(2, 2, 0), new Wall());
        //areaBuilder.getBlocks().put(Position3D.create(3, 2, 0), new Wall());
        //areaBuilder.getBlocks().put(Position3D.create(4, 2, 0), new Wall());


        int iteration = 0;
        this.rooms = new ArrayList<Room>();

        Room roomFactory = new Room(Position3D.create(0, 0, 0),0,0,0,0);

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



        // System.out.print("The block is: " + (areaBuilder.getBlocks().get(Position3D.create(2, 2, 0)).getBlocksMovement()));
        // Place the player at an empty location in the top-left quarter
        int width = areaBuilder.getWidth() / 2 - 2;
        int height = areaBuilder.getHeight() / 2 - 2;
        areaBuilder.addAtEmptyPosition(
                areaBuilder.getPlayer(),
                Position3D.create(1, 1, 0),
                Size3D.create(width, height, 1)
        );
        //System.out.print("getWidth: " + width);
        //System.out.print("getHeight: " + height);
        //

        /*
        // Place the stairs at an empty location in the top-right quarter
        areaBuilder.addAtEmptyPosition(
                new Stairs(),
                Position3D.create(areaBuilder.getWidth() / 2, areaBuilder.getHeight() / 2, 0),
                Size3D.create(areaBuilder.getWidth() / 2 - 2, areaBuilder.getHeight() / 2 - 2, 1)
        );
        */

        for (int i = 0; i <= currentLevel; ++i) {
            areaBuilder.addAtEmptyPosition(new Rat(), Position3D.defaultPosition(), areaBuilder.getSize());
        }

        Map<Position3D, Integer> floodFill = Pathfinding.floodFill(areaBuilder.getPlayer().getPosition(), areaBuilder, Pathfinding.INSTANCE.getEightDirectional(), Pathfinding.INSTANCE.getDoorOpening());

        Object[] floodFillArray = floodFill.keySet().toArray();
        Random random = new Random();
        Position3D farPosition = (Position3D) floodFillArray[floodFillArray.length - random.nextInt(floodFillArray.length / 10)];

        //System.out.print("floodfill:" + (Position3D)floodFillArray[floodFillArray.length - random.nextInt(floodFillArray.length / 10)]);
        areaBuilder.addEntity(new Stairs(), farPosition);
        //System.out.print("defaultPosition: " + Position3D.defaultPosition());
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
