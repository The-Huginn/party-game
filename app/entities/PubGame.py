import json, random
from entities.Game import Game
from flask_babel import gettext

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

        f = open(f'{Game.TASK_PATH}tasks/PubMode/pub.json', "r")
        data = json.loads(f.read())

        for task in data['tasks']:
            # Yes, it is for sure in here, not rigged at all 🙃
            if 'za jednu ruku' in task['task']:
                special = task['task']
            self.tasks.append(task['task'])


        random.shuffle(self.tasks)

        self.tasks = [self.tasks[i] for i in range(PubGame.ROUNDS)]
        self.tasks[6] = special

    def startGame(self):
        self.newGame()

        # Possibly window explaining game mode at first
        return self.template, {
            'title' : 'py-rules',
            'buttonName' : gettext('py-start'),
            'task' : '''
                        <p>''' + gettext('py-pub-message-1') + '''<br><br></p>
                        <ol>
                            <li>''' + gettext('py-pub-message-2') + '''</li>
                            <li>''' + gettext('py-pub-message-3') + '''</li>
                            <li>''' + gettext('py-pub-message-4') + '''</li>
                        </ol>
                        <p><br>''' + gettext('py-pub-message-5') + '''<br></p>
                        <p><br>''' + gettext('py-pub-message-6') + '''</p>
                        '''
        }

    def nextMove(self):
        super().nextMove()

        if self.currentTask >= PubGame.ROUNDS:
            return self.template, {'title' : gettext('py-congratulations'), 'task' : gettext('py-finished') + ' ' + str(PubGame.ROUNDS) + ' ' + gettext('py-pub-game'), 'noButton' : ''}

        args = {'task' : self.tasks[self.currentTask], 'title' : 'py-task-number', 'title_static' : str(self.currentTask + 1)}
        self.currentTask = self.currentTask + 1

        return self.template, args
    
    def currentMove(self):
        return super().currentMove()

    # Some temporary solution
    def getCSS(self):
        if self.currentTask % 3 == 0:
            return Game.CSS_PATH + "single.css"
        elif self.currentTask % 3 == 1:
            return Game.CSS_PATH + "duo.css"

        return Game.CSS_PATH + "all.css"

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