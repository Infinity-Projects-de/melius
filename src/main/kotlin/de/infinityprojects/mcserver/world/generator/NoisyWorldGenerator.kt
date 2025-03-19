package de.infinityprojects.mcserver.world.generator

import de.articdive.jnoise.core.api.functions.Interpolation
import de.articdive.jnoise.generators.noise_parameters.fade_functions.FadeFunction
import de.articdive.jnoise.generators.noisegen.perlin.PerlinNoiseGenerator
import de.articdive.jnoise.modules.octavation.OctavationModule
import de.articdive.jnoise.pipeline.JNoise
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.generator.GenerationUnit
import net.minestom.server.instance.generator.Generator

class NoisyWorldGenerator(val seed: Long = 1231231234): Generator {
    val noise = JNoise.newBuilder().value(seed, Interpolation.CUBIC, FadeFunction.NONE)
        .scale(1 / 16.0)
        .addModifier { v -> (v + 1) / 2.0 }
        .build()

    override fun generate(unit: GenerationUnit) {
        val start = unit.absoluteStart()
        for (x in 0 until unit.size().blockX()) {
            for (z in 0 until unit.size().blockZ()) {
                val bottom = start.add(x.toDouble(), 0.0, z.toDouble())
                synchronized(noise) {
                    val height = noise.evaluateNoise(bottom.x(), 0.0, bottom.z()) * 16

                    unit.modifier().fill(bottom, bottom.add(1.0, 0.0, 1.0).withY(height), Block.STONE)
                }

            }
        }


    }

}