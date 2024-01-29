package ase.athlete_view.domain.activity.util

import ase.athlete_view.common.exception.fitimport.InvalidFitFileException
import com.garmin.fit.Decode
import com.garmin.fit.FitDecoder
import com.garmin.fit.FitMessages
import com.garmin.fit.FitRuntimeException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

@Component
class FitParser {
    private val log = KotlinLogging.logger {}
    private var parser: FitDecoder = FitDecoder()

    fun decode(filename: InputStream): FitMessages {
        log.trace { "Util | decode()" }
        val byteData = filename.readAllBytes()
        val decoder = Decode()
        val isFitFile = decoder.isFileFit(ByteArrayInputStream(byteData))

        try {
            if (isFitFile) {
                return parser.decode(ByteArrayInputStream(byteData))
            }
        } catch (e: FitRuntimeException) {
            log.error { "Something went wrong processing the file" }
            log.error { e }
        } catch (e: IOException) {
            log.error { "IOException during processing!" }
            log.error { e }
        } catch (e: Exception) {
            log.error { "Exception during processing!" }
            log.error { e }
        }

        log.error { "Noticed invalid fit-upload, skipping!" }
        throw InvalidFitFileException("File $filename is not a valid .fit-File!")
    }
}