import json, random
from entities.Game import Game

class PubGame(Game):
    ROUNDS = 12

    def __init__(self,
            name,
            timestamp = None,
            tasks = [],
            currentTask = 0):
        super().__init__(name, timestamp)
        self.tasks = tasks
        self.currentTask = currentTask
        self.template = "pub.html"

    def __eq__(self, other) -> bool:
        return Game.__eq__(self, other)

    def __hash__(self) -> int:
        return Game.__hash__(self)

    def __repr__(self) -> str:
        return "Pub Mode: " + self.name

    def newGame(self):
        super().newGame()
        self.tasks.clear()
        self.currentTask = 0

        f = open("tasks/PubMode/pub.json", "r")
        data = json.loads(f.read())

        for task in data['tasks']:
            self.tasks.append(task['task'])


        random.shuffle(self.tasks)

        self.tasks = [self.tasks[i] for i in range(PubGame.ROUNDS)]

    def startGame(self):
        self.newGame()

        # Possibly window explaining game mode at first
        return self.template, {
            'title' : 'Rules',
            'task' : '''
                        <p>You should stop at each pub for one drink, preferrebly pint. You should ideally follow these steps.<br><br></p>
                        <ol>
                            <li>Always before leaving pick a new pub.</li>
                            <li>Check next task on your list.</li>
                            <li>Follow the task assigned to you</li>
                        </ol>
                        <p><br>When someone gets caught breaking the task-rule he should either <u>Drink an extra shot</u> or <u>Buy drink in the next pub to the one, who caught you</u> and continue to follow the task-rule<br></p>
                        <p><br>And lastly have a lovely night out! Enjoy!</p>
                        '''
        }

    def nextMove(self):
        super().nextMove()

        if self.currentTask >= PubGame.ROUNDS:
            return self.template, {'title' : 'Congratulations!', 'task' : 'You have finished the ' + str(PubGame.ROUNDS) + ' Pub game!'}

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
        data = super().serialize()
        data.update(
            {
            "mode" : "PubMode",
            "tasks" : self.tasks,
            "currentTask" : self.currentTask
        }
        )
        return data

    def deserialize(data):
        return PubGame(
            data['_id'],
            data['timestamp'],
            data['tasks'],
            data['currentTask']
        )