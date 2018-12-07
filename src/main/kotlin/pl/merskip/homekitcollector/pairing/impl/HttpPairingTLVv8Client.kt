package pl.merskip.homekitcollector.pairing.impl

import org.apache.commons.io.IOUtils
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.HttpClientBuilder
import pl.merskip.homekitcollector.loggerFor
import pl.merskip.homekitcollector.pairing.PairingClient
import pl.merskip.homekitcollector.pairing.UnexpectedHTTPStatus
import pl.merskip.homekitcollector.tlv.TLVData
import pl.merskip.homekitcollector.tlv.TLVEncoder
import pl.merskip.homekitcollector.tlv.TLVReader
import java.net.URL

class HttpPairingTLVv8Client(private val url: URL): PairingClient {

    private val logger = loggerFor(javaClass)

    private val httpClient = HttpClientBuilder.create().build()

    override fun request(body: List<TLVData>): TLVReader {

        val httpPost = HttpPost(url.toURI())

        httpPost.addHeader("Content-Type", "application/pairing+tlv8")
        httpPost.entity = ByteArrayEntity(TLVEncoder().encode(body))

        logger.debug("Request HTTP POST to ${httpPost.uri}...")
        val httpResponse = httpClient.execute(httpPost)

        val status = httpResponse.statusLine.statusCode
        logger.debug("Response status code: $status")
        if (status != HttpStatus.SC_OK)
            throw UnexpectedHTTPStatus(status, httpResponse.statusLine.reasonPhrase)

        val responseContent = IOUtils.toByteArray(httpResponse.entity.content)
        return TLVReader(responseContent)
    }
}