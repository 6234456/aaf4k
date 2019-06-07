package eu.qiou.aaf4k.plugins

import org.openqa.selenium.firefox.FirefoxDriver

class CNLegalDocuments {
    init {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            System.setProperty("webdriver.gecko.driver", "C:\\Users\\qiou\\Documents\\geckodriver.exe")
            System.setProperty("webdriver.firefox.bin", "C:\\Program Files\\Mozilla Firefox\\firefox.exe")
        } else {
            System.setProperty("webdriver.gecko.driver", "/home/yang/Documents/geckodriver")
            System.setProperty("webdriver.firefox.bin", "/usr/bin/firefox")
        }
    }

    private fun truncatCoreURL(url: String): String {
        val pos = url.indexOf("&KeyWord=")
        return if (pos < 0) url else url.take(pos)
    }

    // location 天津市
    // category 民事
    // courtLevel 基层 中级 高级 最高
    // tribunalLevel  一审 二审
    fun search(keyword: String = "", location: String? = null, category: String? = null, courtLevel: String? = null
               , tribunalLevel: String? = null) {
        val webDriver = FirefoxDriver()

        val url = "http://wenshu.court.gov.cn/list/list/?sorttype=1&conditions=searchWord+QWJS+++全文检索:$keyword${
        if (location != null)
            "&conditions=searchWord+$location+++法院地域:$location"
        else
            ""
        }${
        if (category != null)
            "&conditions=searchWord+${category}案由+++一级案由:${category}案由"
        else
            ""
        }${
        if (courtLevel != null)
            "&conditions=searchWord+${courtLevel}法院+++法院层级:${courtLevel}法院"
        else
            ""
        }${
        if (tribunalLevel != null)
            "&conditions=searchWord+$tribunalLevel+++审判程序:$tribunalLevel"
        else
            ""
        }"

        webDriver.get(url)

        // process the current page
        fun f() {
            val v = webDriver.findElementsByCssSelector(".wstitle a")

            if (v.isEmpty()) {
                val t = webDriver.findElementById("resultList").text

                if (t.contains("繁忙")) {
                    webDriver.close()
                    search(keyword, location, category, courtLevel, tribunalLevel)
                } else if (t.contains("无符合")) {
                    println("not found")
                } else {
                    Thread.sleep(1000)
                    f()
                }
            } else {
                v.forEach {
                    it.click()
                }

                while (webDriver.windowHandles.count() < v.count()) {
                    Thread.sleep(2000)
                }

                webDriver.windowHandles.forEachIndexed { index, s ->
                    if (index > 0) {
                        webDriver.switchTo().window(s)

                        while (!webDriver.currentUrl.contains("DocID=")) {
                            Thread.sleep(800)
                        }

                        println(truncatCoreURL(webDriver.currentUrl))
                        // contentTitle
                        println(try {
                            webDriver.findElementById("contentTitle").text
                        } catch (
                                e: Exception
                        ) {
                            ""
                        })

                        webDriver.close()
                    }
                }
                //   webDriver.close()
            }
        }
        f()
    }

}