package pl.merskip.homekitcollector.http

import org.apache.http.HttpHost
import org.apache.http.nio.conn.SchemeIOSessionStrategy
import org.apache.http.nio.reactor.IOSession

class HAPIOSessionStrategy: SchemeIOSessionStrategy {

    override fun isLayeringRequired() = true

    override fun upgrade(host: HttpHost?, iosession: IOSession?) = HAPIOSession(iosession!!)
}