package pl.merskip.hapsolution.hapclient.http.proxy

import java.nio.ByteBuffer
import java.nio.channels.ByteChannel

abstract class ByteChannelProxy(private val channel: ByteChannel): ByteChannel {

    override fun isOpen() = channel.isOpen

    override fun write(src: ByteBuffer?) = channel.write(src)

    override fun close() = channel.close()

    override fun read(dst: ByteBuffer?) = channel.read(dst)
}