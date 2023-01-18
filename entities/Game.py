# This file contains base abtract class for general game mode
from abc import ABC, abstractmethod
from datetime import datetime, timedelta

class Game(ABC):
    DELTA = timedelta(days=1)

    def __init__(self, name, timestamp) -> None:
        super().__init__()
        self.name = name
        self.lastAccess = datetime.utcnow() - Game.DELTA if timestamp == None else timestamp

    def __eq__(self, other) -> bool:
        return isinstance(self, Game) and isinstance(other, Game) and self.name == other.name

    def __hash__(self) -> int:
        return self.name.__hash__()

    def getID(self):
        """
        Returns ID of the game
        """
        return self.name

    @abstractmethod
    def newGame(self):
        """
        Creates new instance
        """
        self.lastAccess = datetime.utcnow()

    @abstractmethod
    def startGame(self):
        """
        Prepares and starts the game
        """
        pass

    @abstractmethod
    def nextMove(self):
        """
        Returns tuple of (template, args)
        """
        self.lastAccess = datetime.utcnow()

    @abstractmethod
    def getCSS(self):
        """
        Returns css file path
        """
        pass

    @abstractmethod
    def serializeNextMove(self):
        """
        Returns dictionary representation of detached data from db after nextMove
        """
        return {"$set" : {"timestamp" : self.lastAccess}}

    @abstractmethod
    def serialize(self):
        """
        Returns dictionary representation of game instance
        """
        return {
            "_id" : self.name,
            "timestamp" : self.lastAccess
        }

    @abstractmethod
    def deserialize(data):
        from entities.TaskGame import TaskGame
        from entities.PubGame import PubGame
        """
        Returns new instance from dictionary or None if data None
        """
        if data == None:
            return None

        if data['mode'] == "TaskMode":
            return TaskGame.deserialize(data)
        elif data['mode'] == "PubMode":
            return PubGame.deserialize(data)
        

    def continueGame(self):
        return datetime.utcnow() - self.lastAccess < Game.DELTA
