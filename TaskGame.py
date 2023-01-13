import glob, json, random
from Game import Game
from pathlib import Path
from flask import render_template

# Currently we do not support statistics
class Player:

    def __init__(self, name) -> None:
        self.name = name
        self.shots = 0
        self.done = 0
        self.failed = 0

    def __eq__(self, other) -> bool:
        return isinstance(self, Player) and isinstance(other, Player) and self.name == other.name

    def __hash__(self) -> int:
        return self.name.__hash__()

    def __repr__(self) -> str:
        return self.name + " {" + str(self.done) + "," + str(self.failed) + "," + str(self.shots) + "}"

    def failed(self, shots):
        self.failed = self.failed + 1
        self.shots = self.shots + shots

    def done(self, shots):
        self.done = self.done + 1
        self.shots = self.shots + shots


class TaskGame(Game):

    def __init__(self, name) -> None:
        super().__init__()
        self.name = name
        self.players = []
        self.currentPlayer = 0
        self.initialTasks = set()
        self.currentTasks = []
        self.cachedTasks = []
        self.css = "/static/css/default.css"
        self.selected = set()

    def __eq__(self, other) -> bool:
        return isinstance(self, Game) and isinstance(other, Game) and self.name == other.name

    def __hash__(self) -> int:
        return self.name.__hash__()

    def __repr__(self) -> str:
        return self.name + " { players: " + self.players + ", current: " + self.players[self.currentPlayer] + "}"

    def getAllCategories():
        categories = [Path(x).stem for x in glob.glob('tasks/*.json')]
        return categories

    def getCategories(self):
        return self.selected

    def setCategories(self, categories):
        self.selected.clear()
        self.selected = set(categories)

    def addPlayer(self, name) -> bool:

        if (len(name) < 3):
            return False

        player = Player(name)

        if player in self.players:
            return False

        self.players.append(player)
        return True

    def removePlayer(self, index):
        self.players.pop(index)

    def getCurrentPlayer(self):
        return self.players[self.currentPlayer]

    def loadGame(self):
        super().loadGame()
        self.initialTasks.clear()
        self.currentTasks.clear()
        self.cachedTasks.clear()

        for filename in self.selected:
            f = open("tasks/" + filename + ".json", "r")
            data = json.loads(f.read())

            if 'tasks' not in data:
                print("Missing tasks in file: " + filename)
            else:
                for task in data['tasks']:
                    if not Task.checkJSON(task):
                        print("Corrupted task")
                    else:
                        self.initialTasks.add(Task(task))

            f.close()

    def newGame(self):
        super().newGame()
        for task in self.initialTasks:
            for x in range(task.frequency):
                self.currentTasks.append(task)

    def randomPlayers(self):

        available = [i for i in self.players if i.name != self.getCurrentPlayer().name]
        random.shuffle(available)

        return available

    def getPlayers(self):
        return self.players

    def randomTeams(self):
        """
        returns list of randomly paired players' names
        """

        available = [i.name for i in self.players]
        random.shuffle(available)

        return list(zip(*[iter(available)]*2))

    def nextMove(self):
        super().newGame()
        self.currentPlayer = self.currentPlayer + 1
        if (self.currentPlayer >= len(self.players)):
            self.currentPlayer = 0

        while True:
            taskIndex = random.randrange(0, len(self.currentTasks))
            task = self.currentTasks[taskIndex]
            if task not in self.cachedTasks and task.resolveTask(self) == True:
                break
        
        self.cachedTasks.append(task)
        # We will allow same tasks after 1/5 of others tasks were done
        # Note not exactly true as some tasks are removed
        if len(self.cachedTasks) >= len(self.initialTasks) / 5:
            self.cachedTasks.pop(0)

        self.css = "/static/css/" + task.getCSS() + ".css"

        if self.currentTasks[taskIndex].repeat == False:
            self.currentTasks.pop(taskIndex)

        return task.template, task.args(self)

    def getCSS(self):
        super().getCSS()
        return self.css


class Task:
    DEFAULT_TIMER = 30

    def __init__(self, data) -> None:
        self.unresolvedTask = data['task']
        self.template = data.get('template', 'single-simple.html')
        self.frequency = data.get('frequency', 1)
        self.repeat = data.get('repeat', False)
        self.price = data.get('price', 1)
        self.message = data.get('message', 'Inak pijes')
        self.data = data
    
    def checkJSON(data) -> bool:
        return 'task' in data

    def args(self, game):
        # Resolve task
        self.resolveTask(game)

        aux = {"task" : self.task, "price": self.price, "currentPlayer": game.getCurrentPlayer().name, "message": self.message}

        if 'timer' in self.data or '<timer>' in self.unresolvedTask:
            aux["timer"] = self.data.get('timer', Task.DEFAULT_TIMER)

        if self.template[0:self.template.find('-')] == 'duo':
            aux["pairs"] = game.randomTeams()
        

        return aux

    def resolveTask(self, game):
        """
        Tries to resolve task and returns success of this operation
        """

        self.task = self.unresolvedTask
        players = game.randomPlayers()

        # Replace place holders for random players
        while self.task.find('<') != -1:
            placeholder = self.task[self.task.find('<') + 1:self.task.find('>')]
            value = "\"foo you should not see :)\""
            if placeholder == 'timer':
                value = str(self.data.get('timer', Task.DEFAULT_TIMER))
            elif placeholder[0].isdigit():
                if int(placeholder) >= len(players):
                    return False
                value = players[int(placeholder)].name

            self.task = self.task[0:self.task.find('<')] + value + self.task[self.task.find('>') + 1:]
        return True

    def getCSS(self):
        """
        We expect each template to have name like <type>-xxx.html,
        where <type> is equal to one of css styles
        """
        return self.template[0:self.template.find('-')]

    # # Immutable instances
    # def __setattr__(self, key, value):
    #     raise TypeError('Task cannot be modified after instantiation')
