package de.infinityprojects.mcserver.world.generator

import net.minestom.server.instance.block.Block
import net.minestom.server.instance.generator.GenerationUnit
import net.minestom.server.instance.generator.Generator

class FlatWorldGenerator: Generator {
    override fun generate(unit: GenerationUnit) {
        unit.modifier().fillHeight(0, 1, Block.BEDROCK)
        unit.modifier().fillHeight(1, 3, Block.DIRT)
        unit.modifier().fillHeight(3, 4, Block.GRASS_BLOCK)
    }
}