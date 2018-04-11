package eu.qiou.aaf4k.util.io

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.text.PDFTextStripperByArea
import java.awt.Rectangle
import java.io.File

object PdfUtil {
    /**
     *  @return the list of mapping name of the region to the result string. Each element in the list represents a target page
     */
    fun extractText(document: PDDocument, pageFilter:(Int, PDPage) -> Boolean = {_, _  -> true}, regions: Map<String, Rectangle>, stringProcessor: (String) -> String = { s -> s} ):List<Map<String, String>>{
        val res : MutableList<Map<String, String>> = mutableListOf()
        val stripper = PDFTextStripperByArea()

        regions.forEach({name, rectangle ->
            stripper.addRegion(name, rectangle)
        })

        document.pages.filterIndexed(pageFilter).forEach {
            ele ->
                stripper.extractRegions(ele)
                val tmpMap: MutableMap<String, String> = mutableMapOf()
                regions.forEach{
                        tmpMap.put(it.key, stringProcessor(stripper.getTextForRegion(it.key)))
                }
                res.add(tmpMap)
        }

        return res
    }

    fun readFile(file: File):PDDocument{
        return  PDDocument.load(file)
    }

    fun readFile(file: String):PDDocument{
        return  PDDocument.load(File(file))
    }

}