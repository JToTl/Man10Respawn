package ltotj.minecraft.man10respawn.utilities.TextManager

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class TextManager(text: String){

    private val builder=ComponentBuilder(text)
    private var format=""

    fun black(): TextManager {
        format+="§0"
        return this
    }

    fun darkblue(): TextManager {
        format+="§1"
        return this
    }

    fun darkgreen(): TextManager {
        format+="§2"
        return this
    }

    fun darkaqua(): TextManager {
        format+="§3"
        return this
    }

    fun darkred(): TextManager {
        format+="§4"
        return this
    }

    fun darkpurple(): TextManager {
        format+="§5"
        return this
    }

    fun gold(): TextManager {
        format+="§6"
        return this
    }

    fun gray(): TextManager {
        format+="§7"
        return this
    }

    fun darkgray(): TextManager {
        format+="§8"
        return this
    }

    fun blue(): TextManager {
        format+="§9"
        return this
    }

    fun green(): TextManager {
        format+="§a"
        return this
    }

    fun aqua(): TextManager {
        format+="§b"
        return this
    }

    fun red(): TextManager {
        format+="§c"
        return this
    }

    fun lightpurple(): TextManager {
        format+="§d"
        return this
    }

    fun yellow(): TextManager {
        format+="§e"
        return this
    }

    fun white(): TextManager {
        format+="§f"
        return this
    }

    fun obfuscated(): TextManager {
        format+="§k"
        return this
    }

    fun bold(): TextManager {
        format="§l"
        return this
    }

    fun strikethrough(): TextManager {
        format="§m"
        return this
    }

    fun underline(): TextManager {
        format="§n"
        return this
    }

    fun italic(): TextManager {
        format="§o"
        return this
    }

    fun reset(): TextManager {
        format="§r"
        return this
    }

    fun addText(text:String): TextManager {
        builder.append("$format$text")
                .event(HoverEvent(HoverEvent.Action.SHOW_TEXT,Text("")))
                .event(ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,""))
        format=""
        return this
    }

    fun newLine(): TextManager {
        builder.append("\n")
        return this
    }

    fun clickRunCommand(commandWithoutSlash:String): TextManager {
        builder.event(ClickEvent(ClickEvent.Action.RUN_COMMAND,"/$commandWithoutSlash"))
        return this
    }

    fun clickSuggestCommand(commandWithoutSlash:String): TextManager {
        builder.event(ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/$commandWithoutSlash"))
        return this
    }

    fun clickOpenURL(url:String): TextManager {
        builder.event(ClickEvent(ClickEvent.Action.OPEN_URL,url))
        return this
    }

    fun hoverShowText(text:String): TextManager {
        builder.event(HoverEvent(HoverEvent.Action.SHOW_TEXT,Text(text)))
        return this
    }

    fun hoverShowText(text:MutableList<String>): TextManager {
        val list= mutableListOf<Text>()
        for(str in text){
            list.add(Text(str))
        }
        builder.event(HoverEvent(HoverEvent.Action.SHOW_TEXT,list as List<Text>))
        return this
    }

    fun hoverShowText(text: TextManager): TextManager {
        builder.event(HoverEvent(HoverEvent.Action.SHOW_TEXT,text.builder.create()))
        return this
    }

    fun send(player: Player){
        player.sendMessage(*builder.create())
    }

    fun send(sender:CommandSender){
        sender.sendMessage(*builder.create())
    }

}