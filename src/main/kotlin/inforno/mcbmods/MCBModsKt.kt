package inforno.mcbmods

import inforno.mcbmods.MCBMods.mc
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Temporary file before porting over to Kotlin
 */

class MCBModsKt {

    companion object {
        val json = Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }

        val configDir by lazy {
            File(File(mc.mcDataDir, "config"), "mcbmods").also {
                it.mkdirs()
                File(it, "trackers").mkdirs()
            }
        }
    }
}