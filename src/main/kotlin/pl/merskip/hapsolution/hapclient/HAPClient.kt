package pl.merskip.hapsolution.hapclient

import com.google.gson.Gson
import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.HttpGet
import pl.merskip.hapsolution.hapclient.archive.Archiver
import pl.merskip.hapsolution.hapclient.http.HTTP
import pl.merskip.hapsolution.hapclient.model.Accessory
import pl.merskip.hapsolution.hapclient.pairing.PairCredentials
import pl.merskip.hapsolution.hapclient.pairing.PairSetup
import pl.merskip.hapsolution.hapclient.pairing.PairVerify
import pl.merskip.hapsolution.hapclient.response.AccessoriesResponse
import java.io.File
import java.util.concurrent.TimeUnit

class HAPClient(host: String, port: Int, pinCode: String) {

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
        val getHttp = HttpGet("http://${pairCredentials.host}:${pairCredentials.port}/accessories")
        val response = HTTP.client.execute(getHttp, null).get(60, TimeUnit.SECONDS)

        val responseContent = IOUtils.toByteArray(response.entity.content)
        val accessoriesResponse = Gson().fromJson(String(responseContent), AccessoriesResponse::class.java)
        return accessoriesResponse.accessories
    }
}