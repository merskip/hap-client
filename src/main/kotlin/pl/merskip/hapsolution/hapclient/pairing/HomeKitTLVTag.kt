package pl.merskip.hapsolution.hapclient.pairing

/**
 * Table 4-6. TLV Values
 */
enum class HomeKitTLVTag(val tag: Int) {

    /**
     * Method to use for pairing.
     */
    Method(0x00),

    /**
     * Identifier for authentication.
     */
    Identifier(0x1),

    /**
     * 16+ bytes of random salt.
     */
    Salt(0x02),

    /**
     * Curve25519, SRP public key, or signed Ed25519 key.
     */
    PublicKey(0x03),

    /**
     * Ed25519 or SRP proof.
     */
    Proof(0x04),

    /**
     * Encrypted data with auth tag at end.
     */
    EncryptedData(0x05),

    /**
     * State of the pairing process. 1=M1, 2=M2, etc.
     */
    Sequence(0x06),

    /**
     * Error code. Must only be present if error code is not 0.
     */
    Error(0x7),

    /**
     * Seconds to delay until retrying a setup code.
     */
    RetryDelay(0x08),

    /**
     * X.509 Certificate.
     */
    Certificate(0x09),

    /**
     * Ed25519
     */
    Signature(0x0A),

    /**
     * Bit value describing permissions of the controller being added.
     * when is false - Regular user
     *      is true - Admin that is able to add and remove pairings against the accessory.
     */
    Permissions(0x0B),

    /**
     * Non-last fragment of data. If length is 0, it's an ACK.
     */
    FragmentData(0x0C),

    /**
     * Last fragment of data.
     */
    FragmentLast(0x0D),

    /**
     * Zero-length TLV that separates different TLVs in a list.
     */
    Separator(0xFF);

    /**
     * Table 4-4. Methods
     */
    enum class Methods(val value: Int) {
        PairSetup(1),
        PairVerify(2),
        AddPairing(3),
        RemovePairing(4),
        ListPairing(5)
    }

    /**
     * Table 4-5. Error Codes
     */
    enum class ErrorCodes(val value: Int) {

        /**
         * Generic error to handle unexpected errors.
         */
        Unknown(1),

        /**
         * Setup code or signature verification failed.
         */
        Authentication(2),

        /**
         * Client must look at the retry delay TLV item and wait that many seconds before retrying.
         */
        Backoff(3),

        /**
         * Server cannot accept any more pairings.
         */
        MaxPeers(4),

        /**
         * Server reached its maximum number of authentication attempts.
         */
        MaxTries(5),

        /**
         * Server pairing method is unavailable.
         */
        Unavailable(6),

        /**
         * Server is busy and cannot accept a pairing request at this time.
         */
        Busy(7)
    }
}
