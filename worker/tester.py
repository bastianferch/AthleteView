import sys
import pika
import json
import time
import signal

from dotenv import load_dotenv
import os
from Schedule import _loadScheduleFromJson

from pprint import *


def custom_sigterm_handler(signal_number, frame):
    print("Received SIGTERM. Exiting now...")
    sys.exit(0)


def custom_sigint_handler(signal_number, frame):
    print("Received SIGINT. Exiting now...")
    sys.exit(0)


def _readCredentials():
    global _rmqHost
    global _rmqPort
    global _rmqUser
    global _rmqPassword
    global _requestQueueName
    global _responseQueueName
    global _maxTimeOut

    load_dotenv()

    # rmqHost = "rabbit"
    _rmqHost = os.getenv("RMQ_HOST")
    _rmqPort = os.getenv("RMQ_PORT")
    _rmqUser = os.getenv("RMQ_USER")
    _rmqPassword = os.getenv("RMQ_PASSWORD")
    _requestQueueName = os.getenv("RMQ_REQUESTQUEUE")
    _responseQueueName = os.getenv("RMQ_RESPONSEQUEUE")
    _maxTimeOut = os.getenv("MAX_TIMEOUT")


def testcallback(ch, method, prop, body):
    data = json.loads(body.decode("utf-8"))
    #print("read" + json.dumps(data))
    print("sovled successfully: "+str(data["success"]))
    if data["success"]:
        print("response time: "+str(data["duration"]))
        print("activities:")
        pprint(data["activities"])
    else:
        print("Error: "+data["error"])


def _publish(routing_key, body):
    print("trying to publish: " + json.dumps(body))
    channel.basic_publish(
        exchange="",
        routing_key=routing_key,
        body=json.dumps(json.dumps(body)),
        properties=pika.BasicProperties(delivery_mode=pika.DeliveryMode.Persistent),
    )
    print("published to: " + routing_key)


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
    channel.queue_declare(queue=_responseQueueName)
    channel.queue_declare(queue=_requestQueueName)
    channel.basic_consume(
        queue=_responseQueueName, auto_ack=True, on_message_callback=testcallback
    )
    #_publish(_requestQueueName, {"requestId": 1234})
    loaded = ""
    with open("test/1.json", "r") as fp:
        loaded = json.load(fp)
    _publish(_requestQueueName,loaded)
    print("Setup done")
    print("Starting consumption \n")
    channel.start_consuming()


if __name__ == "__main__":
    print("Tester starting")

    signal.signal(signal.SIGTERM, custom_sigterm_handler)
    signal.signal(signal.SIGINT, custom_sigterm_handler)
    run()
