package de.infinityprojects.mcserver.world.generator

import de.articdive.jnoise.core.api.functions.Interpolation
import de.articdive.jnoise.generators.noise_parameters.fade_functions.FadeFunction
import de.articdive.jnoise.modules.octavation.fractal_functions.FractalFunction
import de.articdive.jnoise.pipeline.JNoise
import net.minestom.server.coordinate.Point
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.generator.GenerationUnit
import net.minestom.server.instance.generator.Generator
import net.minestom.server.registry.DynamicRegistry
import net.minestom.server.world.biome.Biome
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow
import kotlin.random.Random

class OverworldGenerator: Generator {
    val random = Random(System.currentTimeMillis())
    fun getRandomSeed(): Long {
        return random.nextLong()
    }

    private val size = 128.0;

    val terrainSeed = getRandomSeed()

    private val temperature = JNoise.newBuilder()
        .perlin(getRandomSeed(), Interpolation.LINEAR, FadeFunction.CUBIC_POLY)
        .octavate(2, 0.4, 2.0, FractalFunction.FBM, false).scale(1.0 / (8 * size)).build()

    private val humidity = JNoise.newBuilder()
        .perlin(getRandomSeed(), Interpolation.LINEAR, FadeFunction.CUBIC_POLY)
        .octavate(4, 0.4, 2.0, FractalFunction.FBM, false).scale(1 / (6 * size)).build()

    private val continentalness = JNoise.newBuilder()
        .perlin(terrainSeed, Interpolation.CUBIC, FadeFunction.CUBIC_POLY)
        .octavate(7, 0.5, 2.0, FractalFunction.FBM, false).scale(1 / (4 * size)).build()

    private val erosion = JNoise.newBuilder()
        .perlin(getRandomSeed(), Interpolation.CUBIC, FadeFunction.CUBIC_POLY)
        .octavate(4, 0.5, 2.0, FractalFunction.FBM, false).scale(1 / (4 * size)).build()

    private val weirdness = JNoise.newBuilder()
        .perlin(getRandomSeed(), Interpolation.CUBIC, FadeFunction.CUBIC_POLY)
        .octavate(8, 0.5, 2.0, FractalFunction.FBM, false).scale(1 / (4 * size)).build()

    private val density = JNoise.newBuilder()
        .perlin(terrainSeed, Interpolation.CUBIC, FadeFunction.CUBIC_POLY)
        .octavate(7, 0.5, 2.0, FractalFunction.FBM, false).scale(1 / (4 * size)).build()

    private val nature = JNoise.newBuilder()
        .gaussianWhite(getRandomSeed()).build()
        //.octavate(8, 0.5, 2.0, FractalFunction.FBM, false).scale(0.1)

    private val baseHeight = 64.0

