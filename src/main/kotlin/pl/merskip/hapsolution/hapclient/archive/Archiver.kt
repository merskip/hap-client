package pl.merskip.hapsolution.hapclient.archive

import com.google.gson.GsonBuilder
import java.io.File

object Archiver {

    val gson = GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ByteArray::class.java, GsonByteArrayTypeAdapter())
            .create()

    inline fun <reified T> require(file: File, builder: () -> T): T {
        var obj = load<T>(file)
        return if (obj != null) obj
        else {
            obj = builder()
            save(file, obj)
            obj
        }
    }

    fun <T> save(file: File, data: T) {
        val json = gson.toJson(data)
        val writer = file.printWriter()
        writer.write(json)
        writer.close()
    }

    inline fun <reified T> load(file: File): T? {
        return try {
            gson.fromJson(file.reader(), T::class.java)
        } catch (e: Exception) {
            null
        }
    }
}