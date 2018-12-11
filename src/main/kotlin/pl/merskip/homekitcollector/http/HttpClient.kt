package pl.merskip.homekitcollector.http

import org.apache.http.client.HttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import pl.merskip.homekitcollector.pairing.SessionKeys

object Http {

    var lastHttpClientConnection: SecuredHttpClientConnection? = null

    val client: HttpClient = {

        val clientConnectionFactory = SecuredHttpClientConnectionFactory()
        val connectionManager = PoolingHttpClientConnectionManager(clientConnectionFactory)

        HttpClients.custom()
                .setConnectionManager(connectionManager)
                .disableAutomaticRetries()
                .disableContentCompression()
                .build()
    }()

    fun updateSessionKeys(sessionKeys: SessionKeys) {
        lastHttpClientConnection?.setSessionKeys(sessionKeys)
    }
}