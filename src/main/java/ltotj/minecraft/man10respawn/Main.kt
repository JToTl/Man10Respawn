package ltotj.minecraft.man10respawn

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.regions.RegionContainer
import ltotj.minecraft.man10respawn.utilities.ConfigManager.ConfigManager
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class Main : JavaPlugin(),Listener {

    companion object{

        lateinit var plugin: JavaPlugin
        const val pluginTitle="§f§l[§dMan10Respawn§f§l]§r"
        lateinit var worldGuard:RegionContainer
        lateinit var con:FileConfiguration
        var enable=true
        var softEnable=true
        val respawnMap=HashMap<String,Location>()
        val respawnPlayers=HashMap<Player,Location>()
        val respawnFiles=HashMap<String,ConfigManager>()
        var generalRespawn:Location?=null

    }


    override fun onEnable() {
        // Plugin startup logic
        plugin=this
        worldGuard=WorldGuard.getInstance().platform.regionContainer
        con=config
        enable=config.getBoolean("enable")
        server.pluginManager.registerEvents(this,this)
        loadRespawnFile()
        MRespawnCommand(this,"mspawn", pluginTitle)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    fun sendMessage(sender: CommandSender, message:String){
        sender.sendMessage("${pluginTitle}${message}")
    }

    fun loadRespawnFile(){
        respawnMap.clear()
        respawnFiles.clear()
        val folder= File("${plugin.dataFolder.absolutePath}${File.separator}respawn")
        folder.mkdir()
        folder.listFiles()?.forEach { file->
            if(file.extension!="yml")return@forEach
            val config=ConfigManager(plugin,file.nameWithoutExtension,"respawn")
            loadRespawn(config)
            respawnFiles[file.nameWithoutExtension]=config
        }
        val world=Bukkit.getWorld(con.getString("general.world")?:"")?:return
        generalRespawn= Location(world,con.getDouble("general.x"),con.getDouble("general.y"),con.getDouble("general.z"),con.getDouble("general.yaw").toFloat(),con.getDouble("general.pitch").toFloat())
    }

    fun deleteFile(sender:CommandSender,name:String){
        if(respawnFiles.containsKey(name)){
            respawnFiles[name]?.delete()
            sendMessage(sender,"§a削除しました")
            return
        }
        sendMessage(sender,"§c存在しません")
    }

    fun showFile(sender:CommandSender,name:String){
        if(respawnFiles.containsKey(name)){
            sendMessage(sender,"§e${name}の設定")
            sendMessage(sender,"§c${respawnFiles[name]!!.getString("originWorld")}${if(respawnFiles[name]!!.getString("originRegion")!=null)respawnFiles[name]!!.getString("originRegion") else ""}§aで死亡すると" +
                    "§c${respawnFiles[name]!!.getString("destinationWorld")}、${respawnFiles[name]!!.getString("x")}、${respawnFiles[name]!!.getString("y")}、${respawnFiles[name]!!.getString("z")}§aにリスポーンする")
            return
        }
        sendMessage(sender,"§c存在しません")
    }

    fun setGeneral(player:Player){
        val loc=player.location
        config.set("general.world",player.world.name)
        config.set("general.x",loc.x)
        config.set("general.y",loc.y)
        config.set("general.z",loc.z)
        config.set("general.yaw",loc.yaw)
        config.set("general.pitch",loc.pitch)
        plugin.saveConfig()
        generalRespawn=loc
        sendMessage(player,"§a基礎リスポーン地点を設定しました")
    }

    fun setRespawn(player:Player,fileName:String,originWorld:String,region:String?=null){
        val location=player.location
        var key=originWorld
        if(region!=null){
            key+=",${region}"
        }
        if(respawnMap.containsKey(key)){
            sendMessage(player,"§c${key}は既に設定されています")
            return
        }
        val config= ConfigManager(plugin,fileName,"respawn")
        config.setValue("destinationWorld",location.world.name)
        config.setValue("x",location.x)
        config.setValue("y",location.y)
        config.setValue("z",location.z)
        config.setValue("yaw",location.yaw)
        config.setValue("pitch",location.pitch)
        config.setValue("originWorld",originWorld)
        config.setValue("originRegion",region)
        config.save()
        respawnFiles[fileName]=config
        respawnMap[key]=location
        sendMessage(player,"§a${key}のリスポーンを設定しました")
    }

    fun loadRespawn(config:ConfigManager){
        val world=Bukkit.getWorld(config.getString("destinationWorld")?:"")?:return
        val loc=Location(world,config.getDouble("x"),config.getDouble("y"),config.getDouble("z"),config.getDouble("yaw").toFloat(),config.getDouble("pitch").toFloat())
        respawnMap[if(config.getString("originRegion")==null)world.name else "${world.name},${config.getString("originRegion")}"]=loc
    }

    @EventHandler
    fun death(e:PlayerDeathEvent){
        val loc=e.player.location
        val regions=worldGuard.get(BukkitAdapter.adapt(loc.world))?.getApplicableRegions(BlockVector3.at(loc.x,loc.y,loc.z))?:return
        for(region in regions.regions){
            if(respawnMap.containsKey("${loc.world.name},${region.id}")){
                respawnPlayers[e.player]= respawnMap["${loc.world.name},${region.id}"]!!
                return
            }
        }
        if(respawnMap.containsKey(loc.world.name)){
            respawnPlayers[e.player]= respawnMap[loc.world.name]!!
            return
        }
    }

    @EventHandler
    fun respawn(e:PlayerRespawnEvent){
        if(respawnPlayers.containsKey(e.player)){
            e.respawnLocation=respawnPlayers[e.player]!!
            if(softEnable) {
                Bukkit.getScheduler().runTask(this,
                    Runnable {
                        e.player.teleport(respawnPlayers[e.player]!!)
                        respawnPlayers.remove(e.player)
                    })
            }
            return
        }
        e.respawnLocation=generalRespawn?:return
        if(softEnable) {
            Bukkit.getScheduler().runTask(this, Runnable {
                e.player.teleport(generalRespawn ?: return@Runnable)
            })
        }
    }

    @EventHandler
    fun logout(e:PlayerQuitEvent){
        respawnPlayers.remove(e.player)
    }

}