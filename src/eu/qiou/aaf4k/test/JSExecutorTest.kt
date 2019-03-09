package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.io.JSExecutor
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths

class JSExecutorTest {

    @Test
    operator fun invoke() {
        val s = Files.readAllLines(Paths.get("script/url"))[0]
        println(s)
        val y = JSExecutor("script/ywtu.js").invoke("getc", s).toString()
        val a = JSExecutor("script/encrypt.js").invoke("getCookies", s, "3Un8INNpK5ugs2GCigj0gAkYxezkJCeRRacmLsLST2vmjoc.oIr4plOYjZ2wxWOKX5oNFuasmDtIIF0DaI2kl1J0hDBeHtd2We5IVlc6X1frPUnF7i9SsTonax2CANDouWNchRsFK1p_46ezPXINqyUSQJZ8blSdpZ8_nG2YTE6kcRBoDg6bWswJduuaNlpnUFUBymZJo5cx2e_OHcDmxK_KFYSh61zduWuGUkUI8UZr2aGdrO8w3cHiLA_7ws2_MVvZDg3slcat1EpMZVAuNBLloDwfTO_P856suxUkNpJMk671lv4XYKriXwfyG0o5oBkX9tJAUItwK4k.xoKYCp9ae4hZLPUTBjPbJo2ZFNnm_FG", y).toString()

        println(a)

    }
}