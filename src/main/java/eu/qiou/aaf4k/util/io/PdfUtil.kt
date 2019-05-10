package eu.qiou.aaf4k.util.io

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.text.PDFTextStripperByArea
import java.awt.Rectangle
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels
import java.util.*


object PdfUtil {
    /**
     *  @return the list of mapping name of the region to the result string. Each element in the list represents a target page
     */
    fun extractText(document: PDDocument, pageFilter: (Int, PDPage) -> Boolean = { _, _ -> true },
                    regions: Map<String, Rectangle> = mapOf("a" to Rectangle(130, 50, 690, 750)),
                    stringProcessor: (String) -> String = { s -> s }): List<Map<String, String>> {
        val res : MutableList<Map<String, String>> = mutableListOf()
        val stripper = PDFTextStripperByArea()

        regions.forEach { name, rectangle ->
            stripper.addRegion(name, rectangle)
        }

        document.pages.filterIndexed(pageFilter).forEach {
            ele ->
                stripper.extractRegions(ele)
                val tmpMap: MutableMap<String, String> = mutableMapOf()
                regions.forEach{
                    tmpMap[it.key] = stringProcessor(stripper.getTextForRegion(it.key))
                }
                res.add(tmpMap)
        }

        return res
    }

    private fun readFile(file: File): PDDocument {
        return  PDDocument.load(file)
    }

    fun readFile(file: String):PDDocument{
        return  PDDocument.load(File(file))
    }

    fun readFile(file: URL): PDDocument {
        val fileName = "tmp/${UUID.randomUUID().toString().replace("-", "")}.pdf"
        val rbc = Channels.newChannel(file.openStream())
        val fos = FileOutputStream(fileName)
        fos.channel.transferFrom(rbc, 0, java.lang.Long.MAX_VALUE)

        return readFile(fileName)
    }

    fun mining(file: String, pageFilter: (Int, PDPage) -> Boolean = { _, _ -> true },
               regions: Map<String, Rectangle> = mapOf("a" to Rectangle(130, 50, 690, 750)),
               stringProcessor: (String) -> String = { s -> s }): List<Map<String, String>> {

        return extractText(readFile(file), pageFilter, regions, stringProcessor)
    }

    fun mining(file: URL, pageFilter: (Int, PDPage) -> Boolean = { _, _ -> true },
               regions: Map<String, Rectangle> = mapOf("a" to Rectangle(130, 50, 690, 750)),
               stringProcessor: (String) -> String = { s -> s }): List<Map<String, String>> {

        return extractText(readFile(file), pageFilter, regions, stringProcessor)
    }

    fun mining(file: File, pageFilter: (Int, PDPage) -> Boolean = { _, _ -> true },
               regions: Map<String, Rectangle> = mapOf("a" to Rectangle(130, 50, 690, 750)),
               stringProcessor: (String) -> String = { s -> s }): List<Map<String, String>> {

        return extractText(readFile(file), pageFilter, regions, stringProcessor)
    }

}