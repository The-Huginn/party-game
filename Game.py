# This file contains base abtract class for general game mode
from abc import ABC, abstractmethod

class Game(ABC):

    @abstractmethod
    def newGame(self):
        """
        Creates new instance
        """
        pass

    @abstractmethod
    def loadGame(self):
        """
        Loads the selected setup
        """
        pass

    @abstractmethod
    def nextMove(self):
        """
        Returns tuple of (template, args)
        """
        pass

    @abstractmethod
    def getCSS(self):
        """
        Returns css file path
        """
        pass