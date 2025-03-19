package de.infinityprojects.mcserver.server

import de.infinityprojects.mcserver.data.WorldSpawn
import de.infinityprojects.mcserver.ui.ScoreboardManager
import de.infinityprojects.mcserver.ui.TablistManager
import de.infinityprojects.mcserver.utils.SERVER_BRAND
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.TagStringIOExt
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.minestom.server.MinecraftServer
import net.minestom.server.advancements.FrameType
import net.minestom.server.advancements.Notification
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import org.slf4j.LoggerFactory
import java.io.File

class PlayerManager {
    private val logger = LoggerFactory.getLogger("PlayerManager")
    private val players = hashMapOf<String, Player>() // replaceable with Audiences
    private val notification =
        Notification(
            Component
                .text("Welcome to $SERVER_BRAND!")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD),
            FrameType.CHALLENGE,
            ItemStack.of(Material.RAW_GOLD_BLOCK),
        )

    val tablistManager = TablistManager()
    val scoreboardManager = ScoreboardManager()

    init {
        MinecraftServer.getConnectionManager().setPlayerProvider { player, user ->
            Player(player, user)
        }

        File("players").mkdirs()

        val node = EventNode.type("player_manager", EventFilter.PLAYER)
        MinecraftServer.getGlobalEventHandler().addChild(node)

        node.addListener(AsyncPlayerConfigurationEvent::class.java, ::configurationEvent)
        node.addListener(PlayerSpawnEvent::class.java, ::spawnEvent)
        node.addListener(PlayerDisconnectEvent::class.java, ::disconnectEvent)
    }

    fun configurationEvent(event: AsyncPlayerConfigurationEvent) {
        val player = event.player
        val playerFile = File("players/${player.uuid}.dat")

        if (playerFile.exists()) {
            val serial = playerFile.readText()
            val tags = TagStringIOExt.readTag(serial) as CompoundBinaryTag
            player.tagHandler().updateContent(tags)
        }

        val inventoryFile = File("players/${player.uuid}-inventory.dat")
        if (inventoryFile.exists()) {
            val nbtString = inventoryFile.readText()
            val nbtCompound = TagStringIOExt.readTag(nbtString) as CompoundBinaryTag
            nbtCompound.forEach { (key, value) ->
                if (value is CompoundBinaryTag) {
                    val itemStack = ItemStack.fromItemNBT(value)
                    player.inventory.setItemStack(key.toInt(), itemStack)
                } else {
                    logger.error("Invalid item stack in inventory file")
                }
            }
        }

        val world = MeliusServer.worldManager.getDefaultWorld()
        event.spawningInstance = world
        val worldName = MeliusServer.worldManager.getWorldName(world)
        player.respawnPoint = getSpawn(player, worldName)

        players[player.username] = player
        broadcast(
            Component
                .empty()
                .append(Component.text("[+] ").color(NamedTextColor.GREEN))
                .append(Component.text(player.username).color(NamedTextColor.GOLD))
                .append(Component.text(" joined the server").color(NamedTextColor.YELLOW)),
        )
    }

    fun spawnEvent(event: PlayerSpawnEvent) {
        val player = event.player
        player.sendNotification(notification)

        tablistManager.sendTabList(player)
        scoreboardManager.sendScoreboard(player)
    }

    fun disconnectEvent(event: PlayerDisconnectEvent) {
        players.remove(event.player.username)
        val tags = event.player.tagHandler().asCompound()
        val serial = TagStringIOExt.writeTag(tags)

        val playerFile = File("players/${event.player.uuid}.dat")
        playerFile.writeText(serial)

        val nbtCompound = CompoundBinaryTag.empty()
        event.player.inventory.itemStacks.forEachIndexed { i, t ->
            nbtCompound.put(i.toString(), t.toItemNBT())
        }

        val nbtString = TagStringIOExt.writeTag(nbtCompound)

        val inventoryFile = File("players/${event.player.uuid}-inventory.dat")
        inventoryFile.writeText(nbtString)

        broadcast(
            Component
                .empty()
                .append(Component.text("[-] ").color(NamedTextColor.RED))
                .append(Component.text(event.player.username).color(NamedTextColor.GOLD))
                .append(Component.text(" left the server").color(NamedTextColor.YELLOW)),
        )
    }

    fun broadcast(message: Component) {
        Audiences.all().sendMessage(message)
    }

    fun setSpawn(
        player: Player,
        worldName: String,
        coords: Pos,
    ) {
        val worldSpawn = WorldSpawn(coords)
        player.setTag(WorldSpawn.getTag(worldName), worldSpawn)
    }

    fun getSpawn(
        player: Player,
        worldName: String,
    ): Pos {
        val worldSpawn = player.getTag(WorldSpawn.getTag(worldName))

        if (worldSpawn == null) {
            val world = MeliusServer.worldManager.getWorld(worldName) ?: throw IllegalArgumentException("World not found")
            val spawn = MeliusServer.worldManager.getSpawn(world)
            return spawn
        }

        return worldSpawn.pos
    }
}
