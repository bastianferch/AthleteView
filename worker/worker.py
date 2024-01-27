import sys
import pika
import json
import time
import signal
import logging

import traceback

from timeout_decorator import timeout

from dotenv import load_dotenv
import os

from Exceptions import *
from Scheduler import scheduleThresholdSearch
from Schedule import _loadScheduleFromJson


global r
global channel
global _maxTimeOut 
_maxTimeOut = 60

def custom_sigterm_handler(signal_number, frame):
    logger = logging.getLogger()
    logger.info("Received SIGTERM. Exiting now...")
    sys.exit(0)

def custom_sigint_handler(signal_number, frame):
    logger = logging.getLogger()
    logger.info("Received SIGINT. Exiting now...")
    sys.exit(0)

def _publish(routing_key, body):
    logger.info("trying to publish response")
    logger.debug("response: "+json.dumps(body))
    channel.basic_publish(
        exchange="",
        routing_key=routing_key,
        body=json.dumps(body),
        properties=pika.BasicProperties(delivery_mode=pika.DeliveryMode.Persistent),
    )
    logger.info("published to: " + _responseQueueName)

def _checkRequestFields(data):
    fields = ["trainerId","activities","requestTimestamp","schedule"]
    for field in fields:
        if field not in data:
            raise DataMalformedExcpetion(field+" missing from request")

def _checkActivitiyFields(activity):
    fields = ["duration","intensity","withTrainer","athlete","id"]
    for field in fields:
        if field not in activity:
            raise DataMalformedExcpetion(field+" missing from activity")

def _checkScheduleFields(schedule):
    pass

def _parseData(data):
    _checkRequestFields(data)
    activities = []
    for a in data["activities"]:
        _checkActivitiyFields(a)
        activities.append(
            Activity(a["id"],a["athlete"], a["duration"], a["intensity"], a["withTrainer"])
        )
    _checkScheduleFields(data["schedule"])
    schedule = _loadScheduleFromJson(data["schedule"])

    return (activities, schedule)




@timeout(_maxTimeOut, timeout_exception=TimerInterruptException)
def _solve(data):
    response = {"trainerId":data["trainerId"],
                  "requestTimestamp":data["requestTimestamp"],
                  "activities":data["activities"],
                  "success": False}
    try:
        (activities, schedule) = _parseData(data)
        
        start_time = time.time()
        (treshold,resp) = scheduleThresholdSearch(activities, schedule, 1)
        resp = resp[0]
        end_time = time.time()
        elapsed_time = (end_time - start_time) * 1000  # Convert to milliseconds

        response["success"] = True
        response["activities"] = resp.getActivitiesAsJson()
        response["threshold"] = treshold
        response["duration"] = int(elapsed_time)

        _publish(_responseQueueName, response)
    except TrainerConstraintExcpetion as e:
        logger.info("Caught TrainerConstraintExcpetion")
        response["error"] = e.message
        _publish(_responseQueueName, response)
    except IntensityConstraintException as e:
        logger.info("Caught IntensityConstraintException")
        response["error"] = e.message
        response["errorAthleteId"] = e.athlete
        _publish(_responseQueueName, response)
    except DataMalformedExcpetion as e:
        logger.info("Caught DataMalformedExcpetion")
        response["error"] = e.message
        _publish(_responseQueueName, response)
    except TimerInterruptException:
        logger.info("Caught TimerInterruptException")
        response["error"] = "Timeout occured (1 min)"
        _publish(_responseQueueName, response)
    except Exception as e:
        logger.info("Caught unhandeled exception in solve")
        _publish(_responseQueueName, {"trainerId":data["trainerId"],"requestTimestamp":data["requestTimestamp"],"activities":data["activities"],"success": False, "error": "unhandled"})


def callback(ch, method, prop, body):
    data = json.loads(json.loads(body.decode("utf-8")))
    logger.info("recieved request:")
    logger.debug(data)

    try:
        _solve(data)
    except Exception as e:
        traceback.print_exc() 
        logger.warning("Cought unhandeled exception in callback")

def _readCredentials():
    global _rmqHost
    global _rmqPort
    global _rmqUser
    global _rmqPassword
    global _requestQueueName
    global _responseQueueName
    global _maxTimeOut

    load_dotenv()

    _rmqHost = os.getenv("RMQ_HOST")
    _rmqPort = os.getenv("RMQ_PORT")
    _rmqUser = os.getenv("RMQ_USER")
    _rmqPassword = os.getenv("RMQ_PASSWORD")
    _requestQueueName = os.getenv("RMQ_REQUESTQUEUE")
    _responseQueueName = os.getenv("RMQ_RESPONSEQUEUE")
    _maxTimeOut = os.getenv("MAX_TIMEOUT")


def run():
    global r
    global channel

    _readCredentials()

    connection = pika.BlockingConnection(
        pika.ConnectionParameters(
            _rmqHost, credentials=pika.PlainCredentials(_rmqUser, _rmqPassword)
        )
    )
    channel = connection.channel()
    channel.queue_declare(queue=_requestQueueName)
    channel.queue_declare(queue=_responseQueueName)

    channel.basic_consume(
        queue=_requestQueueName, auto_ack=True, on_message_callback=callback
    )
    logger.info("Queue Setup done - starting consumption")
    channel.start_consuming()


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    global logger
    logger = logging.getLogger()
    logger.info("Worker starting")

    
    signal.signal(signal.SIGTERM, custom_sigterm_handler)
    signal.signal(signal.SIGINT, custom_sigterm_handler)
    run()
