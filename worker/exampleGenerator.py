from math import e
from TimeTable import *
from Schedule import *
import random
import json


def generateRandomTimetable(userId):
    days = []
    for i in range(0, 7):
        days.append(generateRandomWeekday())
    return TimeTable(days, userId)


def generateRandomWeekday():
    slots = []
    for h in range(0, DAY_LENGTH):  # hour of the day
        slots.append(Slot(random.randint(0, 1) == 1))
    return WeekDay(slots)


def generateRandomActivities(athlete, cnt=10, maxDuration=6):
    ret = []
    for i in range(0, cnt):
        rnd = random.randint(0, 13)
        ret.append(
            Activity(rnd,athlete, 1 + (rnd % (maxDuration)), (rnd % 3), (rnd % 2 == 0))
        )
    return ret


def generateRandomSchedule(athleteCnt=3):
    athleteTables = {}
    for athleteId in range(0, athleteCnt):
        athleteTables[athleteId] = generateRandomTimetable(athleteId)
    return Schedule(generateRandomTimetable(1337), athleteTables)


def generateRandomExample(athleteCnt=3, activitiesPerAthlete=10, maxDuration=6):
    activities = []
    for athleteId in range(0, athleteCnt):
        activities = activities + generateRandomActivities(
            athleteId, activitiesPerAthlete, maxDuration
        )

    schedule = generateRandomSchedule(athleteCnt)
    return (activities, schedule)


def loadTimeTableFromArry(array, userId):
    days = []
    for day in array:
        slots = []
        for slot in day:
            slots.append(Slot(slot))
        days.append(WeekDay(slots))
    return TimeTable(days, userId)


def loadScheduleFromJson(json):
    athleteTables = dict()
    for athleteId in json["athleteTables"]:
        # print(athleteId)
        athleteTables[int(athleteId)] = loadTimeTableFromArry(
            json["athleteTables"][athleteId], athleteId
        )
    return Schedule(
        loadTimeTableFromArry(json["trainerTable"], json["trainerId"]), athleteTables, []
    )


def generateExamples(cnt):
    for i in range(0, cnt):
        rnd = random.randint(0, 1337)
        athleteCnt = (rnd % 5) + 1
        activityCnt = (rnd % 5) + 3
        maxDuration = (rnd % 6) + 1

        (activities, schedule) = generateRandomExample(
            athleteCnt, activityCnt, maxDuration
        )
        dump = {}
        activityList = []
        for a in activities:
            activityList.append(a.toJson())
        dump["activities"] = activityList
        dump["schedule"] = schedule.toJson()
        with open("examples/" + str(i) + ".json", "w") as fp:
            json.dump(dump, fp)


def loadExample(number):
    with open("examples/" + str(number) + ".json", "r") as fp:
        loaded = json.load(fp)
        activities = []
        for a in loaded["activities"]:
            activities.append(
                Activity(1337,a["athlete"], a["duration"], a["intensity"], a["withTrainer"])
            )
        schedule = loadScheduleFromJson(loaded["schedule"])

        return (activities, schedule)


def addActivityIds(number):
    with open("examples/" + str(number) + ".json", "r") as fp:
        loaded = json.load(fp)
        activities = []
        
        for a in loaded["activities"]:
            rnd = random.randint(0, 1337)
            a["id"] = rnd

    with open("examples/" + str(number) + ".json", "w") as fp:
        json.dump(loaded,fp)

def main():

    # this expects /examples to exist so you might have to create that directory first
    generateExamples(1000)
    for n in range(0,1000):
        addActivityIds(n)
    


if __name__ == "__main__":
    main()
