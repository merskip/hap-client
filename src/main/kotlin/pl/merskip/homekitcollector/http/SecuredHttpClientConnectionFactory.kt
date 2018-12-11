package pl.merskip.homekitcollector.http

import org.apache.http.config.ConnectionConfig
import org.apache.http.conn.HttpConnectionFactory
import org.apache.http.conn.ManagedHttpClientConnection
import org.apache.http.conn.routing.HttpRoute

class SecuredHttpClientConnectionFactory: HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> {

    companion object {
        var counter = 0
    }

    override fun create(route: HttpRoute?, config: ConnectionConfig?): ManagedHttpClientConnection {
        val id = "http-outgoing-$counter"
        counter++

        val connection = SecuredHttpClientConnection(id, config?.bufferSize ?: 1024)
        Http.lastHttpClientConnection = connection
        return connection
    }

}