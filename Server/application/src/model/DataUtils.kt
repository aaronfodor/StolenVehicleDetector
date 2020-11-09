package com.arpadfodor.stolenvehicledetector.model

import java.io.File
import java.io.FileInputStream
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

object DataUtils{

    fun getFileContent(filePath: String) : String{
        var content = ""

        var inputStream: FileInputStream? = null
        var sc: Scanner? = null

        try {

            inputStream = FileInputStream(filePath)
            sc = Scanner(inputStream, "UTF-8")

            while (sc.hasNextLine()) {
                val line = sc.nextLine() + "\n"
                content += line
            }

            if (sc.ioException() != null) {
                throw sc.ioException()
            }

        }
        catch (e: Exception){

        }
        finally {
            inputStream?.close()
            sc?.close()
        }

        return content
    }

    fun writeFileContent(filePath: String, content: String){
        val file = File(filePath)
        // if the file does not exist, create it
        file.createNewFile()
        file.writeText(content)
    }

    fun currentTimeUTC() : String {
        return DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now())
    }

}