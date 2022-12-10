from flask import Flask, request, render_template, make_response, url_for, flash
from game import Game
import secrets


app = Flask(__name__)
app.config["TEMPLATES_AUTO_RELOAD"] = True
app.config['DEBUG'] = False
app.config['TESTING'] = False

secret = secrets.token_urlsafe(32)
app.secret_key = secret

games = {}
@app.route('/login', methods=['POST'])
def login():
    game = request.form['gameID']
    if game not in games.keys():
        games[game] = Game(game)

        # flash("Game already exists")
        # return render_template('login.html')

    resp = make_response(render_template('lobby.html', players=games[game].getPlayers(), len=len(games[game].getPlayers())))
    resp.set_cookie('gameID', game)

    return resp

@app.route('/addPlayer', methods=['POST'])
def addPlayer():
    name = request.form['name']
    game = games[request.cookies.get('gameID')]
    
    if not game.addPlayer(name):
        flash("Pridanie sa nepodarilo, pouzivatul uz existuje alebo zadane meno je kratke")

    return render_template('lobby.html', players=game.getPlayers(), len=len(game.getPlayers()))

@app.route('/removePlayer', methods=['DELETE'])
def removePlayer():
    index = int(request.form['id'])
    game = games[request.cookies.get('gameID')]

    game.removePlayer(index)

    return render_template('lobby.html', players=game.getPlayers(), len=len(game.getPlayers()))

@app.route('/start', methods=['POST'])
def start():
    game = games[request.cookies.get('gameID')]
    game.addTasks(["all"])
    game.newGame()

    if len(game.getPlayers()) < 2:
        flash("Aspon 2 hraci pre start hry")
        return render_template('lobby.html', players=game.getPlayers(), len=len(game.getPlayers()))

    return nextMove()

@app.route('/')
def hello_world():
    return render_template('index.html')

@app.route('/css', methods=['GET'])
def getCSS():
    game = games[request.cookies.get('gameID')]
    return game.getCSS()

@app.route('/favicon.ico')
def favicon():
    return url_for('static', filename='image/favicon.ico')

@app.route('/nextMove', methods=['GET'])
def nextMove():
    game = games[request.cookies.get('gameID')]

    template, args = game.nextMove()
    return make_response(render_template(template, **args))

if __name__ == "__main__":
    app.run(port=34743, host='0.0.0.0')