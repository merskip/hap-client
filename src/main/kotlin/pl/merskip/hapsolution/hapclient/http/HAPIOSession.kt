package pl.merskip.hapsolution.hapclient.http

import org.apache.http.nio.reactor.IOSession

class HAPIOSession(session: IOSession): IOSessionProxy(session) {

    private val originalChannel = super.channel()
    private var channel: HAPByteChannel? = null

        override fun channel() = channel ?: originalChannel

    fun upgrade(sessionKeys: SessionKeys) {
        channel = HAPByteChannel(sessionKeys, originalChannel)
    }
}