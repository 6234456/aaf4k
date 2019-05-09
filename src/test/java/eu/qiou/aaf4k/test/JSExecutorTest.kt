package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.io.JSExecutor
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths

class JSExecutorTest {

    @Test
    operator fun invoke() {
        val s = Files.readAllLines(Paths.get("script/url"))[0]
        val y = JSExecutor("script/ywtu.js").invoke("getc", s).toString()
        val n = "3g9Q4omynblEFe4Ckg3UFOHchMFtBYB81wPrU47c2lXG6qfe.ufCCSh8lsiFZ3fhCEFi8kwIUGcvebbMpsN0LrRw80MBL7L3LmAdeyY9ruEkwPtkdffIEB8USBNQ_BflI32UMjFRnnxyxn5zdfD20tpEnJqqmoNLPnsaEvZx.N.UPtyzwAyzAASUe3SxIYJg9l4W"
        val a = JSExecutor("script/encrypt.js").invoke("getCookies", s, n, y).toString()

        println(s)
        println(y)
        println(n)
        println(a)

        /*
        .nlhMQ4fLE5kDpdRPaAhcvkdj7Y851Qqw_arKQt.ZNE
3g9Q4omynblEFe4Ckg3UFOHchMFtBYB81wPrU47c2lXG6qfe.ufCCSh8lsiFZ3fhCEFi8kwIUGcvebbMpsN0LrRw80MBL7L3LmAdeyY9ruEkwPtkdffIEB8USBNQ_BflI32UMjFRnnxyxn5zdfD20tpEnJqqmoNLPnsaEvZx.N.UPtyzwAyzAASUe3SxIYJg9l4W
3glVyo2Sab9q3eM5rggI3OBtxM_czYHKAwcxY4WtvliLTqrTNur50S.KDsX153rk0E_DQkeUYGPBdbDsssxZjryYQyLmr9HZxjwj3OoQmDsCYcSNRqeHwV70LoCiM9u_ssgnSUwpC60kCBxTND4JuZPo5tLv9QG_32s6GZKN5K1Jyf1A5Bo7pMZwJAtg1N4rghT3BXMRkZ8XComqrpF1Xf333qplNP7.kK.3VtfX4BrvNUf2pSrnbCeI3d2s6rlOxrZ49VSJjxJBfOKTHUGA4527Ndu9qyHY9FOPjlIAJCnsGYa


3JxoznLP9sNsWEKzYJTWWKPRwX6pyGcmlLHFrirRuSk4fTWbeDWzGlbm1bOrjzWAGe6YECalrYBZ.shqoblB500DEDV7xClKJPylI5gzW_2W3ExDwjJbj22KNd89kKtUZEb0S2KqG32pHlRFY2jIMAsWm3Dz4nouHNnhhCGUAYQckVWkbjV33P3O8TBrtS3y7EUT0I2LEqloCSKZAGCO9bUpEfwCEEVD1tppf6Rl9De.eclNqzwFA7APrJzdME11Do0xKFecU_2D_99I0748B.EPqXNtDOcxRGTTWGsScV_6pAq



        */

    }
}