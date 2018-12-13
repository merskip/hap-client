package pl.merskip.homekitcollector.http.proxy

import org.apache.http.nio.reactor.IOSession
import org.apache.http.nio.reactor.SessionBufferStatus
import java.net.SocketAddress
import java.nio.channels.ByteChannel

abstract class IOSessionProxy(private val session: IOSession): IOSession {

    override fun getStatus(): Int = session.status

    override fun clearEvent(op: Int) = session.clearEvent(op)

    override fun hasBufferedOutput(): Boolean = session.hasBufferedOutput()

    override fun getLocalAddress(): SocketAddress = session.localAddress

    override fun removeAttribute(name: String?): Any = session.removeAttribute(name)

    override fun getSocketTimeout(): Int = session.socketTimeout

    override fun setSocketTimeout(timeout: Int) {
        session.socketTimeout = timeout
    }

    override fun setBufferStatus(status: SessionBufferStatus?) = session.setBufferStatus(status)

    override fun shutdown() = session.shutdown()

    override fun hasBufferedInput(): Boolean = session.hasBufferedInput()

    override fun setAttribute(name: String?, obj: Any?) = session.setAttribute(name, obj)

    override fun getAttribute(name: String?): Any = session.getAttribute(name)

    override fun getEventMask(): Int = session.eventMask

    override fun setEventMask(ops: Int) {
        session.eventMask = ops
    }

    override fun channel(): ByteChannel = session.channel()

    override fun setEvent(op: Int) = session.setEvent(op)

    override fun close() = session.close()

    override fun isClosed(): Boolean = session.isClosed

    override fun getRemoteAddress(): SocketAddress = session.remoteAddress
}