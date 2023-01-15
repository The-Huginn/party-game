import glob, json, random
from Game import Game
from pathlib import Path
from flask import render_template, flash
import copy

class TaskGame(Game):

    def __init__(self,
                name,
                timestamp = None,
                players = [],
                currentPlayer = 0,
                initialTasks = set(),
                currentTasks = [],
                cachedTasks = [],
                selected = set(),
                css = "/static/css/default.css"):
        super().__init__(timestamp)
        self.name = name
        self.players = players
        self.currentPlayer = currentPlayer
        self.initialTasks = initialTasks
        self.currentTasks = currentTasks
        self.cachedTasks = cachedTasks
        self.css = css
        self.selected = selected

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

        if name in self.players:
            return False

        self.players.append(name)
        return True

    def removePlayer(self, index):
        self.players.pop(index)

    def getCurrentPlayer(self):
        return self.players[self.currentPlayer]

    def newGame(self):
        super().newGame()
        for task in self.initialTasks:
            for x in range(task.frequency):
                self.currentTasks.append(copy.deepcopy(task))

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
                        self.initialTasks.add(Task(task, self.getPlayers()))

            f.close()

    def startGame(self):
        self.loadGame()
        self.newGame()

        if len(self.getPlayers()) < 2:
            flash("Nebud alkoholik, najdi si aspon jedneho ineho hraca")
            return render_template('lobby.html', players=self.getPlayers(), len=len(self.getPlayers()), title="Lobby pre pripravu hracov")

        return self.nextMove()

    def randomPlayers(self):

        available = [name for name in self.players if name != self.getCurrentPlayer()]
        random.shuffle(available)

        return available

    def getPlayers(self):
        return self.players

    def randomTeams(self):
        """
        returns list of randomly paired players' names
        """

        available = [name for name in self.players]
        random.shuffle(available)

        return list(zip(*[iter(available)]*2))

    def nextMove(self):
        super().nextMove()
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

        template = task.template
        args = task.args(self)

        if self.currentTasks[taskIndex].canRemove():
            self.currentTasks.pop(taskIndex)

        return template, args

    def getCSS(self):
        return self.css

    def getID(self):
        return self.name

    def serializeNextMove(self):
        timestamp = super().serializeNextMove()
        return {
            "timestamp" : timestamp,
            "currentPlayer" : self.currentPlayer,
            "css" : self.css,
            "currentTasks" : [task.serialize() for task in self.currentTasks],
            "cachedTasks" : [task.serialize() for task in self.cachedTasks]
        }

    def serializeSelected(self):
        return {"selected" : list(self.selected)}

    def serializePlayers(self):
        return {"players" : self.players}

    def serialize(self):
        timestamp = super().serialize()
        return {
            "_id" : self.name,
            "timestamp" : timestamp,
            "mode" : "TaskMode",
            "players" : self.players,
            "currentPlayer" : self.currentPlayer,
            "css" : self.css,
            "initialTasks" : [task.serialize() for task in self.initialTasks],
            "currentTasks" : [task.serialize() for task in self.currentTasks],
            "cachedTasks" : [task.serialize() for task in self.cachedTasks],
            "selected" : list(self.selected)
        }

    def deserialize(data):
        return TaskGame(
            data['_id'],
            data['timestamp'],
            data['players'],
            data['currentPlayer'],
            set([Task.deserialize(task) for task in data['initialTasks']]),
            [Task.deserialize(task) for task in data['currentTasks']],
            [Task.deserialize(task) for task in data['cachedTasks']],
            set(data['selected']),
            data['css']
        )

class Task:
    DEFAULT_TIMER = 30
    NEVER = "Never"
    ALWAYS = "Always"
    PER_PLAYER = "Once Per Player"

    def __init__(self, data, players) -> None:
        self.unresolvedTask = data['task']
        self.template = data.get('template', 'single-simple.html')
        self.frequency = data.get('frequency', 1)
        self.repeat = data.get('repeat', Task.NEVER)
        if self.repeat == Task.PER_PLAYER:
            self.players = players.copy()
        else:
            self.players = list()
        self.price = data.get('price', 1)
        self.message = data.get('message', 'Inak pijes')
        self.data = data
        self.data['players'] = self.players
    
    def checkJSON(data) -> bool:
        return 'task' in data

    def args(self, game):
        # Resolve task
        self.resolveTask(game)

        if self.repeat == Task.PER_PLAYER:
            self.players.remove(game.getCurrentPlayer())

        aux = {"task" : self.task, "price": self.price, "currentPlayer": game.getCurrentPlayer(), "message": self.message}

        if 'timer' in self.data or '<timer>' in self.unresolvedTask:
            aux["timer"] = self.data.get('timer', Task.DEFAULT_TIMER)

        if self.template[0:self.template.find('-')] == 'duo':
            aux["pairs"] = game.randomTeams()
        
        return aux

    def resolveTask(self, game):
        """
        Tries to resolve task and returns success of this operation
        """

        if self.repeat == Task.PER_PLAYER and game.getCurrentPlayer() not in self.players:
            return False

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
                value = players[int(placeholder)]

            self.task = self.task[0:self.task.find('<')] + value + self.task[self.task.find('>') + 1:]
        return True

    def canRemove(self):
        return self.repeat == "Never" or (self.repeat == Task.PER_PLAYER and len(self.players) == 0)

    def serialize(self):
        self.data['players'] = self.players
        return self.data

    def deserialize(data):
        return Task(data, data.get('players', list()))

    def getCSS(self):
        """
        We expect each template to have name like <type>-xxx.html,
        where <type> is equal to one of css styles
        """
        return self.template[0:self.template.find('-')]

    # # Immutable instances
    # def __setattr__(self, key, value):
    #     raise TypeError('Task cannot be modified after instantiation')
