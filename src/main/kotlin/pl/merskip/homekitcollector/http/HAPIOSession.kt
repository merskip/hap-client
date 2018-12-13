package pl.merskip.homekitcollector.http

import org.apache.http.nio.reactor.IOSession
import pl.merskip.homekitcollector.http.proxy.IOSessionProxy
import pl.merskip.homekitcollector.pairing.SessionKeys

class HAPIOSession(session: IOSession): IOSessionProxy(session) {

    private val originalChannel = super.channel()
    private var channel: HAPByteChannel? = null

    override fun channel() = channel ?: originalChannel

    fun upgrade(sessionKeys: SessionKeys) {
        channel = HAPByteChannel(sessionKeys, originalChannel)
    }
}