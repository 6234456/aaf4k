package eu.qiou.aaf4k.util.io

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import javax.script.Invocable
import javax.script.ScriptEngineManager

class JSExecutor(private val script: String) {
    companion object {
        private val cache = mutableMapOf<String, Invocable>()

        fun clearCache() {
            cache.clear()
        }
    }

    private val engine = ScriptEngineManager().getEngineByExtension("js")

    private val content: Invocable = if (cache.containsKey(script)) cache[script]!! else Files.newBufferedReader(Paths.get(script), StandardCharsets.UTF_8).let {
        engine.eval(it)
        (engine as Invocable).apply {
            cache[script] = this
        }
    }


    fun invoke(func: String, vararg params: Any): Any {
        return content.invokeFunction(func, * params)  // the vararg to java add *
    }

}