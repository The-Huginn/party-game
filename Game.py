# This file contains base abtract class for general game mode
from abc import ABC, abstractmethod
from datetime import datetime, timedelta

class Game(ABC):
    DELTA = timedelta(days=1)

    def __init__(self) -> None:
        super().__init__()
        self.lastAccess = datetime.utcnow() - Game.DELTA

    @abstractmethod
    def newGame(self):
        """
        Creates new instance
        """
        self.lastAccess = datetime.utcnow()

    @abstractmethod
    def loadGame(self):
        """
        Loads the selected setup
        """
        pass

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
    def getID(self):
        """
        Returns ID of the game
        """
        pass

    @abstractmethod
    def serializeNextMove(self):
        """
        Returns dictionary representation of detached data from db after nextMove
        """
        pass

    @abstractmethod
    def serialize(self):
        """
        Returns dictionary representation of game instance
        """
        pass

    @abstractmethod
    def deserialize(data):
        from TaskGame import TaskGame
        """
        Returns new instance from dictionary
        """
        if data['mode'] == "TaskMode":
            return TaskGame.deserialize(data)
        

    def continueGame(self):
        return datetime.utcnow() - self.lastAccess < Game.DELTA
