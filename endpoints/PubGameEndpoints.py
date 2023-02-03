from flask import Blueprint, request, render_template
from services.PubService import PubService
from entities.PubGame import PubGame

service = PubService()
pub_page = Blueprint('pub_page', __name__)

@pub_page.route('/PubMode', methods=['POST'])
def pubMode():
    _id = request.cookies.get('gameID')
    game = service.getGame(_id)
    
    if not isinstance(game, PubGame):
        service.deleteGame(_id)
    
    game = service.newPubGame(_id)

    template, args = service.startGame(game)
    return render_template(template, **args)