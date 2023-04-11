package cz.cuni.gamedev.nail123.roguelike.tiles

import cz.cuni.gamedev.nail123.roguelike.world.Direction
import org.hexworks.zircon.api.CP437TilesetResources
import org.hexworks.zircon.api.color.TileColor
import org.hexworks.zircon.api.data.Tile
import org.hexworks.zircon.api.resource.TilesetResource

object GameTilesOriginal {
    val defaultCharTileset = CP437TilesetResources.rogueYun16x16()
    val defaultGraphicalTileset = TilesetResources.klinkos16x16

    val EMPTY: Tile = Tile.empty()

    // Allowed characters for tiles are https://en.wikipedia.org/wiki/Code_page_437
    val FLOOR = characterTile('.', GameColors.FLOOR_FOREGROUND, GameColors.FLOOR_BACKGROUND)

    // Wall tile is replaced by autotiling in Wall.kt
    //val WALL = characterTile('#', GameColors.WALL_FOREGROUND, GameColors.WALL_BACKGROUND)
    val WALL = graphicalTile("Wall") //useful

    val CORRIDOR = graphicalTile("Corridor") //useful

    //var HEART_FULL = graphicalTile("Heart full")
    //var HEART_HALF_FULL = graphicalTile("Heart half full")
    //var HEART_EMPTY = graphicalTile("Heart empty")
    //var TABLE = graphicalTile("Table")

    val PLAYER = graphicalTile("Player") //useful
    val CLOSED_DOOR = graphicalTile("PLACEHOLDER 1")
    val OPEN_DOOR = graphicalTile("PLACEHOLDER 2")
    val STAIRS_DOWN = graphicalTile("Stair") //useful
    val STAIRS_UP = graphicalTile("PLACEHOLDER 3")
    val BLACK = characterTile(' ', GameColors.BLACK, GameColors.BLACK)

    val RAT = graphicalTile("Rat") //useful

    val SWORD = graphicalTile("PLACEHOLDER 4")

    // Autotiling tries to find a tile by whether similar tiles neighbor in some directions
    // It iterates through a list of Directional flags (which must be fulfilled) to tiles that should be used
    // It uses the first one found
    private val all8 = Direction.eightDirections.sumBy { it.flag }
    /*
    val wallTiling = Autotiling(
            // Default
            graphicalTile("Wall thick W E"),
            // Walls all around
            all8 to graphicalTile("Black"),
            // Walls everywhere except one corner
            all8 - Direction.NORTH_WEST.flag to graphicalTile("Wall thick N W"),
            all8 - Direction.NORTH_EAST.flag to graphicalTile("Wall thick N E"),
            all8 - Direction.SOUTH_WEST.flag to graphicalTile("Wall thick S W"),
            all8 - Direction.SOUTH_EAST.flag to graphicalTile("Wall thick S E"),
            // Lines
            Direction.NORTH + Direction.SOUTH to graphicalTile("Wall thick N S"),
            Direction.WEST + Direction.EAST to graphicalTile("Wall thick W E"),
            // Corners
            Direction.NORTH + Direction.EAST to graphicalTile("Wall thick N E"),
            Direction.NORTH + Direction.WEST to graphicalTile("Wall thick N W"),
            Direction.SOUTH + Direction.EAST to graphicalTile("Wall thick S E"),
            Direction.SOUTH + Direction.WEST to graphicalTile("Wall thick S W"),
            // Single adjacent (horizontal ones fallback to default)
            Direction.NORTH.flag to graphicalTile("Wall thick N S"),
            Direction.SOUTH.flag to graphicalTile("Wall thick N S")
    )

     */

    fun characterTile(char: Char,
                      foreground: TileColor = GameColors.OBJECT_FOREGROUND,
                      background: TileColor = TileColor.transparent()): Tile {

        return Tile.newBuilder()
                   .withCharacter(char)
                   .withForegroundColor(foreground)
                   .withBackgroundColor(background)
                   .build()
    }

    fun graphicalTile(tag: String, tileset: TilesetResource = defaultGraphicalTileset): Tile {
        return Tile.newBuilder()
                   .withName(tag)
                   .withTags(tag.split(' ').toSet())
                   .withTileset(tileset)
                   .buildGraphicalTile()
    }
}