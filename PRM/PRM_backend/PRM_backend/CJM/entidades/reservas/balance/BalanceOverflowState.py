class BalanceOverflowState:
    OK_STATE = "ok"
    OVERFLOWN_STATE = "overflown"
    TEMPORALLY_OVERFLOWN_STATE = "temporally-overflown"

    def __init__(self):
        self.state = self.OK_STATE

    def mark_as_overflown(self):
        if self.state == self.OK_STATE:
            self.state = self.OVERFLOWN_STATE
        elif self.state == self.OVERFLOWN_STATE:
            self.state = self.OVERFLOWN_STATE
        elif self.state == self.TEMPORALLY_OVERFLOWN_STATE:
            self.state = self.OVERFLOWN_STATE

    def mark_as_ok(self):
        if self.state == self.OK_STATE:
            self.state = self.OK_STATE
        elif self.state == self.OVERFLOWN_STATE:
            self.state = self.TEMPORALLY_OVERFLOWN_STATE
        elif self.state == self.TEMPORALLY_OVERFLOWN_STATE:
            self.state = self.TEMPORALLY_OVERFLOWN_STATE

    def update_state_from_balance(self, balance):
        if balance >= 0:
            self.mark_as_ok()
        else:
            self.mark_as_overflown()

    def __str__(self):
        return self.state

    def __repr__(self):
        return self.__str__()
