from flask import Flask, request, render_template, make_response, url_for, send_from_directory
from flask_babel import Babel, gettext
from services.GameService import GameService
import secrets


app = Flask(__name__)
babel = Babel(app)
app.config["TEMPLATES_AUTO_RELOAD"] = True
app.config['DEBUG'] = False
app.config['TESTING'] = False
app.config['LANGUAGES'] = {
    'en': 'English',
    'sk': 'Slovenčina'
}
secret = secrets.token_urlsafe(32)
app.secret_key = secret

service = GameService()

# Importing subrouting
from endpoints.TaskGameEndpoints import task_page
from endpoints.PubGameEndpoints import pub_page

app.register_blueprint(task_page)
app.register_blueprint(pub_page)

@app.route('/robots.txt')
@app.route('/sitemap.xml')
def static_seo():
    return send_from_directory(app.static_folder, request.path[1:])

def get_locale():
    return request.accept_languages.best_match(app.config['LANGUAGES'].keys())

babel.init_app(app, locale_selector=get_locale)

@app.route('/gameMode', methods=['POST', 'GET'])
def gameMode():
    if request.method == 'GET':
        return render_template('mode-selection.html', title=gettext('Choose game mode'))

    gameID = request.form['gameID']
    if service.getGame(gameID) != None and service.getGame(gameID).continueGame():
        # We can continue previously played game

       resp = make_response(render_template('continue.html'))
       resp.set_cookie('gameID', gameID)
       return resp
        
    resp = make_response(render_template('mode-selection.html', title=gettext('Choose game mode')))
    resp.set_cookie('gameID', gameID)

    return resp

@app.route('/home', methods=['GET'])
def home():
    return render_template('home-page.html', title=gettext('Choose your unique game name'))

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

@app.errorhandler(404)
def defaultHandler(e):
    return render_template('404.html'), 404

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
        filename = "/static/css/default.css"
    else:
        game = service.getGame(request.cookies.get('gameID'))
        filename = game.getCSS()
        
    filename = filename[1:]
    with open(filename, 'r') as file:
        data = file.read().replace('\n', ' ')

    return data

if __name__ == "__main__":
    app.run(host='0.0.0.0')