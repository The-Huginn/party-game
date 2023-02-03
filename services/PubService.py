from .GameService import GameService
from entities.PubGame import PubGame

class PubService(GameService):
    def __init__(self):
        super().__init__()

    def newPubGame(self, _id):
        game = PubGame(_id)
        self.saveGame(game)
        return game