    override fun generate(unit: GenerationUnit) {
        val start = unit.absoluteStart()
        for (x in 0 until unit.size().blockX()) {
            for (z in 0 until unit.size().blockZ()) {
                val bottom = start.add(x.toDouble(), 0.0, z.toDouble())

                val bX = bottom.x()
                val bZ = bottom.z()

                val (temp, rawTemp) = getTemperature(bX,bZ)
                val (hum, rawHum) = getHumidity(bX,bZ)
                val (cont, rawCont) = getContinentalness(bX,bZ)

                val weird = synchronized(weirdness) {
                    return@synchronized weirdness.evaluateNoise(bX, 0.0, bZ)
                }
                val eros = getErosion(bX,bZ)

                val nature = synchronized(nature) {
                    return@synchronized nature.evaluateNoise(bX, 0.0, bZ)
                }

                val biome = getBiome(temp, hum, cont, weird)

                val peaks = 1 - abs(abs(3.0 * weird) - 2.0)

                var blocksAbove = 0

                var height = baseHeight + rawCont / 4

                var heightBias = 0.01 + 0.005 * peaks + 0.0005 * (3 - rawTemp) + 0.0005 * (3 - rawHum) + 0.0005 * (3 - rawCont)

                for (y in unit.size().blockY() - 1 downTo 0 ) {
                    val bottom = start.add(x.toDouble(), y.toDouble(), z.toDouble())
                    var density = density.evaluateNoise(bottom.x(), bottom.y() - baseHeight, bottom.z()) + 0.1 + 0.1 * peaks

                    val distance = bottom.blockY() - height
                    density -= heightBias * distance
                    if (density > 0.0) {
                        val block = if (biome == Biome.BEACH || biome == Biome.SNOWY_BEACH || biome == Biome.DESERT) {
                            if (blocksAbove < 5) {
                                Block.SAND
                            } else if (blocksAbove < 10) {
                                Block.DIRT
                            } else {
                                Block.STONE
                            }
                        } else if (cont.ordinal > Continentalness.COAST.ordinal) {
                            if (blocksAbove == 0) {
                                if (bottom.blockY() < baseHeight - 1) {
                                    Block.DIRT
                                } else {
                                    putNature(unit, bottom.add(0.0, 1.0, 0.0), nature, biome)

                                    Block.GRASS_BLOCK
                                }
                            } else if (blocksAbove < 5) {
                                Block.DIRT
                            } else {
                                Block.STONE
                            }
                        } else {
                            if (blocksAbove < 5) {
                                val random = floor(rawHum + rawTemp).toInt()
                                when (random) {
                                    0, 1, 8 -> Block.DIRT
                                    7, -> Block.COARSE_DIRT
                                    3, 4 -> Block.SAND
                                    5 -> Block.GRAVEL
                                    2, 6 -> Block.CLAY
                                    else -> {
                                        Block.DIRT
                                    }
                                }
                            } else {
                                Block.STONE
                            }
                        }

                        unit.modifier().setBlock(bottom, block)

                        blocksAbove++
                    } else if (bottom.blockY() < baseHeight) {
                        unit.modifier().setBlock(bottom, Block.WATER)
                    }

                    unit.modifier().setBiome(bottom, biome)

                }
            }
        }
    }

    fun putNature(unit: GenerationUnit, pos: Point, nature: Double, biome: DynamicRegistry.Key<Biome>) {
        val floorPlants = when (biome) {
            Biome.PLAINS -> when {
                nature < 0.1 -> Block.SHORT_GRASS
                nature < 0.2 -> Block.TALL_GRASS
                nature < 0.3 -> Block.FERN
                nature < 0.4 -> Block.LARGE_FERN
                nature < 0.5 -> Block.SUNFLOWER
                else -> null
            }
            Biome.FOREST -> when {
                nature < 0.1 -> Block.SHORT_GRASS
                nature < 0.2 -> Block.TALL_GRASS
                nature < 0.3 -> Block.FERN
                nature < 0.4 -> Block.LARGE_FERN
                nature < 0.45 -> Block.OAK_SAPLING
                else -> null
            }
            else -> null
        }

        if (floorPlants != null && nature > 0) {
            if (floorPlants == Block.SUNFLOWER) {
                unit.modifier().setBlock(pos, floorPlants)
                unit.modifier().setBlock(pos.add(0.0, 1.0, 0.0), Block.SUNFLOWER)
            }

            if (floorPlants == Block.OAK_SAPLING) {
                unit.fork { setter ->
                    for (i in 0 until 4) {
                        setter.setBlock(pos.add(0.0, i.toDouble(), 0.0), Block.OAK_LOG)
                    }
                    for (i in -2 until 2) {
                        for (j in -2 until 2) {
                            setter.setBlock(pos.add(i.toDouble(), 4.0, j.toDouble()), Block.OAK_LEAVES)
                        }
                    }
                }
            }


            unit.modifier().setBlock(pos, floorPlants)
        }
    }

    enum class Continentalness(
        val lowerBound: Double,
        val upperBound: Double
    ) {
        MUSHROOM_FIELDS(-1.2, -1.05),
        DEEP_OCEAN(-1.05, -0.455),
        OCEAN(-0.455, -0.19),
        COAST(-0.19, -0.11),
        NEAR_INLAND(-0.11, 0.03),
        MID_INLAND(0.03, 0.3),
        FAR_INLAND(0.3, 1.0),
    }

