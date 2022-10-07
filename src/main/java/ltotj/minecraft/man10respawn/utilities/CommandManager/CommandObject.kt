package ltotj.minecraft.man10respawn.utilities.CommandManager

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.lang.StringBuilder
import java.util.*
import java.util.function.Consumer

class CommandObject{

    private var argNum = 1
    private val nextObjects = ArrayList<CommandObject>()
    private var nullableNextArgument = false
    private var function:Consumer<Pair<CommandSender,Array<out String>>>?=null
    private var errorMessage=arrayOf("§4エラーメッセージ未設定")
    private var useCommentOnTab=false

    private val allowedType= ArrayList<CommandArgumentType>()
    private val arguments=ArrayList<String>()
    private var comment:String?=null
    private var hide=false
    private var permission:String?=null
    private var explanation=""
    private var onlyPlayer=false

    constructor(argument:String){
        this.arguments.add(argument)
    }

    constructor(argumentType: CommandArgumentType){
        allowedType.add(argumentType)
    }

    constructor(argumentTypes: Array<CommandArgumentType>){
        allowedType.addAll(argumentTypes)
    }

    constructor(arguments:MutableSet<String>,comment:String){
        this.arguments.addAll(arguments)
        this.comment="<$comment>"
    }

    private fun getTypeString(commandArgumentType: CommandArgumentType):String{
        return when(commandArgumentType){
            CommandArgumentType.INT, CommandArgumentType.Long -> "整数"
            CommandArgumentType.BOOLEAN -> "true/false"
            CommandArgumentType.STRING -> "文字列"
            CommandArgumentType.ONLINE_PlAYER -> "プレイヤー名"
            CommandArgumentType.DOUBLE -> "数字"
        }
    }

    fun hide():Boolean{
        return hide
    }


    fun addAllowedArgType(commandArgumentType: CommandArgumentType): CommandObject {
        allowedType.add(commandArgumentType)
        comment+="$commandArgumentType"
        return this
    }

    fun addNextArgument(commandObject: CommandObject): CommandObject {
        nextObjects.add(commandObject)
        commandObject.addArgNum(argNum)
        return this
    }

    private fun addArgNum(argNum:Int){
        this.argNum+=argNum
        if(nextObjects.isNotEmpty()){
            for(cobj in nextObjects){
                cobj.addArgNum(argNum)
            }
        }
    }


    fun hasPermission(sender:CommandSender):Boolean{
        return if(permission==null) true else sender.hasPermission(permission!!)
    }

    fun getArgument():MutableList<String>{
        val list= mutableListOf<String>()
        if(hide){
            return list
        }
        if(comment!=null&&useCommentOnTab){
            list.add(comment!!)
        }
        else if(arguments.isNotEmpty()){
            list.addAll(arguments)
        }
        else{
            if(allowedType.contains(CommandArgumentType.INT))list.add("整数")
            if(allowedType.contains(CommandArgumentType.BOOLEAN)){
                list.add("true")
                list.add("false")
            }
            if(allowedType.contains(CommandArgumentType.DOUBLE))list.add("数字")
            if(allowedType.contains(CommandArgumentType.STRING))list.add("文字列")
            if(allowedType.contains(CommandArgumentType.ONLINE_PlAYER)){
                for(player in Bukkit.getOnlinePlayers()){
                    list.add(player.name)
                }
            }
        }
        return list
    }

    private fun getNextArguments():MutableList<String> {
        val list = mutableListOf<String>()
        for (next in nextObjects) {
            if(!next.hide) {
                if (next.comment != null) {
                    list.add(next.comment!!)
                } else {
                    for (type in next.allowedType) {
                        list.add(getTypeString(type))
                    }
                    list.addAll(next.arguments)
                }
            }
        }
        return list
    }

