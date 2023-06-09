package cz.cuni.gamedev.nail123.roguelike.tiles

import org.hexworks.zircon.api.GraphicalTilesetResources
import org.hexworks.zircon.api.resource.TilesetResource

object TilesetResources {
    val filty32x32 by lazy { loadTileset("filty32x32", 32) }
    val kenney16x16 by lazy { loadTileset("kenney_superscaled_16x16", 16) }
    val tilation16x16 by lazy { loadTileset("small_indoor_tileset_16x16", 16) }
    val klinkos16x16 by lazy { loadTileset("mcr_sci_fi_1_16x16", 16) }

    fun loadTileset(name: String, dim: Int): TilesetResource {
        return GraphicalTilesetResources.loadTilesetFromJar(dim, dim, "/tilesets/$name/$name.zip")
    }
}