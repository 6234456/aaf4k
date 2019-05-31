package eu.qiou.aaf4k.plugins

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.Serializable
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

data class EntityInfo(val SECCode: String, val orgCode: String, val SECName: String, val industry1: String, val industry2: String,
                      val orgName: String = "", val orgNameEN: String = "", val location: String = "", val url: String = "",
                      val email: String = "", val boardSecretary: String = "", val emailBoardSecretary: String = "",
                      val registeredCapital: Double = 0.0, val securityDelegator: String = "", val auditor: String = "", val sz: Boolean = true,
                      val fs: String = "", val category: String = "A股"
) : Serializable {
    override fun hashCode(): Int {
        return SECCode.toInt().hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is EntityInfo && other.SECCode == this.SECCode
    }

    fun downloadFS(path: String? = null): Deferred<EntityInfo> {
        return GlobalScope.async {
            if (fs.isNotBlank())
                Files.copy(URL(fs).openStream(), Paths.get(path
                        ?: "data/tmp/${SECCode}_${SECName}.pdf"), StandardCopyOption.REPLACE_EXISTING)

            this@EntityInfo
        }
    }
}