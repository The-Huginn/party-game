from flask import Blueprint, request, render_template
from services.PubService import PubService
from entities.PubGame import PubGame

service = PubService()
pub_page = Blueprint('pub_page', __name__)

@pub_page.route('/PubMode', methods=['POST'])
def pubMode():
    game = PubGame(request.cookies.get('gameID'))
    game.newGame()
    template, args = game.startGame()
    service.saveGame(game)
    return render_template(template, **args)