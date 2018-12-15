package pl.merskip.hapsolution.hapclient

import com.google.gson.Gson
import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.HttpGet
import java.io.File
import java.util.concurrent.TimeUnit


fun main(args: Array<String>) {

    HTTP.client.start()

    val pairCredentials = Archiver.require(File("pair_credentials.json")) {
        PairSetup("192.168.1.8", 51826)
                .pair("031-45-154")
    }

    val sessionKeys = PairVerify(pairCredentials).verify()

    HTTP.updateSessionKeys(sessionKeys)

    val getHttp = HttpGet("http://192.168.1.8:${pairCredentials.port}/accessories")


    val response = HTTP.client.execute(getHttp, null).get(60, TimeUnit.SECONDS)
    println(response)

    val responseContent = IOUtils.toByteArray(response.entity.content)

    println(String(responseContent))

    val accessoriesResponse = Gson().fromJson(String(responseContent), AccessoriesResponse::class.java)

    println(accessoriesResponse)
}
