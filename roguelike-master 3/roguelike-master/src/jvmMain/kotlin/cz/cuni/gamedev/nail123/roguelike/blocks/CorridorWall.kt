package cz.cuni.gamedev.nail123.roguelike.blocks

import cz.cuni.gamedev.nail123.roguelike.tiles.GameTiles

class CorridorWall: GameBlock(GameTiles.CORRIDOR){
    override val blocksMovement = true
    override val blocksVision = false
}
