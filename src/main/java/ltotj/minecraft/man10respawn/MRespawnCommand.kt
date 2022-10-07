package ltotj.minecraft.man10respawn

import ltotj.minecraft.man10respawn.Main
import ltotj.minecraft.man10respawn.utilities.CommandManager.CommandArgumentType
import ltotj.minecraft.man10respawn.utilities.CommandManager.CommandManager
import ltotj.minecraft.man10respawn.utilities.CommandManager.CommandObject
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.plugin.Plugin

class MRespawnCommand(val plugin:Main,alias:String,val pluginTitle:String): CommandManager(plugin,alias,pluginTitle) {


    val deleteObject=CommandObject(Main.respawnFiles.keys,"保存名")
        .setExplanation("指定のリスポーン設定を削除")
        .setFunction{
            plugin.deleteFile(it.first,it.second[1])
            reloadObject()
        }

    val showObject=CommandObject(Main.respawnFiles.keys,"保存名")
        .setExplanation("指定のリスポーン設定を表示")
        .setFunction{
            plugin.showFile(it.first,it.second[1])
            reloadObject()
        }

    fun reloadObject(){
        deleteObject.setArguments(Main.respawnFiles.keys)
        showObject.setArguments(Main.respawnFiles.keys)
    }

    init {

        setPermission("mrespawn.admin")

        addFirstArgument(
            CommandObject("delete")
                .addNextArgument(deleteObject)
        )

        addFirstArgument(
            CommandObject("show")
                .addNextArgument(showObject)
        )

        addFirstArgument(
            CommandObject("reload")
                .setExplanation("コンフィグをリロード")
                .setFunction{
                    Main.con=plugin.config
                    plugin.loadRespawnFile()
                    reloadObject()
                }
        )

        addFirstArgument(
            CommandObject("off")
                .setExplanation("プラグインの無効化")
                .setFunction{
                    if(Main.enable){
                        HandlerList.unregisterAll(plugin as Plugin)
                        Main.enable=false
                        plugin.config.set("enable",false)
                        plugin.saveConfig()
                        plugin.sendMessage(it.first,"オフにしました")
                    }
                    else{
                        plugin.sendMessage(it.first,"既にオフです")
                    }
                }
        )

        addFirstArgument(
            CommandObject("on")
                .setExplanation("プラグインの有効化")
                .setFunction{
                    if(Main.enable){
                        plugin.sendMessage(it.first,"既にオンです")
                    }
                    else{
                        plugin.server.pluginManager.registerEvents(plugin,plugin)
                        Main.enable=true
                        plugin.config.set("enable",true)
                        plugin.saveConfig()
                        plugin.sendMessage(it.first,"オンにしました")
                    }
                }
        )

        addFirstArgument(
            CommandObject("setGeneral")
                .setExplanation("基本リスポーン位置を指定")
                .setOnlyPlayer(true)
                .setFunction{
                    plugin.setGeneral(it.first as Player)
                }
        )

        addFirstArgument(
            CommandObject("softOn")
                .setExplanation("リスポーン直後にリスポーン位置にTPするように変更")
                .setFunction{
                    if(Main.softEnable){
                        plugin.sendMessage(it.first,"既にオンです")
                    }
                    else{
                        Main.enable=true
                        plugin.config.set("softEnable",true)
                        plugin.saveConfig()
                        plugin.sendMessage(it.first,"オンにしました")
                    }
                }
        )

        addFirstArgument(
            CommandObject("softOff")
                .setExplanation("リスポーン直後にリスポーン位置にTPしないように変更")
                .setFunction{
                    if(Main.softEnable){
                        Main.enable=true
                        plugin.config.set("softEnable",true)
                        plugin.saveConfig()
                        plugin.sendMessage(it.first,"オフにしました")
                    }
                    else{
                        plugin.sendMessage(it.first,"既にオフです")
                    }
                }
        )

        addFirstArgument(
            CommandObject("set")
                .addNextArgument(
                    CommandObject(CommandArgumentType.STRING)
                        .setComment("保存名")
                        .addNextArgument(
                            CommandObject(CommandArgumentType.STRING)
                                .setComment("ワールド名")
                                .setNullable(true)
                                .setOnlyPlayer(true)
                                .setFunction{
                                    val player=it.first as Player
                                    plugin.setRespawn(player,it.second[1],it.second[2])
                                    reloadObject()
                                }
                                .addNextArgument(
                                    CommandObject(CommandArgumentType.STRING)
                                        .setExplanation("<ワールド名>の<リージョン名(なくても良い)>で死んだ場合に、今立っている地点にリスポーンするように設定する")
                                        .setComment("リージョン名")
                                        .setNullable(true)
                                        .setFunction{
                                            val player=it.first as Player
                                            plugin.setRespawn(player,it.second[1],it.second[2],it.second[3])
                                            reloadObject()
                                        }
                                )
                        )
                )
        )

    }

}