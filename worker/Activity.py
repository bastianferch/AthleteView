class Activity:
    def __init__(self, id,athlete, duration, intensity, withTrainer, scheduledDay=None,scheduledStartTime=None):
        self.id = id
        self.athlete = athlete
        self.duration = duration
        self.intensity = intensity
        self.withTrainer = withTrainer
        self.scheduledDay = scheduledDay
        self.scheduledStartTime = scheduledStartTime

    def getDuration(self):
        return self.duration

    def getAthlete(self):
        return self.athlete

    def print(self):
        print(self.toString())

    def setTime(self, start, day):
        self.scheduledDay = day
        self.scheduledStartTime = start

    def hash(self):
        return hash(self.toString())

    def toString(self):
        return "(dur:{}, load:{}, athlete:{}, withTrainer: {})".format(
            self.duration, self.intensity, self.athlete, self.withTrainer
        )

    def toJson(self):
        ret = {
            "id":self.id,
            "duration": self.duration,
            "intensity": self.intensity,
            "withTrainer": self.withTrainer,
            "athlete": self.athlete,
        }
        if self.scheduledDay != None:
            ret["scheduledDay"] = self.scheduledDay
            ret["scheduledStartTime"]  = self.scheduledStartTime
        return ret
    
    def deepcopy(self):
        return Activity(self.id,self.athlete,self.duration,self.intensity,self.withTrainer,self.scheduledDay,self.scheduledStartTime)
