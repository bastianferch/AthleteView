from Schedule import *
from Activity import Activity
from TimeTable import TimeTable

from typing import List


class ThresholdException(Exception):
    def __init__(self, message):
        super().__init__(message)


class IntensityConstraintException(Exception):
    def __init__(self, message, activities: List[Activity]):
        super().__init__(message)
        self.activities = activities


class TrainerConstraintExcpetion(Exception):
    def __init__(self, message, table: TimeTable, activities: List[Activity]):
        super().__init__(message)
        self.table = table
        self.activities = activities

class TimerInterruptException(Exception):
    def __init__(self):
        super().__init__("Timeout")


class DataMalformedExcpetion(Exception):
    def __init__(self,msg):
        super().__init__(msg)
