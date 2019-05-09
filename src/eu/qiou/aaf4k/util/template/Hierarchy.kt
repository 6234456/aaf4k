package eu.qiou.aaf4k.util.template

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

//the default reference index comply with the numeration of list
//the hierarchy is separated by period
class Hierarchy(private val name: String, private var index: String = "A0") {
    companion object {
        private var unifyFileName: (String) -> String = { "${GlobalConfiguration.DEFAULT_PROJECT_NAME}_${GlobalConfiguration.DEFAULT_REPORTING_ENTITY.abbreviation}_${it.replace("""[*$%|/\\]+""".toRegex(), "_")}".take(100) }
    }

    class Doc(val template: WorkingPaper, fileName: String, index: String) {
        val fileName = "$index " + if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) fileName
        else "$fileName.xlsx"
    }

    private val subHierarchies: MutableList<Hierarchy> = mutableListOf()
    private val documents: MutableList<Doc> = mutableListOf()
    private var cnt = 1

    fun document(index: String? = null, name: String, tmpl: KClass<out WorkingPaper>, init: WorkingPaper.() -> Unit): Hierarchy {
        val n = unifyFileName(name)
        return this.apply {
            // index = "${this@Hierarchy.index}.${this@Hierarchy.cnt++}"
            documents.add(Doc(tmpl.createInstance().apply {
                workingPaperName = name
                init()
            }, n, index ?: "${this@Hierarchy.index}.${cnt++}"))
        }
    }

    fun document(name: String, tmpl: KClass<out WorkingPaper>, init: WorkingPaper.() -> Unit): Hierarchy {
        return document(null, name, tmpl, init)
    }

    fun hierarchy(index: String? = null, name: String, init: Hierarchy.() -> Unit): Hierarchy {
        return Hierarchy(name, index ?: "${this@Hierarchy.index}.${this@Hierarchy.cnt++}").apply {
            init()
            this@Hierarchy.subHierarchies.add(this)
        }
    }

    fun hierarchy(name: String, init: Hierarchy.() -> Unit): Hierarchy {
        return hierarchy(null, name, init)
    }


    fun generate(root: String = "tmp") {
        val rootFolder = "$root/$index $name"
        val p = Paths.get(rootFolder)

        if (!Files.exists(p))
            Files.createDirectory(p)

        documents.forEach {
            it.template.build("$rootFolder/${it.fileName}")
        }
        subHierarchies.forEach {
            it.generate(rootFolder)
        }
    }
}

fun hierarchy(name: String, init: Hierarchy.() -> Unit): Hierarchy {
    return Hierarchy(name).apply {
        init()
    }
}
