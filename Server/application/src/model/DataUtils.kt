package com.arpadfodor.ktor.model

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
        finally {
            inputStream?.close()
            sc?.close()
        }

        return content

    }

    fun currentTimeUTC() : String {
        return DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now())
    }

}