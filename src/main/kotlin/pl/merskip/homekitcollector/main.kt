package pl.merskip.homekitcollector

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import pl.merskip.homekitcollector.pairing.PairCredentials
import pl.merskip.homekitcollector.pairing.PairSetup
import pl.merskip.homekitcollector.pairing.PairVerify
import java.io.File


fun main(args : Array<String>) {

    val credentials: PairCredentials

    val pairCredentialsFile = File("pair_credentials.json")
    if (pairCredentialsFile.exists()) {
        println("Read credentials from file: ${pairCredentialsFile.absolutePath}")
        credentials = Gson().fromJson(String(pairCredentialsFile.readBytes()), PairCredentials::class.java)
    }
    else {

        credentials = PairSetup("192.168.1.8", 51826)
                .pair("031-45-154")

        val credentialsJson = GsonBuilder().setPrettyPrinting().create().toJson(credentials)
        val writer = pairCredentialsFile.printWriter()
        writer.write(credentialsJson)
        writer.close()

        println("Saved credentials to file: ${pairCredentialsFile.absolutePath}")
    }

    println(credentials)

    println("Pair verification...")
    PairVerify(credentials).verify()
    println("Done :-)")

}
