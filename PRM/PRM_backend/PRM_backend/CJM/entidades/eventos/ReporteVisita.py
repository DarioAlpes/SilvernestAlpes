from CJM.services.validations import VALID_CATEGORIES


class ReporteVisita:
    UNKNOWN_CATEGORY_STRING = ""
    INITIAL_TIME_NAME = "initial-time"
    FINAL_TIME_NAME = "final-time"

    def __init__(self):
        self.visits_per_category = dict()
        self.visits_per_category[ReporteVisita.UNKNOWN_CATEGORY_STRING] = 0
        for category in VALID_CATEGORIES:
            self.visits_per_category[category] = 0

    def add_visit(self, person):
        if person.categoria in self.visits_per_category:
            self.visits_per_category[person.categoria] += 1
        else:
            self.visits_per_category[ReporteVisita.UNKNOWN_CATEGORY_STRING] += 1

    def to_dict(self):
        return self.visits_per_category
