package ltotj.minecraft.man10respawn.utilities.CommandManager

import ltotj.minecraft.man10respawn.utilities.TextManager.TextManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.function.Consumer

open class CommandManager(plugin:JavaPlugin,val alias: String, private val pluginTitle:String):CommandExecutor,TabCompleter{


    private val commandList=ArrayList<CommandObject>()
    private var function: Consumer<CommandSender>?=null
    private var permission:String?=null
    private var onlyPlayer=false

    init{
        plugin.getCommand(alias)!!.setExecutor(this)
        addFirstArgument(
            CommandObject("help")
                .setFunction{
                    sendHelpMessage(it.first)
                }
        )
    }


    fun addFirstArgument(commandObject: CommandObject): CommandManager {
        commandList.add(commandObject)
        return this
    }

    fun setFunction(consumer: Consumer<CommandSender>): CommandManager {
        function=consumer
        return this
    }

    fun setPermission(permission:String): CommandManager {
        this.permission=permission
        return this
    }

    fun setOnlyPlayer(boolean: Boolean): CommandManager{
        this.onlyPlayer=boolean
        return this
    }

    private fun sendHelpMessage(sender:CommandSender){
        sender.sendMessage("§d==================[§a${pluginTitle}§d]==================")
        for(commandObject in commandList) {
            if (!commandObject.hide()) {
                TextManager("§e/${alias}")
                        .addText(commandObject.getExplanation())
                        .send(sender)
            }
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>):Boolean {
        if(permission!=null&&!sender.hasPermission(permission!!)){
            sender.sendMessage("§4このコマンドを実行する権限がありません")
            return true
        }
        if(onlyPlayer&&sender !is Player){
            sender.sendMessage("§4このコマンドはプレイヤーのみ実行可能です")
            return true
        }
        if(args.isEmpty()){
            //空だったときの処理
            //↑これ書かなくてもわかるくない？
            if(function!=null){
                function!!.accept(sender)
            }
            return true
        }
        for(commandObject in commandList){
            if(commandObject.match(args[0])){
                if(commandObject.hasPermission(sender)) {
                    commandObject.execute(sender, args)
                }
                else{
                    sender.sendMessage("§4このコマンドを実行する権限がありません")
                }
                return true
            }
        }
        //コマンドが存在しないときの処理
        sendHelpMessage(sender)
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        val list= mutableListOf<String>()
        if(alias==this.alias) {
            if (args.size==1) {
                for (commandObject in commandList) {
                    if(commandObject.hasPermission(sender))list.addAll(commandObject.getArgument())
                }
            } else {
                for (commandObject in commandList) {
                    if (commandObject.match(args[0])) {
                        if(commandObject.hasPermission(sender))list.addAll(commandObject.getTabComplete(sender,args as Array<String>))
                    }
                }
            }
        }
        return list
    }

}