const express = require('express');
const fs = require('fs');
const path = require('path');
const app = express();

const AUTH_TOKEN = '9579e55c-5522-4033-ae15-2c0aa4b2067e';
const port = 8089;
let requestCounter = 0;

// actual server
app.get('/', (req, res) => {
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
    const type = req.query.importType;
    if (!type) {
        console.error('Method is not supported');
        return;
    }

    if (type === 'health') {
        res.writeHead(200, {'Content-Type': 'application/json'});
        res.write(getJSONRandomHealthData());
        console.log('Successfully sent a random health data. Current counter: ' + ++requestCounter);
    } else if (type === 'activity') {
        const filePath = path.join(__dirname, 'mock.fit');

        res.setHeader('Content-Type', 'application/octet-stream');
        res.setHeader('Content-Disposition', 'attachment; filename=mock.fit');

        const fileStream = fs.createReadStream(filePath);
        fileStream.pipe(res);
        console.log('Successfully sent a random activity data. Current counter: ' + ++requestCounter);
    } else {
        res.writeHead(400, {'Content-Type': 'application/json'});
        res.write(JSON.stringify({message: 'Method is not supported'}));
        console.log('Could not parse the type.');
        res.end();
    }
})

app.listen(port, () => {
    console.log(`Example app listening on port ${port}`)
})

// end of actual server

/**
 * Main function, which returns the random health data to the user.
 * There is a date in the range between 2010-01-01 and today.
 * There is an average day steps between 3000 and 15000.
 * There is an average heart beats per minute between 60 and 120.
 * There is an average sleep duration (in minutes) between 4h and 10h.
 *
 * @returns {string} is the JSON with 7 objects (last 7 days), each with 4 fields. Example:
 * {
 *     "date": "2010-03-10",
 *     "avgSteps": 12059,
 *     "avgBPM": 85,
 *     "avgSleepDuration": 483
 * }
 */
function getJSONRandomHealthData() {
    const healthList = [];
    const currentDate = new Date();
    // iterate through the last 7 days.
    for (let i = 0; i < 7; i++) {
        let pastDate = new Date(currentDate);
        pastDate.setDate(currentDate.getDate() - i);
        healthList.push(
            {
                'date': toHyphenDate(pastDate),
                'avgSteps': getRandomIntInclusive(3000, 15000),
                'avgBPM': getRandomIntInclusive(60, 120),
                'avgSleepDuration': getRandomIntInclusive(60 * 4, 60 * 10) // duration in minutes -> (4h, 10h)
            }
        )
    }
    return JSON.stringify(healthList);
}

function getJSONRandomFitnessData() {
    const previousWeekDays = getPreviousWeekDaysAt10();
    const previousWeekFitnessData = []

    for (let i = 0; i < previousWeekDays.length; i++) {
        previousWeekFitnessData.push(getFitnessDataWithStartTime(previousWeekDays[i]))
    }
    return JSON.stringify(previousWeekFitnessData);
}

function getPreviousWeekDaysAt10() {
    const startOfPreviousWeek = new Date();

    // Calculate the start of the previous week
    startOfPreviousWeek.setDate(startOfPreviousWeek.getDate() - (startOfPreviousWeek.getDay() + 6) % 7);
    startOfPreviousWeek.setHours(10, 0, 0, 0);

    const days = [];

    // Generate an array of each day in the previous week at 10:00
    for (let i = 0; i < 7; i++) {
        const currentDay = new Date(startOfPreviousWeek);
        currentDay.setDate(startOfPreviousWeek.getDate() + i);
        days.push(currentDay);
    }

    return days;
}

function getFitnessDataWithStartTime(startTime) {
    const endTime = new Date(startTime);
    endTime.setHours(18)
    return {
        accuracy: Math.random(),
        averageBpm: getRandomIntInclusive(60, 200),
        minBpm: getRandomIntInclusive(60, 200),
        maxBpm: getRandomIntInclusive(60, 200),
        distance: getRandomIntInclusive(1000, 20000),
        spentKcal: getRandomIntInclusive(60, 1000),
        cadence: Math.random(),
        avgPower: Math.random(),
        maxPower: Math.random(),
        fitData: null,
        startTime,
        endTime,
        laps: [
            getRandomLap(1),
            getRandomLap(2),
            getRandomLap(3),
            getRandomLap(4),
            getRandomLap(5),
        ],
        comments: []
    }
}

function getRandomLap(lapNum) {
    return {
        lapNum,
        time: getRandomIntInclusive(60, 300),
        distance: getRandomIntInclusive(60, 300),
        avgSpeed: getRandomIntInclusive(1, 3),
        avgPower: getRandomIntInclusive(1, 10),
        maxPower: getRandomIntInclusive(1, 10),
        avgBpm: getRandomIntInclusive(60, 180),
        maxBpm: 180,
        avgCadence: getRandomIntInclusive(60, 120),
        maxCadence: 120,
    }
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
