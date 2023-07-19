from flask import Blueprint, request, render_template, flash
from flask_babel import gettext
from services.TaskService import TaskService
from entities.TaskGame import TaskGame

service = TaskService()
task_page = Blueprint('task_page', __name__)

@task_page.route('/categories', methods=['POST'])
def categories():
    game = service.getGame(request.cookies.get('gameID'))
    categories = request.form.getlist('categories[]')

    if (len(categories) == 0):
        flash('py-one-category')
        return render_template('categories.html', categories=TaskGame.getAllCategories(), selected=game.getCategories(), title='py-lobby')

    service.setCategories(game, categories)

    return render_template('lobby.html', players=game.getPlayers(), len=len(game.getPlayers()), title='py-lobby')

@task_page.route('/addPlayer', methods=['POST'])
def addPlayer():
    name = request.form['name']
    game = service.getGame(request.cookies.get('gameID'))
    
    if not service.addPlayer(game, name):
        flash('py-add-player-fail')

    return render_template('lobby.html', players=game.getPlayers(), len=len(game.getPlayers()), title='py-lobby')

@task_page.route('/removePlayer', methods=['DELETE'])
def removePlayer():
    index = int(request.form['id'])
    game = service.getGame(request.cookies.get('gameID'))

    service.removePlayer(game, index)

    return render_template('lobby.html', players=game.getPlayers(), len=len(game.getPlayers()), title='py-lobby')

@task_page.route('/TaskMode', methods=['POST'])
def taskMode():
    _id = request.cookies.get('gameID')
    game = service.getGame(_id)
    
    if not isinstance(game, TaskGame):
        service.deleteGame(_id)
        game = None
    
    if game == None:
        game = service.newTaskGame(request.cookies.get('gameID'))
    else:
        game = service.resetTaskGame(game)
    
    return render_template('categories.html', categories=TaskGame.getAllCategories(), selected=game.getCategories(), title='py-category')
