from flask import Flask, request, render_template, make_response, url_for, flash
from Game import Game
from TaskGame import TaskGame
# from PubGame import PubGame
import secrets


app = Flask(__name__)
app.config["TEMPLATES_AUTO_RELOAD"] = True
app.config['DEBUG'] = False
app.config['TESTING'] = False
secret = secrets.token_urlsafe(32)
app.secret_key = secret

games = {}

# Importing subrouting
import TaskGameEndpoints

@app.route('/login', methods=['POST'])
def login():
    gameID = request.form['gameID']
    if gameID not in games.keys():
        games[gameID] = TaskGame(gameID)
        
    game = games[gameID]
    # Change to select mode
    resp = make_response(render_template('categories.html', categories=TaskGame.getAllCategories(), selected=game.getCategories()))
    resp.set_cookie('gameID', gameID)

    return resp

@app.route('/home', methods=['GET'])
def home():
    return render_template('home-page.html')

@app.route('/')
def hello_world():
    return render_template('about.html')

@app.route('/favicon.ico')
def favicon():
    return url_for('static', filename='image/favicon.ico')

######################
# General game moves #
######################

@app.route('/start', methods=['POST'])
def start():
    game = games[request.cookies.get('gameID')]
    game.loadGame()
    game.newGame()

    if len(game.getPlayers()) < 2:
        flash("Nebud alkoholik, najdi si aspon jedneho ineho hraca")
        return render_template('lobby.html', players=game.getPlayers(), len=len(game.getPlayers()))

    return nextMove()

@app.route('/nextMove', methods=['GET'])
def nextMove():
    game = games[request.cookies.get('gameID')]

    template, args = game.nextMove()
    return make_response(render_template(template, **args))

@app.route('/css', methods=['GET'])
def getCSS():
    default = request.args.get('defaultCss')
    
    if default == 'true' or 'gameID' not in request.cookies:
        return "/static/css/default.css"

    game = games[request.cookies.get('gameID')]
        
    return game.getCSS()

if __name__ == "__main__":
    app.run(port=34743, host='0.0.0.0')