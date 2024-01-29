from Schedule import *
from Activity import Activity
from TimeTable import TimeTable

from typing import List


class ThresholdException(Exception):
    def __init__(self, message):
        super().__init__()
        self.message = message


class IntensityConstraintException(Exception):
    def __init__(self, message, athlete):
        super().__init__()
        self.message = message
        self.athlete = athlete


class TrainerConstraintExcpetion(Exception):
    def __init__(self, message, table: TimeTable, activities: List[Activity]):
        super().__init__()
        self.message = message
        self.table = table
        self.activities = activities

class TimerInterruptException(Exception):
    def __init__(self):
        super().__init__("Timeout")
        self.message = "Timeout"


class DataMalformedExcpetion(Exception):
    def __init__(self,message):
        super().__init__()
        self.message = message
