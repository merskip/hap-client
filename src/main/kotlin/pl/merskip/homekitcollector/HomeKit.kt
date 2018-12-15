package pl.merskip.homekitcollector

import com.google.gson.Gson
import org.apache.commons.io.IOUtils
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.concurrent.FutureCallback
import pl.merskip.homekitcollector.archive.Archiver
import pl.merskip.homekitcollector.http.HTTP
import pl.merskip.homekitcollector.model.Accessory
import pl.merskip.homekitcollector.pairing.PairCredentials
import pl.merskip.homekitcollector.pairing.PairSetup
import pl.merskip.homekitcollector.pairing.PairVerify
import pl.merskip.homekitcollector.response.AccessoriesResponse
import java.io.File
import java.util.concurrent.TimeUnit

class HomeKit(host: String, port: Int, pinCode: String) {

    private val pairCredentials: PairCredentials

    init {
        HTTP.client.start()

        pairCredentials = Archiver.require(File("pair_credentials.json")) {
            PairSetup(host, port).pair(pinCode)
        }

        val sessionKeys = PairVerify(pairCredentials).verify()
        HTTP.updateSessionKeys(sessionKeys)
    }

    fun getAccessories(): List<Accessory> {
        val getHttp = HttpGet("http://192.168.1.8:${pairCredentials.port}/accessories")
        val response = HTTP.client.execute(getHttp, object: FutureCallback<HttpResponse> {
            override fun cancelled() {
                println("asda")
            }

            override fun completed(result: HttpResponse?) {
                println(result)
            }

            override fun failed(ex: Exception?) {
                println(ex)
            }

        }).get(60, TimeUnit.SECONDS)

        val responseContent = IOUtils.toByteArray(response.entity.content)
        val accessoriesResponse = Gson().fromJson(String(responseContent), AccessoriesResponse::class.java)
        return accessoriesResponse.accessories
    }
}