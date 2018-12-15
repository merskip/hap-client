package pl.merskip.hapsolution.hapclient.pairing

data class SessionKeys(
    val accessoryToControllerKey: ByteArray,
    val controllerToAccessoryKey: ByteArray
) {

    companion object {

        fun createFromSharedKey(sharedKey: ByteArray) = SessionKeys(
                HKDFSHA512.encrypt(sharedKey, "Control-Salt", "Control-Read-Encryption-Key"),
                HKDFSHA512.encrypt(sharedKey, "Control-Salt", "Control-Write-Encryption-Key")
        )
    }
}