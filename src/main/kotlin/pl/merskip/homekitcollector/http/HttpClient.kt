package pl.merskip.homekitcollector.http

import org.apache.http.HttpResponse
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.config.RegistryBuilder
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient
import org.apache.http.impl.nio.client.HttpAsyncClients
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor
import org.apache.http.nio.conn.ManagedNHttpClientConnection
import org.apache.http.nio.conn.SchemeIOSessionStrategy
import org.apache.http.protocol.HttpContext
import pl.merskip.homekitcollector.pairing.SessionKeys


object HTTP {

    private var hapIOSession: HAPIOSession? = null

    val client: CloseableHttpAsyncClient = {

        val ioReactor = DefaultConnectingIOReactor()
        val schemeRegistry= RegistryBuilder.create<SchemeIOSessionStrategy>()
                .register("http", HAPIOSessionStrategy())
                .build()
        val connectionManager = PoolingNHttpClientConnectionManager(ioReactor, schemeRegistry)

        HttpAsyncClients
                .custom()
                .addInterceptorLast { _: HttpResponse?, context: HttpContext? ->
                    val httpClientContext = HttpClientContext.adapt(context)
                    val connection = httpClientContext.connection as ManagedNHttpClientConnection
                    hapIOSession = connection.ioSession as HAPIOSession
                }
                .setConnectionManager(connectionManager)
                .build()
    }()

    fun updateSessionKeys(sessionKeys: SessionKeys) {
        hapIOSession?.upgrade(sessionKeys)
    }
}