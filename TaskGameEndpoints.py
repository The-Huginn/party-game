from flask import Flask, request, render_template, make_response, url_for, flash
import TaskGame
from __main__ import app, games

@app.route('/categories', methods=['POST'])
def categories():
    game = games[request.cookies.get('gameID')]
    categories = request.form.getlist('categories[]')

    if (len(categories) == 0):
        flash("Zvolte aspon jednu kategoriu")
        return render_template('categories.html', categories=TaskGame.getAllCategories(), selected=game.getCategories())

    game.setCategories(categories)

    return render_template('lobby.html', players=game.getPlayers(), len=len(game.getPlayers()))

@app.route('/addPlayer', methods=['POST'])
def addPlayer():
    name = request.form['name']
    game = games[request.cookies.get('gameID')]
    
    if not game.addPlayer(name):
        flash("Pridanie sa nepodarilo, pouzivatel uz existuje alebo zadane meno je kratke")

    return render_template('lobby.html', players=game.getPlayers(), len=len(game.getPlayers()))

@app.route('/removePlayer', methods=['DELETE'])
def removePlayer():
    index = int(request.form['id'])
    game = games[request.cookies.get('gameID')]

    game.removePlayer(index)

    return render_template('lobby.html', players=game.getPlayers(), len=len(game.getPlayers()))
