package pl.merskip.homekitcollector.pairing

import java.util.*

data class PairCredentials(
        val host: String,
        val port: Int,
        val pairingID: UUID,
        val longTermSecretKeyBytes: ByteArray,
        val longTermPublicKeyBytes: ByteArray,
        val accessoryLongTermPublicKeyBytes: ByteArray
)