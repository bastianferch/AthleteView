const AUTH_TOKEN = '9579e55c-5522-4033-ae15-2c0aa4b2067e'

const http = require('http');
try {
    http.createServer(function (req, res) {
        if (req.headers.authorization !== AUTH_TOKEN){
            res.write(JSON.stringify({message: 'Not Authorized'}));
            res.end();
            return;
        }
        res.writeHead(200, {'Content-Type': 'application/json'});
        res.write(JSON.stringify(
            {
                'date': '2020-01-01',
                'avgSteps': 1,
                'avgBPM': 1,
                'avgSleepDuration': 1
            }));
        res.end();
    }).listen(8089);
} catch (e) {
    console.log(e);
}
