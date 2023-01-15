from flask import Flask, request, render_template, make_response, url_for, flash
from services.TaskService import TaskService
from TaskGame import TaskGame
from __main__ import app

service = TaskService()

@app.route('/categories', methods=['POST'])
def categories():
    game = service.getGame(request.cookies.get('gameID'))
    categories = request.form.getlist('categories[]')

    if (len(categories) == 0):
        flash("Zvolte aspon jednu kategoriu")
        return render_template('categories.html', categories=TaskGame.getAllCategories(), selected=game.getCategories(), title="Lobby pre pripravu hracov")

    service.setCategories(game, categories)

    return render_template('lobby.html', players=game.getPlayers(), len=len(game.getPlayers()), title="Lobby pre pripravu hracov")

@app.route('/addPlayer', methods=['POST'])
def addPlayer():
    name = request.form['name']
    game = service.getGame(request.cookies.get('gameID'))
    
    if not service.addPlayer(game, name):
        flash("Pridanie sa nepodarilo, pouzivatel uz existuje alebo zadane meno je kratke")

    return render_template('lobby.html', players=game.getPlayers(), len=len(game.getPlayers()), title="Lobby pre pripravu hracov")

@app.route('/removePlayer', methods=['DELETE'])
def removePlayer():
    index = int(request.form['id'])
    game = service.getGame(request.cookies.get('gameID'))

    service.removePlayer(game, index)

    return render_template('lobby.html', players=game.getPlayers(), len=len(game.getPlayers()), title="Lobby pre pripravu hracov")

@app.route('/TaskMode', methods=['POST'])
def taskMode():
    game = service.getGame(request.cookies.get('gameID'))
    
    return render_template('categories.html', categories=TaskGame.getAllCategories(), selected=game.getCategories(), title="Vyberte si kategorie")