    fun getContinentalness(x: Double, z: Double): Pair<Continentalness, Double> {
        synchronized(continentalness) {
            val cont = continentalness.evaluateNoise(x, 0.0, z)

            val continentalness = when (cont) {
                in -1.2..-1.05 -> Continentalness.MUSHROOM_FIELDS
                in -1.05..-0.455 -> Continentalness.DEEP_OCEAN
                in -0.455..-0.19 -> Continentalness.OCEAN
                in -0.19..-0.11 -> Continentalness.COAST
                in -0.11..0.03 -> Continentalness.NEAR_INLAND
                in 0.03..0.3 -> Continentalness.MID_INLAND
                in 0.3..1.0 -> Continentalness.FAR_INLAND
                else -> Continentalness.OCEAN
            }

            return Pair(continentalness, continentalness.ordinal + ((cont - continentalness.lowerBound) / (continentalness.upperBound - continentalness.lowerBound)))
        }
    }

    enum class Temperature(
        val lowerBound: Double,
        val upperBound: Double
    ) {
        FROZEN(-1.0, -0.45),
        COLD(-0.45, -0.15),
        MEDIUM(-0.15, 0.2),
        WARM(0.2, 0.55),
        HOT(0.55, 1.0),
    }

    fun getTemperature(x: Double, z: Double): Pair<Temperature, Double> {
        synchronized(temperature) {
            val temp = temperature.evaluateNoise(x, 0.0, z)

            val temperature = when (temp) {
                in -1.0..-0.45 -> Temperature.FROZEN
                in -0.45..-0.15 -> Temperature.COLD
                in -0.15..0.2 -> Temperature.MEDIUM
                in 0.2..0.55 -> Temperature.WARM
                in 0.55..1.0 -> Temperature.HOT
                else -> Temperature.MEDIUM
            }

            return Pair(temperature, temperature.ordinal + ((temp - temperature.lowerBound) / (temperature.upperBound - temperature.lowerBound)))
        }
    }

    enum class Humidity(
        val lowerBound: Double,
        val upperBound: Double
    ) {
        DRY(-1.0, -0.35),
        SEMI_DRY(-0.35, -0.1),
        MEDIUM(-0.1, 0.1),
        SEMI_WET(0.1, 0.3),
        WET(0.3, 1.0),
    }

    fun getHumidity(x: Double, z: Double): Pair<Humidity, Double> {
        synchronized(humidity) {
            val hum = humidity.evaluateNoise(x, 0.0, z)

            val humidity = when (hum) {
                in -1.0..-0.35 -> Humidity.DRY
                in -0.35..-0.1 -> Humidity.SEMI_DRY
                in -0.1..0.1 -> Humidity.MEDIUM
                in 0.1..0.3 -> Humidity.SEMI_WET
                in 0.3..1.0 -> Humidity.WET
                else -> Humidity.MEDIUM
            }

            return Pair(humidity, humidity.ordinal + ((hum - humidity.lowerBound) / (humidity.upperBound - humidity.lowerBound)))
        }
    }

    fun getErosion(x: Double, z: Double): Int {
        synchronized(erosion) {
            val ero = erosion.evaluateNoise(x, 0.0, z)

            return when (ero) {
                in -1.0..-0.78 -> 0
                in -0.78..-0.375 -> 1
                in -0.375..-0.2225 -> 2
                in -0.2225..0.05 -> 3
                in 0.05..0.45 -> 4
                in 0.45..0.55 -> 5
                in 0.55..1.0 -> 6
                else -> -1
            }
        }
    }

