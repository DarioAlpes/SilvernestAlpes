
class Measure(object):

    MEASURE_NAME = "measure"

    def __init__(self, measure):
        self.measure = measure

    def to_dict(self):
        fields_dict = dict()
        fields_dict[self.MEASURE_NAME] = self.measure
        return fields_dict
