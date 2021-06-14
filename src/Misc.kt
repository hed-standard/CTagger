import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.lang.Exception
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder


fun getHEDSessionInfo(hostURL:String):Pair<String,String> {
    val csrf_url = "$hostURL/services"
    val url = URL(csrf_url)
    val con: HttpURLConnection = url.openConnection() as HttpURLConnection
    val cookiesHeader = con.getHeaderField("Set-Cookie")
    val stream = BufferedReader(
            InputStreamReader(con.inputStream))
    var inputLine: String?
    val content = StringBuffer()
    while (stream.readLine().also { inputLine = it } != null) {
        content.append(inputLine)
    }
    val csrfIdx = content.indexOf("csrf_token")
    val tmp = content.substring(csrfIdx + "csrf_token".length + 1, content.length)
    val regex = Regex("\".*?\"")
    val csrftoken = regex.find(tmp)
    var csrf = ""
    if (csrftoken != null) {
        csrf = csrftoken.value.trim('"')
    }
    stream.close()
    return Pair<String, String>(cookiesHeader, csrf)
}

fun toJson(map: Any): String {
    val gson = GsonBuilder().setPrettyPrinting().create()
    return gson.toJson(map)
}

fun sendRequestToHEDServer(host:String,data:String): HEDResponse? {
    val services_url = "$host/services_submit?service=get_services"
    val cookies_csrf = getHEDSessionInfo(host)
    val url = URL(services_url)
    val con: HttpURLConnection = url.openConnection() as HttpURLConnection
    try {
        // set headers
        con.requestMethod = "POST"
        con.setRequestProperty("Content-Type", "application/json")
        con.setRequestProperty("Accept", "application/json")
        con.setRequestProperty("X-CSRFToken", cookies_csrf.second)
        con.setRequestProperty("Cookie", cookies_csrf.first)
        con.setConnectTimeout(60)
        con.setReadTimeout(60)
        // https://www.baeldung.com/httpurlconnection-post
        // allow to write content to output stream
        con.doOutput = true
        // write data to output stream
        con.outputStream.use { os ->
            val input: ByteArray = data.toByteArray() // charset utf-8 used by default
            os.write(input, 0, input.size)
        }

        // read response
        val stream = BufferedReader(
                InputStreamReader(con.inputStream))
        var inputLine: String?
        val content = StringBuffer()
        while (stream.readLine().also { inputLine = it } != null) {
            content.append(inputLine)
        }
        stream.close()
        val gson = Gson()
        return gson.fromJson(content.toString(),HEDResponse::class.java)
    }
    catch (e: MalformedURLException) {
        return null
    }
    catch (e: IOException) {
        return null
    }
    catch (e: Exception) {
        return null
    }
}
fun getStringServiceData(service:String="string_validate", schema_version:String="8.0.0-alpha.2", string_list: List<String>, check_for_warnings:Boolean=true): String{
    val data = HashMap<String,String>()
    data["service"] = service
    data["schema_version"] = schema_version
    data["string_list"] = toJson(string_list)
    data["check_for_warnings"] = check_for_warnings.toString()

    return toJson(data)
}
fun main() {
    val host = "https://hedtools.ucsd.edu/hed"
    val result = sendRequestToHEDServer(host, getStringServiceData(string_list = listOf("Red,Blue")))
    print(result)
}
class HEDResponse {
    var service = ""
    var results = HashMap<String,String>()
    var error_type = ""
    var error_msg = ""
}

