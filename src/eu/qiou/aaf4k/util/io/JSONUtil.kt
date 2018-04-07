package eu.qiou.aaf4k.util.io

import com.google.api.client.http.GenericUrl
import com.google.api.client.http.javanet.NetHttpTransport
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.FileReader
import java.time.LocalDate
import java.util.Calendar



object JSONUtil {

    private val cache:MutableMap<String, JSONObject> = mutableMapOf()

    fun processDataSource(source:String, isRawString : Boolean = false, callback: (JSONObject)->Unit, useCache: Boolean = true){

        var obj: JSONObject? = null

        if(useCache && cache.containsKey(source)){

            obj = cache.get(source)

        } else{

            val parser = JSONParser()

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

            cache.put(source, obj)
        }

        callback(obj!!)
    }

    fun <T>fetch(source:String, isRawString : Boolean = false, queryString: String, queryStringSeparator: String = ".", useCache: Boolean = true):T {
        var res:Any? = null
        val f: (JSONObject)->Unit = {
            obj ->
                res = query(obj,queryString, queryStringSeparator)
        }

        processDataSource(source, isRawString, f, useCache)

        return (res as T)
    }

    fun <T> query(obj:JSONObject, queryString: String, queryStringSeparator: String = ".") : T {
        val tmp = queryString.split(queryStringSeparator)
        var initObj:Any = obj

        tmp.forEachIndexed({ i, s ->
            initObj = when{
                initObj is JSONArray -> (initObj as JSONArray).get(s.toInt())!!
                initObj is JSONObject -> (initObj as JSONObject).get(s)!!
                else-> throw Exception("Type Error")
            }
        })

        return (initObj as T)
    }

    fun readFromCache(source:String):JSONObject {
        if(cache.containsKey(source)){
            return cache.get(source)!!
        }

        throw Exception("Target source not cached!")
    }

    fun millisecondsToDate(milliseconds:Long):LocalDate {


        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliseconds

        val mYear = calendar.get(Calendar.YEAR)
        val mMonth = calendar.get(Calendar.MONTH) + 1
        val mDay = calendar.get(Calendar.DAY_OF_MONTH)

        return LocalDate.of(mYear, mMonth, mDay)

    }
}

fun <T>JSONObject.query(queryString: String, queryStringSeparator: String = ".") = JSONUtil.query<T>(this, queryString, queryStringSeparator)
fun Long.toDate() = JSONUtil.millisecondsToDate(this)