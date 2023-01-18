from .GameService import GameService
from TaskGame import TaskGame

class TaskService(GameService):
    def __init__(self):
        super().__init__()

    def newTaskGame(self, _id):
        game = TaskGame(_id)
        self.saveGame(game)
        return game

    def setCategories(self, game: TaskGame, categories):
        game.setCategories(categories)
        self.db.update_one({"_id" : game.getID()},
                            {"$set" : game.serializeSelected()})

    def addPlayer(self, game: TaskGame, player):
        if not game.addPlayer(player):
            return False

        self.db.update_one({"_id" : game.getID()},
                            {"$set" : game.serializePlayers()})
        return True

    def removePlayer(self, game: TaskGame, index):
        game.removePlayer(index)
        self.db.update_one({"_id" : game.getID()},
                            {"$set" : game.serializePlayers()})