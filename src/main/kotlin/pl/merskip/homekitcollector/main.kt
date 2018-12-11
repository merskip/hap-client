package pl.merskip.homekitcollector

import com.google.gson.Gson
import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.HttpGet
import pl.merskip.homekitcollector.archive.Archiver
import pl.merskip.homekitcollector.http.Http
import pl.merskip.homekitcollector.pairing.PairSetup
import pl.merskip.homekitcollector.pairing.PairVerify
import pl.merskip.homekitcollector.response.AccessoriesResponse
import java.io.File


fun main(args: Array<String>) {

    val pairCredentials = Archiver.require(File("pair_credentials.json")) {
        PairSetup("192.168.1.8", 51826)
                .pair("031-45-154")
    }

    val sessionKeys = PairVerify(pairCredentials).verify()

    Http.updateSessionKeys(sessionKeys)

    val getHttp = HttpGet("http://192.168.1.8:${pairCredentials.port}/accessories")


    val response = Http.client.execute(getHttp)
    println(response)

    val responseContent = IOUtils.toByteArray(response.entity.content)

    println(String(responseContent))

    val accessoriesResponse = Gson().fromJson(String(responseContent), AccessoriesResponse::class.java)

    println(accessoriesResponse)
}
