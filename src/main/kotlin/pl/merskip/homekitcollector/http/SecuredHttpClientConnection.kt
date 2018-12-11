package pl.merskip.homekitcollector.http

import org.apache.http.impl.conn.DefaultManagedHttpClientConnection
import pl.merskip.homekitcollector.pairing.SessionKeys
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

class SecuredHttpClientConnection(id: String, bufferSize: Int): DefaultManagedHttpClientConnection(id, bufferSize) {

    private var inputStream: SecuredInputStream? = null
    private var outputStream: SecuredOutputStream? = null

    override fun getSocketInputStream(socket: Socket?): InputStream {
        val inputStream = SecuredInputStream(super.getSocketInputStream(socket))
        this.inputStream = inputStream
        return inputStream
    }

    override fun getSocketOutputStream(socket: Socket?): OutputStream {
        val outputStream = SecuredOutputStream(super.getSocketOutputStream(socket))
        this.outputStream = outputStream
        return outputStream
    }

    fun setSessionKeys(sessionKeys: SessionKeys) {
        inputStream?.decryptionKey = sessionKeys.accessoryToControllerKey
        outputStream?.encryptKey = sessionKeys.controllerToAccessoryKey
    }

}