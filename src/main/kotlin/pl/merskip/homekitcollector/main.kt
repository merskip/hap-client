package pl.merskip.homekitcollector

import pl.merskip.homekitcollector.archive.Archiver
import pl.merskip.homekitcollector.pairing.PairSetup
import pl.merskip.homekitcollector.pairing.PairVerify
import java.io.File


fun main(args: Array<String>) {

    val pairCredentials = Archiver.require(File("pair_credentials.json")) {
        PairSetup("192.168.1.8", 51826)
                .pair("031-45-154")
    }

    PairVerify(pairCredentials).verify()

}
