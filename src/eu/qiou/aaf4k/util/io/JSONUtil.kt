package eu.qiou.aaf4k.util.io

import com.google.api.client.http.GenericUrl
import com.google.api.client.http.javanet.NetHttpTransport
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.FileReader

object JSONUtil {
    fun processDataSource(source:String, isRawString : Boolean = false, callback: (JSONObject)->Unit){
        val parser = JSONParser()
        var obj: JSONObject? = null

        if(isRawString){
            obj = parser.parse(source) as JSONObject
        }
        else if(source.startsWith("http", true)){
            val requestFactory =  NetHttpTransport().createRequestFactory()
            val request = requestFactory.buildGetRequest(GenericUrl(source))
            obj =  parser.parse(request.execute().parseAsString()) as JSONObject
        } else {
            obj = parser.parse(FileReader(source)) as JSONObject
        }
        callback(obj!!)
    }

    fun <T>fetchOne(source:String, isRawString : Boolean = false, queryString: String, queryStringSeparator: String = "."):T {
        var res:Any? = null
        val f: (JSONObject)->Unit = {
            obj ->
            val tmp = queryString.split(queryStringSeparator)
            val lastIndex = tmp.count() - 1
            res = tmp.take(lastIndex).fold(obj){
                 a, b -> (a.get(b)!! as JSONObject)
            }.get(tmp.get(lastIndex))
        }
        processDataSource(source, isRawString, f)

        return (res as T)
    }
}