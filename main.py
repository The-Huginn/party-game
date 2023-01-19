from flask import Flask, request, render_template, make_response, url_for, flash
from services.GameService import GameService
import secrets


app = Flask(__name__)
app.config["TEMPLATES_AUTO_RELOAD"] = True
app.config['DEBUG'] = False
app.config['TESTING'] = False
secret = secrets.token_urlsafe(32)
app.secret_key = secret

service = GameService()

# Importing subrouting
from endpoints.TaskGameEndpoints import task_page
from endpoints.PubGameEndpoints import pub_page

app.register_blueprint(task_page)
app.register_blueprint(pub_page)

@app.route('/gameMode', methods=['POST', 'GET'])
def gameMode():
    if request.method == 'GET':
        return render_template('mode-selection.html', title="Vyberte si mod hry")

    gameID = request.form['gameID']
    if service.getGame(gameID) != None and service.getGame(gameID).continueGame():
        # We can continue previously played game

       resp = make_response(render_template('continue.html'))
       resp.set_cookie('gameID', gameID)
       return resp
        
    resp = make_response(render_template('mode-selection.html', title="Vyberte si mod hry"))
    resp.set_cookie('gameID', gameID)

    return resp

@app.route('/home', methods=['GET'])
def home():
    return render_template('home-page.html', title="Zvolte unikatne meno hry")

@app.route('/')
def hello_world():
    return render_template('about.html')

@app.route('/favicon.ico')
def favicon():
    return url_for('static', filename='image/favicon.ico')

@app.route('/health', methods=['GET'])
def health():
    return 'OK', 200

@app.route('/ready', methods=['GET'])
def ready():
    return 'OK', 200

######################
# General game moves #
######################

@app.route('/start', methods=['POST'])
def start():
    game = service.getGame(request.cookies.get('gameID'))

    template, args = service.startGame(game)
    return make_response(render_template(template, **args))

@app.route('/nextMove', methods=['GET'])
def nextMove():
    game = service.getGame(request.cookies.get('gameID'))

    template, args = service.nextMove(game)
    return make_response(render_template(template, **args))

@app.route('/css', methods=['GET'])
def getCSS():
    default = request.args.get('defaultCss')
    
    if default == 'true' or 'gameID' not in request.cookies:
        return "/static/css/default.css"

    game = service.getGame(request.cookies.get('gameID'))
        
    return game.getCSS()

if __name__ == "__main__":
    app.run(host='0.0.0.0')