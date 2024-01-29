const test = require('node:test');
const {strictEqual, deepEqual} = require("assert");
const {
    getRandomIntInclusive,
    hyphenDate,
    toHyphenDate,
    randomDateInclusive,
    getJSONRandomHealthData
} = require("./api");

test('Random int is between their boundaries', () => {
    const random1 = getRandomIntInclusive(1, 1);
    strictEqual(random1, 1);
    const random0to1 = getRandomIntInclusive(0, 1);
    strictEqual(random0to1 === 0 || random0to1 === 1, true);
    const random0to100 = getRandomIntInclusive(0, 100);
    strictEqual(random0to100 >= 0 && random0to100 <= 100, true);
    const randomMinus100to100 = getRandomIntInclusive(-100, 100);
    strictEqual(randomMinus100to100 >= -100 && randomMinus100to100 <= 100, true);
})

test('Hyphen date returns a correct date', () => {
    const date1 = hyphenDate(2020, 1, 1);
    strictEqual(date1, '2020-01-01');
    const date2 = hyphenDate(2020, 12, 30);
    strictEqual(date2, '2020-12-30');
})

test('To hyphen date returns a correct data', () => {
    const dateAsString1 = '2020-01-01'
    const dateAsDate1 = new Date(dateAsString1);
    const parsedDate1 = toHyphenDate(dateAsDate1);
    strictEqual(parsedDate1, dateAsString1);

    const dateAsString2 = '2024-2-1'
    const dateAsDate2 = new Date(dateAsString2);
    const parsedDate2 = toHyphenDate(dateAsDate2);
    strictEqual(parsedDate2, '2024-02-01');
})

test('random date returns a correct data', () => {
    const from = new Date('2020-1-1');
    const till = new Date('2020-1-30');
    const random = randomDateInclusive(from, till);
    strictEqual(random !== null, true);
    strictEqual(random >= from && random <= till, true);
})

test('getJSONRandomHealthData returns the correct data', () => {
    const json = getJSONRandomHealthData();
    const arr = JSON.parse(json);
    strictEqual(arr.length, 7);
    const keys = Object.keys(arr[0]);
    const values = Object.values(arr[0]);
    strictEqual(keys.length, 4);
    strictEqual(values.length, 4);
    deepEqual(keys, ['date', 'avgSteps', 'avgBPM', 'avgSleepDuration']);
    strictEqual(typeof values[0], 'string');
    strictEqual(values[0].length, 10);
    strictEqual(typeof values[1], 'number');
    strictEqual(typeof values[2], 'number');
    strictEqual(typeof values[3], 'number');
})