package ase.athlete_view.domain.activity.util

import com.garmin.fit.FitDecoder
import com.garmin.fit.FitMessages
import org.springframework.stereotype.Component
import java.io.BufferedInputStream

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream

@Component
class FitParser {
    private var parser: FitDecoder = FitDecoder()

    fun decode(filename: InputStream): FitMessages {
        try {
            val inputStream = BufferedInputStream(filename)
            return parser.decode(inputStream)
        } catch (fne: FileNotFoundException) {
            // TODO: log
        }

        return FitMessages()
    }
}