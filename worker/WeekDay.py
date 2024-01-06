from Activity import Activity
from Slot import Slot
from Constants import *

from typing import List

import copy


# Consists of a an array of slots for each timeslot of the day
class WeekDay:
    # init with an array of DAY_LENGTH Slots
    def __init__(self, slots: List[Slot]):
        if len(slots) != DAY_LENGTH:
            raise Exception(
                "Invalid amount of activities supplied when trying to create WeekDay!"
            )
        self.free = True
        for slot in slots:
            if slot.hasActicity():
                self.free = False
        self.slots = slots

    # calcs all possible starts of an activity for the WeekDay
    # returns list with all possible start times
    def calcPossibleStarts(self, activity: Activity):
        starts = []
        windows = self._findWindows()
        for start, length in windows:
            if length >= activity.duration:
                for i in range(
                    start, start + length - activity.duration + 1
                ):  # TODO: this needs special attention when switching to 15min intervals
                    starts.append(i)
        return starts

    # calcs all possible starts of an activity for the WeekDay
    # this is used when assigning activities without trainer
    # returns list with all possible start times (also allowing activities on slots that are not free)
    def fuzzyCalcPossibleStarts(self, activity: Activity):
        starts = []
        for slotNumber in range(0, len(self.slots)):
            if not self.getSlot(slotNumber).hasActicity():
                fits = True
                if slotNumber + activity.duration > len(self.slots):
                    fits = False
                else:
                    for i in range(slotNumber, slotNumber + activity.duration):
                        if self.getSlot(i).hasActicity():
                            fits = False
                if fits:
                    starts.append(slotNumber)
        return starts

    # checks intensity of this day and returns true if the given activity can follow this day up (intensity constraint)
    def canBeFollowedUpBy(self, activity: Activity):
        for slot in self.slots:
            if slot.activity != None:
                intensity = slot.activity.intensity
                if intensity + activity.intensity >= 3:
                    return False
        return True

    # calulates all "free" windows within the day
    # returns it as a list of tuple [(day,[timeslots])]
    def _findWindows(self):
        start = -1
        length = 0
        ret = []
        for i in range(0, len(self.slots)):
            if self.slots[i].isFree():
                if start == -1:
                    start = i
                    length = 1
                else:
                    length += 1
            if not self.slots[i].isFree():
                if start != -1:
                    ret.append((start, length))
                    start = -1
                    length = 0
        if start != -1:
            ret.append((start, length))
        return ret

    # assigns an activity to the weekday by setting it to the slots that are taken
    def assignActivity(self, activity: Activity, startSlot):
        duration = activity.getDuration()
        self.free = False
        for t in range(startSlot, startSlot + duration):
            self.slots[t].setActivity(activity)

    def print(self):
        hourLine = ""
        for hour in range(0, len(self.slots)):
            if hour < 10:
                hourLine = "  0" + str(hour) + ":00  "
            else:
                hourLine = "  " + str(hour) + ":00  "
            if self.slots[hour]:
                hourLine += self.slots[hour].toString() + "  "
            print(hourLine)

    def isFree(self):
        return self.free

    def getSlot(self, hour):
        return self.slots[hour]

    def hash(self):
        slots = []
        for slot in self.slots:
            slots.append(slot.hash())
        return hash(str(slots))

    def __deepcopy__(self, memo):
        new_instance = self.__class__(copy.deepcopy(self.slots, memo))
        return new_instance
