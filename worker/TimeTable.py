from Activity import Activity
from Slot import Slot
from WeekDay import WeekDay
from Constants import *

from typing import List

import copy


# Consists of an array of WeekDay
class TimeTable:
    def __init__(self, weekDays: List[WeekDay], userId):
        self.weekDays = weekDays
        self.userId = userId

    # calculates all possible start times for each day for a given activity
    # returns list of all possible start times as a list ([[]])
    def calcPossibleStarts(self, activity: Activity):
        starts = []
        for i in range(0, len(self.weekDays)):
            tmp = self.weekDays[i].calcPossibleStarts(activity)
            if len(tmp) > 0:
                starts.append((i, tmp))
        return starts

    # calcs all possible starts of an activity for the WeekDay
    # this is used when assigning activities without trainer
    # returns list with all possible start times (also allowing activities on slots that are not free)
    def fuzzyCalcPossibleStarts(self, activity: Activity):
        starts = []
        for i in range(0, len(self.weekDays)):
            tmp = self.weekDays[i].fuzzyCalcPossibleStarts(activity)
            if len(tmp) > 0:
                starts.append((i, tmp))
        return starts

    # assigns an activity to a day (and effectivly to all required slots within that day)
    def assignActivity(self, activity: Activity, day, startSlot):
        self.weekDays[day].assignActivity(activity, startSlot)

    # returns the class flattened to an array (used for persisting as json)
    def toArray(self):
        weekSlots = []
        for day in self.weekDays:
            daySlots = []
            for slot in day.slots:
                if slot.free:
                    daySlots.append(True)
                else:
                    daySlots.append(False)
            weekSlots.append(daySlots)
        return weekSlots

    def print(self):
        print("+---------+--------------------------------------------------+")
        print("|Time/Day |  Mo     Di     Mi     Do     Fr     Sa     So    |")
        print("+---------+--------------------------------------------------+")
        hourLine = "|"
        for hour in range(0, DAY_LENGTH):
            hourLine = "|"
            if hour < 10:
                hourLine += "  0" + str(hour) + ":00  "
            else:
                hourLine += "  " + str(hour) + ":00  "
            hourLine += "| "
            for day in self.weekDays:
                hourLine += day.getSlot(hour).toString() + "  "
            hourLine += "| "
            print(hourLine)
        print("+---------+--------------------------------------------------+")
        print()

    def log(self,logger):
        logger.debug("+---------+--------------------------------------------------+")
        logger.debug("|Time/Day |  Mo     Di     Mi     Do     Fr     Sa     So    |")
        logger.debug("+---------+--------------------------------------------------+")
        hourLine = "|"
        for hour in range(0, DAY_LENGTH):
            hourLine = "|"
            if hour < 10:
                hourLine += "  0" + str(hour) + ":00  "
            else:
                hourLine += "  " + str(hour) + ":00  "
            hourLine += "| "
            for day in self.weekDays:
                hourLine += day.getSlot(hour).toString() + "  "
            hourLine += "| "
            print(hourLine)
        logger.debug("+---------+--------------------------------------------------+")
        logger.debug()

    def getDay(self, dayNumber) -> WeekDay:
        return self.weekDays[dayNumber]

    def myDeepCopy(self):
        newDays = []
        for day in self.weekDays:
            newSlots = []
            for slot in day.slots:
                newSlot = Slot(slot.free)
                if slot.activity:
                    newSlot.setActivity(slot.activity)
                newSlots.append(newSlot)
            newDays.append(WeekDay(newSlots))
        return TimeTable(newDays, self.userId)

    def hash(self):
        weekDayHashes = []
        for weekDay in self.weekDays:
            weekDayHashes.append(weekDay.hash())
        return hash((self.userId, str(weekDayHashes)))

    def __deepcopy__(self, memo):
        new_instance = self.__class__(copy.deepcopy(self.weekDays, memo), self.userId)
        return new_instance


def _loadTimeTableFromArry(array, userId):
    days = []
    for day in array:
        slots = []
        for slot in day:
            slots.append(Slot(slot))
        days.append(WeekDay(slots))
    return TimeTable(days, userId)
