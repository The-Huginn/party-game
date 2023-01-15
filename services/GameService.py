from pymongo import MongoClient
from Game import Game
import os
from dotenv import load_dotenv, find_dotenv

load_dotenv(find_dotenv())

class GameService():
    def __init__(self):
        USER = os.environ.get('DB_USER')
        PASSWORD = os.environ.get('DB_PASSWORD')
        HOST = os.environ.get('DB_HOST')
        PORT = os.environ.get('DB_PORT')
        # NAME = os.environ.get('DB_NAME')
        client = MongoClient(f'mongodb://{USER}:{PASSWORD}@{HOST}:{PORT}')
        self.db = client['party-game']['games']

    def getGame(self, _id):
        return Game.deserialize(self.db.find_one({"_id" : _id}))

    def saveGame(self, game: Game):
        return self.db.replace_one({"_id" : game.getID()}, game.serialize(), upsert=True)

    def gameExists(self, _id):
        return self.db.find_one({"_id" : _id}) != None

    def startGame(self, game: Game):
        template, args = game.startGame()
        self.saveGame(game)

        return template, args

    def nextMove(self, game: Game):
        template, args = game.nextMove()
        self.db.update_one({"_id" : game.getID()},
                            {"$set" : game.serializeNextMove()})

        return template, args

    def print(self):
        items = self.db.find()
        for item in items:
            print(item)