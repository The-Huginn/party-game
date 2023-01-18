from flask import Blueprint, request, render_template, flash
from services.TaskService import TaskService
from entities.TaskGame import TaskGame

service = TaskService()
task_page = Blueprint('task_page', __name__)

@task_page.route('/categories', methods=['POST'])
def categories():
    game = service.getGame(request.cookies.get('gameID'))
    categories = request.form.getlist('categories[]')

    if (len(categories) == 0):
        flash("Zvolte aspon jednu kategoriu")
        return render_template('categories.html', categories=TaskGame.getAllCategories(), selected=game.getCategories(), title="Lobby pre pripravu hracov")

    service.setCategories(game, categories)

    return render_template('lobby.html', players=game.getPlayers(), len=len(game.getPlayers()), title="Lobby pre pripravu hracov")

@task_page.route('/addPlayer', methods=['POST'])
def addPlayer():
    name = request.form['name']
    game = service.getGame(request.cookies.get('gameID'))
    
    if not service.addPlayer(game, name):
        flash("Pridanie sa nepodarilo, pouzivatel uz existuje alebo zadane meno je kratke")

    return render_template('lobby.html', players=game.getPlayers(), len=len(game.getPlayers()), title="Lobby pre pripravu hracov")

@task_page.route('/removePlayer', methods=['DELETE'])
def removePlayer():
    index = int(request.form['id'])
    game = service.getGame(request.cookies.get('gameID'))

    service.removePlayer(game, index)

    return render_template('lobby.html', players=game.getPlayers(), len=len(game.getPlayers()), title="Lobby pre pripravu hracov")

@task_page.route('/TaskMode', methods=['POST'])
def taskMode():
    _id = request.cookies.get('gameID')
    game = service.getGame(_id)
    
    if not isinstance(game, TaskGame):
        service.deleteGame(_id)
        game = None
    
    if game == None:
        game = service.newTaskGame(request.cookies.get('gameID'))
    
    return render_template('categories.html', categories=TaskGame.getAllCategories(), selected=game.getCategories(), title="Vyberte si kategorie")
