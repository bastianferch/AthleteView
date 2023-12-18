const AUTH_TOKEN = '9579e55c-5522-4033-ae15-2c0aa4b2067e'
let requestCounter = 0;

// actual server
const http = require('http');
try {
    http.createServer(function (req, res) {
        if (req.headers.authorization !== AUTH_TOKEN) {
            res.write(JSON.stringify({message: 'Not Authorized'}));
            res.end();
            console.error('Not Authorized');
            return;
        }
        if (req.method !== 'GET') {
            res.write(JSON.stringify({message: 'Method is not supported'}));
            res.end();
            console.error('Method is not supported');
            return;
        }

        res.writeHead(200, {'Content-Type': 'application/json'});
        res.write(getJSONRandomHealthData());
        res.end();
        console.log('Successfully sent a random health data. Current counter: ' + ++requestCounter);
    }).listen(8089);
} catch (e) {
    console.log(e);
}

// end of actual server

/**
 * Main function, which returns the random health data to the user.
 * There is a date in the range between 2010-01-01 and today.
 * There is an average day steps between 3000 and 15000.
 * There is an average heart beats per minute between 60 and 120.
 * There is an average sleep duration (in minutes) between 4h and 10h.
 *
 * @returns {string} is the JSON with 4 fields. Example:
 * {
 *     "date": "2010-03-10",
 *     "avgSteps": 12059,
 *     "avgBPM": 85,
 *     "avgSleepDuration": 483
 * }
 */
function getJSONRandomHealthData() {
    return JSON.stringify(
        {
            'date': toHyphenDate(randomDateInclusive(new Date(2010, 0, 1), new Date())),
            'avgSteps': getRandomIntInclusive(3000, 15000),
            'avgBPM': getRandomIntInclusive(60, 120),
            'avgSleepDuration': getRandomIntInclusive(60 * 4, 60 * 10) // duration in minutes -> (4h, 10h)
        })
}

/**
 * Provides back the random int between the range of given params.
 *
 * @param min is the lowest possible random number (inclusive).
 * @param max is the highest possible random number (inclusive).
 * @returns {number} is a random number between the provided params.
 */
function getRandomIntInclusive(min, max) {
    min = Math.ceil(min);
    max = Math.floor(max);
    return Math.floor(Math.random() * (max - min + 1) + min);
}

/**
 * Provides back the random date between the range of given params.
 *
 * @param start is of type {Date}. Will be inclusive in the random range.
 * @param end is of type {Date}. Will be inclusive in the random range.
 * @returns {Date} is a random syntactic correct date between the provided params.
 */
function randomDateInclusive(start, end) {
    return new Date(start.getTime() + Math.random() * (end.getTime() - start.getTime()));
}

/**
 * Parses date from "Tue Jul 11 2023 19:15:20 GMT+0200 (Mitteleuropäische Sommerzeit)" to "2023-07-11". <br>
 *
 * @param date is of type "Date". Example: "Tue Jul 11 2023 19:15:20 GMT+0200 (Mitteleuropäische Sommerzeit)"
 * @return date like "xxxx-xx-xx"
 */
function toHyphenDate(date) {
    return hyphenDate(date.getFullYear(), date.getMonth() + 1, date.getDate());
}

/**
 * Provides a date with a type "2022-06-31"
 *
 * @param year is a number.
 * @param month is a number.
 * @param day is a number.
 * @return a date of type string. Example: "2020-01-01"
 */
function hyphenDate(year, month, day) {
    return `${year}-${Number(month) < 10 ? '0' + month : month}-${Number(day) < 10 ? '0' + day : day}`;
}

module.exports = {
    getJSONRandomHealthData, hyphenDate, toHyphenDate, getRandomIntInclusive, randomDateInclusive
}
