import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.lang.Exception

data class HEDWebsericeResponse(
    val service: String = "",
    val command: String = "",
    val command_target: String = "",
    val results: HashMap<String,Any> = HashMap(),
    val error_type: String = "",
    val error_msg: String = ""
)
class HEDWebService {
        fun validateString(hedString: String, schema: String = "8.4.0"): HEDWebsericeResponse {
            val server = "https://hedtools.org/hed/"
            val (csrfToken, cookies) = getSessionInfo(server + "services")
            val gson = GsonBuilder().setPrettyPrinting().create()
            val requestBody = gson.toJson(
                mapOf(
                    "service" to "strings_validate",
                    "schema_version" to schema,
                    "string_list" to listOf(hedString),
                    "check_warnings_validate" to "on"
                )
            )

            val (_, response, result) = Fuel.post(server + "services_submit")
                .header(Pair("X-CSRFToken", csrfToken), Pair("Accept", "application/json"), Pair("Cookie", cookies))
                .jsonBody(requestBody)
                .responseString()
            when (result) {
                is Result.Failure -> {
                    val error = result.error
                    val statusCode = response.statusCode
                    val errorMsg = "HTTP $statusCode: ${error.message ?: "Unknown error"}"
                    throw Exception("Error connecting to HED validator: $errorMsg")
                }
                is Result.Success -> {
                    val data = result.get()
                    val responseGson = Gson()
                    val hedResponse = responseGson.fromJson(data, HEDWebsericeResponse::class.java)
                    // error_type/error_msg are for SERVER errors (like Python exceptions)
                    // Validation errors appear in results["msg_category"] and results["msg"]
                    if (!hedResponse.error_type.isNullOrEmpty()) {
                        throw Exception("HED Service Error: ${hedResponse.error_type} - ${hedResponse.error_msg}")
                    }
                    return hedResponse
                }
            }
        }

        fun validateSidecar(hedSidecarString: String, schema: String = "8.3.0"): HEDWebsericeResponse {
            val server = "https://hedtools.org/hed/"
            val (csrfToken, cookies) = getSessionInfo(server + "services")
            val gson = GsonBuilder().setPrettyPrinting().create()
            val requestBody = gson.toJson(
                mapOf(
                    "service" to "sidecar_validate",
                    "schema_version" to schema,
                    "sidecar_string" to hedSidecarString,
                    "check_for_warnings" to true
                )
            )

            val (_, response, result) = Fuel.post(server + "services_submit")
                .header(Pair("X-CSRFToken", csrfToken), Pair("Accept", "application/json"), Pair("Cookie", cookies))
                .jsonBody(requestBody)
                .responseString()
            when (result) {
                is Result.Failure -> {
                    val error = result.error
                    val statusCode = response.statusCode
                    val errorMsg = "HTTP $statusCode: ${error.message ?: "Unknown error"}"
                    throw Exception("Error connecting to HED validator: $errorMsg")
                }
                is Result.Success -> {
                    val data = result.get()
                    val responseGson = Gson()
                    val hedResponse = responseGson.fromJson(data, HEDWebsericeResponse::class.java)
                    // error_type/error_msg are for SERVER errors (like Python exceptions)
                    // Validation errors appear in results["msg_category"] and results["msg"]
                    if (!hedResponse.error_type.isNullOrEmpty()) {
                        throw Exception("HED Service Error: ${hedResponse.error_type} - ${hedResponse.error_msg}")
                    }
                    return hedResponse
                }
            }
        }
        fun getSessionInfo(csrfURL: String): Pair<String, String> {
            val (_, response, result) = csrfURL
                .httpGet()
                .responseString()

            when (result) {
                is Result.Failure -> {
                    val error = result.error
                    val statusCode = response.statusCode
                    val errorMsg = "HTTP $statusCode: ${error.message ?: "Unknown error"}"
                    throw Exception("Error getting session info from HED validator: $errorMsg")
                }
                is Result.Success -> {
                    val data = result.get()
                    val doc: Document = Jsoup.parse(data)
                    val input = doc.getElementsByTag("input")
                    val csrfToken = input.attr("value")
                    if (csrfToken.isEmpty()) {
                        throw Exception("Failed to retrieve CSRF token from HED validator")
                    }
                    val cookies = response.headers["Set-Cookie"].toString().trim('[').trim(']')
                    return Pair(csrfToken, cookies)
                }
            }
        }
}
