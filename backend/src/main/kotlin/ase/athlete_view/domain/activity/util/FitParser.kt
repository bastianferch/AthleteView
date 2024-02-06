/*-
 * #%L
 * athlete_view
 * %%
 * Copyright (C) 2023 - 2024 TU Wien INSO ASE GROUP 5 WS2023
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
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
