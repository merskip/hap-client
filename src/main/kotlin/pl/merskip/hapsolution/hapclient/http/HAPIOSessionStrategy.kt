package pl.merskip.hapsolution.hapclient.http

import org.apache.http.HttpHost
import org.apache.http.nio.conn.SchemeIOSessionStrategy
import org.apache.http.nio.reactor.IOSession

class HAPIOSessionStrategy: SchemeIOSessionStrategy {

    override fun isLayeringRequired() = true

    override fun upgrade(host: HttpHost?, iosession: IOSession?): IOSession? {
        val session = HAPIOSession(iosession!!)
        val sessionKeys = HTTP.sessionKeys
        if (sessionKeys != null) {
            session.upgrade(sessionKeys)
        }
        return session
    }
}