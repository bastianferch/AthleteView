import Activity
import copy


# Can be free or not
# if not free may hold an assigned Activity
class Slot:
    def __init__(self, free: bool):
        self.free = free
        self.activity = None

    def _padString(self, str: str, goto):
        while len(str) < goto:
            str = str + " "
        return str

    def toString(self) -> str:
        ret = ""
        if self.free:
            ret = "f"
        if self.activity != None:
            return (
                "("
                + str(self.activity.intensity)
                + ","
                + ("+" if self.activity.withTrainer else "-")
                + ")"
            )
        return self._padString(ret, 5)

    def hash(self):
        return hash(self.toString())

    # sets activity to the slot
    def setActivity(self, activity: Activity):
        if activity != None:
            self.activity = activity

    # check if slot was planned to be free and has not yet
    # gotten an activity assigned
    def isFree(self):
        return self.free and self.activity == None

    # since self.free is never set after init
    # can easily checked if this slot was free in the beginning
    # (required for score)
    def wasFree(self):
        return self.free

    def hasActicity(self):
        return self.activity != None

    def getActivity(self):
        return self.activity

    def __deepcopy__(self, memo):
        new_instance = self.__class__(copy.deepcopy(self.free, memo))
        x = self.activity
        if x:
            new_instance.setActivity(x)
        return new_instance
