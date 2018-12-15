package pl.merskip.hapsolution.hapclient.pairing

import java.util.*

data class PairCredentials(
        val host: String,
        val port: Int,
        val pairingID: UUID,
        val longTermSecretKeyBytes: ByteArray,
        val longTermPublicKeyBytes: ByteArray,
        val accessoryLongTermPublicKeyBytes: ByteArray
)