    fun getExplanation():String{
        val message=StringBuilder()

        message.append(" §e${if(comment!=null) comment else if(arguments.isNotEmpty()&&arguments.size==1) arguments[0] else getArgument().toString()}")
        if(nextObjects.size<2){
            return if(nextObjects.isEmpty()){
                message.append(" §d-> $explanation")
                message.toString()
            } else{
                message.append(nextObjects[0].getExplanation())
                message.toString()
            }
        }
        return message.append(" ${getNextArguments()} ... §d-> §e$explanation").toString()
    }

    fun match(string: String):Boolean{
        if(arguments.isEmpty()) {
            if (allowedType.contains(CommandArgumentType.STRING)) return true
            if (allowedType.contains(CommandArgumentType.DOUBLE) && string.toDoubleOrNull() != null) return true
            if (allowedType.contains(CommandArgumentType.INT) && string.toIntOrNull() != null) return true
            if (allowedType.contains(CommandArgumentType.Long)&&string.toLongOrNull()!=null) return true
            if (allowedType.contains(CommandArgumentType.BOOLEAN) && (string == "true" || string == "false")) return true
            if (allowedType.contains(CommandArgumentType.ONLINE_PlAYER)) {
                return Bukkit.getPlayer(string) != null
            }
            return false
        }
        else return arguments.contains(string)
    }

    fun addArgument(string: String): CommandObject {
        arguments.add(string)
        return this
    }

    fun setArguments(arguments:MutableSet<String>): CommandObject {
        this.arguments.clear()
        this.arguments.addAll(arguments)
        return this
    }

    fun setComment(string: String): CommandObject {
        useCommentOnTab=true
        comment="<$string>"
        return this
    }

    fun setCommentOnTab(boolean: Boolean): CommandObject {
        useCommentOnTab=boolean
        return this
    }

    fun setHidden(boolean: Boolean): CommandObject {
        hide=boolean
        return this
    }

    fun setPermission(permission:String): CommandObject {
        this.permission=permission
        return this
    }

    fun setNullable(boolean: Boolean): CommandObject {
        nullableNextArgument = boolean
        return this
    }

    fun setExplanation(string: String): CommandObject {
        explanation="§d${string}"
        return this
    }

    fun setErrorMessage(string:String): CommandObject {
        errorMessage= arrayOf(string)
        return this
    }

    fun setErrorMessage(array:Array<String>): CommandObject {
        errorMessage=array
        return this
    }

    fun setFunction(consumer: Consumer<Pair<CommandSender,Array<out String>>>): CommandObject {
        function=consumer
        return this
    }

    fun setOnlyPlayer(boolean: Boolean): CommandObject{
        this.onlyPlayer=boolean
        return this
    }

    fun sendErrorMessage(sender: CommandSender){
        for(message in errorMessage){
            sender.sendMessage(message)
        }
    }

    fun getTabComplete(sender: CommandSender,args:Array<String>):MutableList<String>{
        val list = mutableListOf<String>()
        if(args.size-1==argNum){
            for (commandObject in nextObjects) {
                if(commandObject.hasPermission(sender))list.addAll(commandObject.getArgument())
            }
        }
        else{
            for(commandObject in nextObjects){
                if(commandObject.match(args[argNum])){
                    if(commandObject.hasPermission(sender))list.addAll(commandObject.getTabComplete(sender,args))
                }
            }
        }
        return list
    }

    fun execute(sender: CommandSender, args: Array<out String>) {
        if(onlyPlayer&&sender !is Player){
            sender.sendMessage("§4このコマンドはプレイヤーのみ実行可能です")
            return
        }
        if (args.size == argNum) {
            if(nullableNextArgument||nextObjects.isEmpty()) {
                if (function == null) {
                    sender.sendMessage("§4[エラー]コマンドの処理が実装されていません")
                    return
                }
                else{
                    function!!.accept(Pair(sender,args))
                    return
                }
            }
            else{
                sendErrorMessage(sender)
            }
        }else{
            for(commandObject in nextObjects){
                if(commandObject.match(args[argNum])){
                    commandObject.execute(sender,args)
                    return
                }
            }
            sendErrorMessage(sender)
        }
    }
}