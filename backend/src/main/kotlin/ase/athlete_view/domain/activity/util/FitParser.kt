package ase.athlete_view.domain.activity.util

import ase.athlete_view.common.exception.fitimport.InvalidFitFileException
import com.garmin.fit.Decode
import com.garmin.fit.FitDecoder
import com.garmin.fit.FitMessages
import com.garmin.fit.FitRuntimeException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream

@Component
class FitParser {
    private val logger = KotlinLogging.logger {}
    private var parser: FitDecoder = FitDecoder()

    fun decode(filename: InputStream): FitMessages {
        val bufferedInputStream = BufferedInputStream(filename)
        bufferedInputStream.mark(0)
        val decoder = Decode()
        val isFitFile = decoder.isFileFit(bufferedInputStream)
        bufferedInputStream.reset()

        try {
            if (isFitFile) {
                return parser.decode(bufferedInputStream)
            }
        } catch (e: FitRuntimeException) {
            logger.error { "Something went wrong processing the file" }
            logger.error { e }
            throw InvalidFitFileException("File $filename is not a valid .fit-File!")
        } catch (e: IOException) {
            logger.error { "IOException during processing!" }
            logger.error { e }
        } catch (e: Exception) {
            logger.error { "Exception during processing!" }
            logger.error { e }
        }

        logger.error { "Noticed invalid fit-upload, skipping!" }
        throw InvalidFitFileException("File is not a valid .fit-File!")
    }
}