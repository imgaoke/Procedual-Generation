package cz.cuni.gamedev.nail123.roguelike.tiles

import cz.cuni.gamedev.nail123.roguelike.world.Direction
import org.hexworks.zircon.api.CP437TilesetResources
import org.hexworks.zircon.api.color.TileColor
import org.hexworks.zircon.api.data.Tile
import org.hexworks.zircon.api.resource.TilesetResource

object GameTiles {
    val defaultCharTileset = CP437TilesetResources.rogueYun16x16()
    val defaultGraphicalTileset = TilesetResources.klinkos16x16

    val EMPTY: Tile = Tile.empty()

    // Allowed characters for tiles are https://en.wikipedia.org/wiki/Code_page_437
    val FLOOR = characterTile('.', GameColors.FLOOR_FOREGROUND, GameColors.FLOOR_BACKGROUND)

    val WALL = graphicalTile("Wall") //useful
    val CORRIDOR = graphicalTile("Corridor") //useful
    val PLAYER = graphicalTile("Player") //useful
    val CLOSED_DOOR = graphicalTile("PLACEHOLDER 1")
    val OPEN_DOOR = graphicalTile("PLACEHOLDER 2")
    val STAIRS_DOWN = graphicalTile("Stair") //useful
    val STAIRS_UP = graphicalTile("PLACEHOLDER 3")
    val BLACK = characterTile(' ', GameColors.BLACK, GameColors.BLACK)
    val RAT = graphicalTile("Rat") //useful
    val SWORD = graphicalTile("PLACEHOLDER 4")

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