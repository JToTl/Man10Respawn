package ltotj.minecraft.man10respawn.utilities.ConfigManager

import org.apache.commons.lang.mutable.Mutable
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.logging.Level

class ConfigManager {

    private lateinit var config:FileConfiguration
    private var file: File
    var filename: String
    private var plugin: JavaPlugin
    private var dir=""

    constructor(plugin: JavaPlugin):this(plugin, "config"){
    }

    constructor(plugin: JavaPlugin, filename: String){
        this.filename="$filename.yml"
        this.plugin=plugin
        this.file=File(plugin.dataFolder, "$filename.yml")
        load()
    }

    constructor(plugin: JavaPlugin, filename: String, dir: String){
        this.dir=dir
        this.filename="$filename.yml"
        this.plugin=plugin
        this.file=File(File(plugin.dataFolder, File.separator + dir), "${File.separator}$filename.yml")
        if(!file.exists()){
            file.createNewFile()
        }
        load()
    }

    fun load(){
        saveDefConfig()
        config=YamlConfiguration.loadConfiguration(file)
        val defConfigStream=plugin.getResource(filename)?:return
        config.setDefaults(YamlConfiguration.loadConfiguration(InputStreamReader(defConfigStream, StandardCharsets.UTF_8)))
    }

    fun delete():Boolean{
        return file.delete()
    }

    fun getConfig():FileConfiguration{
        return config
    }

    fun getString(key: String):String?{
        return when(val retValue=config.getString(key)){
            null -> {
                plugin.logger.info("コンフィグから${key}の値をとるのに失敗しました")
                null
            }
            else -> retValue
        }
    }

    fun getString(key: String,def:String):String?{
        return config.getString(key,def)
    }

    fun getBoolean(key: String):Boolean{
        return config.getBoolean(key)
    }

    fun getBoolean(key: String,def:Boolean):Boolean{
        return config.getBoolean(key,def)
    }

    fun getInt(key: String):Int{
        return config.getInt(key)
        }

    fun getInt(key: String,def:Int):Int{
        return config.getInt(key,def)
    }

    fun getLong(key: String):Long{
        return  config.getLong(key)
    }

    fun getLong(key: String,def:Long):Long{
        return  config.getLong(key,def)
    }

    fun getDouble(key: String):Double{
        return config.getDouble(key)
    }

    fun getDouble(key: String,def:Double):Double{
        return config.getDouble(key,def)
    }

    fun getList(key: String):MutableList<*>{
        return config.getList(key)?:mutableListOf(-1)
    }

    fun getList(key: String,def:MutableList<*>): MutableList<*>? {
        return config.getList(key,def)
    }

    fun getStringList(key: String):MutableList<String> {
        return config.getStringList(key)
    }

    fun getKeys(key: String, depth: Boolean):Set<String>{
        return when(val retValue=config.getConfigurationSection(key)?.getKeys(depth)){
            null -> {
                plugin.logger.info("コンフィグから${key}のkeySetをとるのに失敗しました")
                setOf("")
            }
            else-> retValue
        }
    }

    fun getConfigurationSection(key: String):ConfigurationSection?{
        return config.getConfigurationSection(key)
    }

    fun save(){
        config.save(file)
    }

    fun saveDefConfig(){
        if(!file.exists()){
            plugin.saveResource("${dir}/${filename}", false)
        }
        if(!file.exists()){
            file.createNewFile()
        }
    }

    fun setValue(path: String, any: Any?){
        config.set(path, any)
        save()
    }

    fun saveConfig(){
        try{
            config.save(file)
        }catch (e: IOException){
            plugin.logger.log(Level.SEVERE, "コンフィグをセーブできませんでした")
        }
    }
}