    fun getBiome(temp: Temperature, hum: Humidity, cont: Continentalness, weird: Double): DynamicRegistry.Key<Biome> {
        val biome = if (cont == Continentalness.OCEAN) {
            when (temp) {
                Temperature.FROZEN -> Biome.FROZEN_OCEAN
                Temperature.COLD -> Biome.COLD_OCEAN
                Temperature.MEDIUM -> Biome.OCEAN
                Temperature.WARM -> Biome.LUKEWARM_OCEAN
                Temperature.HOT -> Biome.WARM_OCEAN
                else -> throw IllegalArgumentException("Invalid temperature")
            }
        } else if (cont == Continentalness.DEEP_OCEAN) {
            when (temp) {
                Temperature.FROZEN -> Biome.DEEP_FROZEN_OCEAN
                Temperature.COLD -> Biome.DEEP_COLD_OCEAN
                Temperature.MEDIUM -> Biome.DEEP_OCEAN
                Temperature.WARM -> Biome.DEEP_LUKEWARM_OCEAN
                Temperature.HOT -> Biome.WARM_OCEAN
                else -> throw IllegalArgumentException("Invalid temperature")
            }
        } else if (cont == Continentalness.MUSHROOM_FIELDS) {
            Biome.MUSHROOM_FIELDS
        } else if (cont == Continentalness.COAST) {
            when (temp) {
                Temperature.FROZEN -> Biome.SNOWY_BEACH
                Temperature.COLD, Temperature.MEDIUM, Temperature.WARM -> Biome.BEACH
                Temperature.HOT -> Biome.DESERT
                else -> throw IllegalArgumentException("Invalid temperature")
            }
        } else  {
            when (temp) {
                Temperature.FROZEN -> {
                    when (hum) {
                        Humidity.DRY -> if (weird < 0) Biome.SNOWY_PLAINS else Biome.ICE_SPIKES
                        Humidity.SEMI_DRY -> Biome.SNOWY_PLAINS
                        Humidity.MEDIUM -> if (weird < 0) Biome.SNOWY_PLAINS else Biome.SNOWY_TAIGA
                        Humidity.SEMI_WET -> Biome.SNOWY_TAIGA
                        Humidity.WET -> Biome.TAIGA
                        else -> throw IllegalArgumentException("Invalid humidity")
                    }
                }
                Temperature.COLD -> {
                    when (hum) {
                        Humidity.DRY, Humidity.SEMI_DRY -> Biome.PLAINS
                        Humidity.MEDIUM -> Biome.FOREST
                        Humidity.SEMI_WET -> Biome.TAIGA
                        Humidity.WET -> if (weird < 0) Biome.OLD_GROWTH_SPRUCE_TAIGA else Biome.OLD_GROWTH_PINE_TAIGA
                        else -> throw IllegalArgumentException("Invalid humidity")
                    }
                }
                Temperature.MEDIUM -> {
                    when (hum) {
                        Humidity.DRY -> if (weird < 0) Biome.FLOWER_FOREST else Biome.SUNFLOWER_PLAINS
                        Humidity.SEMI_DRY -> Biome.PLAINS
                        Humidity.MEDIUM -> Biome.FOREST
                        Humidity.SEMI_WET ->  if (weird < 0) Biome.BIRCH_FOREST else Biome.OLD_GROWTH_BIRCH_FOREST
                        Humidity.WET -> Biome.DARK_FOREST
                        else -> throw IllegalArgumentException("Invalid humidity")
                    }
                }
                Temperature.WARM -> {
                    when (hum) {
                        Humidity.DRY, Humidity.SEMI_DRY -> Biome.SAVANNA
                        Humidity.MEDIUM -> if (weird < 0) Biome.FOREST else Biome.PLAINS
                        Humidity.SEMI_WET -> if (weird < 0) Biome.JUNGLE else Biome.SPARSE_JUNGLE
                        Humidity.WET -> if (weird < 0) Biome.JUNGLE else Biome.BAMBOO_JUNGLE
                        else -> throw IllegalArgumentException("Invalid humidity")
                    }
                }
                Temperature.HOT -> Biome.DESERT
                else -> throw IllegalArgumentException("Invalid temperature")
            }
        }

        return biome
    }
}