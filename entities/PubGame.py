import json, random
from Game import Game

class PubGame(Game):
    ROUNDS = 12

    def __init__(self,
            name,
            timestamp = None,
            tasks = [],
            currentTask = 0):
        super().__init__(timestamp)
        self.name = name
        self.tasks = tasks
        self.currentTask = currentTask
        self.template = "pub.html"

    def __eq__(self, other) -> bool:
        return isinstance(self, Game) and isinstance(other, Game) and self.name == other.name

    def __hash__(self) -> int:
        return self.name.__hash__()

    def __repr__(self) -> str:
        return "Pub Mode: " + self.name

    def newGame(self):
        super().newGame()
        self.tasks.clear()

        f = open("tasks/PubMode/pub.json", "r")
        data = json.loads(f.read())

        for task in data['tasks']:
            self.tasks.append(task['task'])


        random.shuffle(self.tasks)

        self.tasks = [self.tasks[i] for i in range(PubGame.ROUNDS)]

    def startGame(self):
        self.newGame()

        # Possibly window explaining game mode at first
        return self.nextMove()

    def nextMove(self):
        super().nextMove()
        print(self.tasks)
        print(self.currentTask)

        args = {"task" : self.tasks[self.currentTask]}
        self.currentTask = self.currentTask + 1

        return self.template, args

    # Some temporary solution
    def getCSS(self):
        if self.currentTask % 3 == 0:
            return "/static/css/single.css"
        elif self.currentTask % 3 == 1:
            return "/static/css/duo.css"

        return "/static/css/all.css"

    def getID(self):
        return self.name

    def serializeNextMove(self):
        update = super().serializeNextMove()
        update_set = update['$set']
        update_set['currentTask'] = self.currentTask
        update['$set'] = update_set
        return update

    def serialize(self):
        timestamp = super().serialize()
        return {
            "_id" : self.name,
            "timestamp" : timestamp,
            "mode" : "PubMode",
            "tasks" : self.tasks,
            "currentTask" : self.currentTask
        }

    def deserialize(data):
        return PubGame(
            data['_id'],
            data['timestamp'],
            data['tasks'],
            data['currentTask']
        )