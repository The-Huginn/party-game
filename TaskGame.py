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
                tasks = [],
                currentTask = 0,
                selected = set(),
                css = "/static/css/default.css"):
        super().__init__(timestamp)
        self.name = name
        self.players = players
        self.currentPlayer = currentPlayer
        self.tasks = tasks
        self.currentTask = currentTask
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
        self.tasks.clear()

        perPlayerTasks = list()
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
                        loadedTask = Task(task)
                        for x in range(loadedTask.frequency):
                            if loadedTask.repeat == Task.PER_PLAYER:
                                perPlayerTasks.append(loadedTask)
                            else:
                                self.tasks.append(copy.deepcopy(loadedTask))

            f.close()

        random.shuffle(self.tasks)

        print("Before insert:" + str([task.unresolvedTask for task in self.tasks]))
        playerTasks = list()
        for i in range(len(self.players)):
            random.shuffle(perPlayerTasks)
            playerTasks.append(copy.deepcopy(perPlayerTasks))

        # Every batch we seed every player one task
        batch = len(self.tasks) // len(perPlayerTasks)
        # Max step we can make to cycle and overlap for new player (len(players) + 1) - mod
        step = (batch + len(self.players)) // (len(self.players) + 1) - 1
        # size of batch with PER_PLAYER tasks in
        batch = batch + len(self.players)
        for batchNum in range(len(perPlayerTasks)):
            index = batch * batchNum
            for player in range(len(self.players)):
                task = playerTasks[player][batchNum]
                self.tasks.insert(index, task)
                index = step + 1

        print("After insert:" + str([task.unresolvedTask for task in self.tasks]))
            

    def startGame(self):
        if len(self.getPlayers()) < 2:
            flash("Nebud alkoholik, najdi si aspon jedneho ineho hraca")
            return render_template('lobby.html', players=self.getPlayers(), len=len(self.getPlayers()), title="Lobby pre pripravu hracov")

        self.newGame()

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

    def nextTask(self):
        self.currentTask = self.currentTask + 1
        if (self.currentTask >= len(self.tasks)):
            self.currentTask = 0

    # We do not delete unresolvable / removable task. This is done in service
    def nextMove(self):
        print(str([task.unresolvedTask for task in self.tasks]))
        super().nextMove()
        self.currentPlayer = self.currentPlayer + 1
        if (self.currentPlayer >= len(self.players)):
            self.currentPlayer = 0

        toDelete = 0

        while True:
            task = self.tasks[self.currentTask]
            if task.canResolve(self):
                break

            # If we overflow we should have only resolvable tasks
            # Note in case this does no hold true service has to be updated
            toDelete = toDelete + 1
            self.nextTask()

        self.css = "/static/css/" + task.getCSS() + ".css"

        template = task.template
        args = task.templateArgs(self)

        if self.tasks[self.currentTask].canRemove():
            print("delete")
            toDelete = toDelete + 1

        update = super().serializeNextMove()
        update_set = update['$set']
        update_set['css'] = self.css
        update_set['currentPlayer'] = self.currentPlayer
        if toDelete > 0:
            while toDelete > 0:
                update['$unset'] = {f"tasks.{self.currentTask - toDelete + 1}" : 1}
                toDelete = toDelete - 1
        else:
            self.nextTask()
            update_set['currentTask'] = self.currentTask

        update['$set'] = update_set
            
        return template, args, update

    def getCSS(self):
        return self.css

    def getID(self):
        return self.name

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
            "currentTask" : self.currentTask,
            "tasks" : [task.serialize() for task in self.tasks],
            "selected" : list(self.selected)
        }

    def deserialize(data):
        return TaskGame(
            data['_id'],
            data['timestamp'],
            data['players'],
            data['currentPlayer'],
            [Task.deserialize(task) for task in data['tasks']],
            data['currentTask'],
            set(data['selected']),
            data['css']
        )

class Task:
    DEFAULT_TIMER = 30
    NEVER = "NEVER"
    ALWAYS = "ALWAYS"
    PER_PLAYER = "ONCE PER PLAYER"

    def __init__(self, data) -> None:
        self.unresolvedTask = data['task']
        self.template = data.get('template', 'single-simple.html')
        self.frequency = data.get('frequency', 1)
        self.repeat = data.get('repeat', Task.NEVER).upper()
        self.price = data.get('price', 1)
        self.message = data.get('message', 'Inak pijes')
        self.timer = data.get('timer', None)
    
    def checkJSON(data) -> bool:
        return 'task' in data

    def canResolve(self, game):
        """
        Checks, whether a task is resolvable
        """
        maxLen = len(game.randomPlayers())
        while self.unresolvedTask.find('<') != -1:
            placeholder = self.unresolvedTask[self.unresolvedTask.find('<') + 1:self.unresolvedTask.find('>')]
            if placeholder[0].isdigit() and int(placeholder) >= maxLen:
                return False

        return True

    def templateArgs(self, game):
        """
        Resolves task and returns tuple(args, changed)
        `args` should be used to resolve template
        `changed` True if Task was changed and needs to be saved in DB
        """
        # Resolve task
        self.resolveTask(game)

        aux = {"task" : self.task, "price": self.price, "currentPlayer": game.getCurrentPlayer(), "message": self.message}

        if self.timer != None or '<timer>' in self.unresolvedTask:
            aux["timer"] = self.timer if self.timer != None else Task.DEFAULT_TIMER

        if self.template[0:self.template.find('-')] == 'duo':
            aux["pairs"] = game.randomTeams()
        
        return aux

    def resolveTask(self, game):
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
        return self.repeat != Task.ALWAYS

    def serialize(self):
        serialized = {
            "task" : self.unresolvedTask,
            "template" : self.template,
            "frequency" : self.frequency,
            "repeat" : self.repeat,
            "price" : self.price,
            "message" : self.message
        }
        if self.timer != None:
            serialized['timer'] = self.timer
        return serialized

    def deserialize(data):
        return Task(data)

    def getCSS(self):
        """
        We expect each template to have name like <type>-xxx.html,
        where <type> is equal to one of css styles
        """
        return self.template[0:self.template.find('-')]

    # # Immutable instances
    # def __setattr__(self, key, value):
    #     raise TypeError('Task cannot be modified after instantiation')
