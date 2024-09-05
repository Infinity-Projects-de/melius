package de.infinityprojects.mcserver.data

import net.kyori.adventure.nbt.CompoundBinaryTag
import net.minestom.server.coordinate.Pos
import net.minestom.server.tag.Tag
import net.minestom.server.tag.TagSerializer

data class WorldSpawn(
    val pos: Pos,
) {
    companion object {
        fun getTag(worldName: String = "this"): Tag<WorldSpawn> =
            Tag.Structure(
                "melius:world_spawn:$worldName",
                TagSerializer.fromCompound(
                    { compound ->
                        val x = compound.getDouble("x")
                        val y = compound.getDouble("y")
                        val z = compound.getDouble("z")
                        val pos = Pos(x, y, z)
                        WorldSpawn(pos)
                    },
                    { world ->
                        CompoundBinaryTag
                            .builder()
                            .putDouble("x", world.pos.x)
                            .putDouble("y", world.pos.y)
                            .putDouble("z", world.pos.z)
                            .build()
                    },
                ),
            )
    }
}
