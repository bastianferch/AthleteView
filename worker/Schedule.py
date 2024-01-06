from Activity import Activity
from Slot import Slot
from WeekDay import WeekDay
from TimeTable import TimeTable, _loadTimeTableFromArry
from Exceptions import *
from Constants import *

from typing import List

import copy


# Consists of a trainerTable and an array of AthleteTables (athleteId:TimeTable) and a list of activities
class Schedule:
    def __init__(self, trainerTable: TimeTable, athleteTables: dict[int, TimeTable], activities:List[Activity]):
        self.trainerTable = trainerTable
        self.athleteTables = athleteTables
        self.assigned_activities = activities

    def toJson(self):
        aTables = {}
        for k in self.athleteTables.keys():
            tmp = self.athleteTables.get(k)
            if tmp:
                aTables[k] = tmp.toArray()
        activities = []
        for a in self.assigned_activities:
            activities.append(a.toJson())
        return {
            "trainerTable": self.trainerTable.toArray(),
            "athleteTables": aTables,
            "trainerId": self.trainerTable.userId,
            "activities":activities
        }
    
    # returns the activities list in json format
    def getActivitiesAsJson(self):
        activities = []
        for a in self.assigned_activities:
            activities.append(a.toJson())
        return activities

    # returns the table of a given athlete (if available)
    def getAthleteTable(self, userId) -> TimeTable:
        ret = self.athleteTables.get(userId)
        if ret != None:
            return ret
        else:
            raise Exception("Athlete table not available when trying to get")

    # assigns activity to trainer and athlete
    def assignActivity(self, activity: Activity, day, startSlot):
        userId = activity.getAthlete()
        athleteTable = self.athleteTables.get(userId)

        tmp = activity.deepcopy()
        tmp.setTime(startSlot,day)
        self.assigned_activities.append(tmp) #add the assigned activity to the list of assigned activities

        if athleteTable != None:
            athleteTable.assignActivity(activity, day, startSlot)
        else:
            raise Exception("Athlete table not available when trying to  assign")

        if activity.withTrainer:
            self.trainerTable.assignActivity(activity, day, startSlot)

    def getAtheleteTablesAsList(self):
        return self.athleteTables.values()

    def print(self):
        print("_______________________________________________________________")
        self.trainerTable.print()
        print(" ")
        for key in self.athleteTables.keys():
            print("table for {}:".format(key))
            tmp = self.athleteTables.get(key)
            if tmp:
                tmp.print()
        print("_______________________________________________________________")
    
    def log(self,logger):
        logger.debug("_______________________________________________________________")
        self.trainerTable.log(logger)
        logger.debug(" ")
        for key in self.athleteTables.keys():
            logger.debug("table for {}:".format(key))
            tmp = self.athleteTables.get(key)
            if tmp:
                tmp.log(logger)
        logger.debug("_______________________________________________________________")

    def hash(self):
        athleteTableHashes = []
        for athleteTable in self.athleteTables.keys():
            tmp = self.athleteTables.get(athleteTable)
            if tmp:
                athleteTableHashes.append(tmp.hash())
        return hash((self.trainerTable.hash(), str(athleteTableHashes)))

    def myDeepcopy(self):
        newDict = dict()
        for key in self.athleteTables.keys():
            newDict[key] = self.athleteTables[key].myDeepCopy()
        activities = []
        for a in self.assigned_activities:
            activities.append(a.deepcopy())
        return Schedule(self.trainerTable.myDeepCopy(), newDict, activities)


def _loadScheduleFromJson(json):
    athleteTables = dict()
    for athleteId in json["athleteTables"]:
        # print(athleteId)
        athleteTables[int(athleteId)] = _loadTimeTableFromArry(
            json["athleteTables"][athleteId], athleteId
        )
    return Schedule(
        _loadTimeTableFromArry(json["trainerTable"], json["trainerId"]), athleteTables, []
    )
