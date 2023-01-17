from .GameService import GameService
from entities.PubGame import PubGame

class PubService(GameService):
    def __init__(self):
        super().__init__()