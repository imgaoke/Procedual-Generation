package cz.cuni.gamedev.nail123.roguelike.blocks

import cz.cuni.gamedev.nail123.roguelike.tiles.GameTiles

class Dummy: GameBlock(GameTiles.FLOOR){
    override val blocksMovement = false
    override val blocksVision = false
}
