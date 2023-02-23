from flask import Flask, request, session, render_template, make_response, url_for, send_from_directory
from flask_babel import Babel, gettext
from services.GameService import GameService
import secrets, glob, json


app = Flask(__name__)
babel = Babel(app)
app.config["TEMPLATES_AUTO_RELOAD"] = True
app.config['DEBUG'] = False
app.config['TESTING'] = False
app.config['BABEL_TRANSLATION_DIRECTORIES'] = 'i18n'
# app.config['SESSION_COOKIE_SAMESITE'] = "None"
# app.config['SESSION_COOKIE_SECURE'] = True

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

@app.route('/language')
def get_locale():
    return request.cookies.get('language', request.accept_languages.best_match(app.config['LANGUAGES'].keys()))

babel.init_app(app, locale_selector=get_locale)

@app.route('/gameMode', methods=['POST', 'GET'])
def gameMode():
    if request.method == 'GET':
        return render_template('mode-selection.html', title='py-game-mode')

    gameID = request.form['gameID']
    if service.getGame(gameID) != None and service.getGame(gameID).continueGame():
        # We can continue previously played game

       resp = make_response(render_template('continue.html'))
       resp.set_cookie('gameID', gameID)
       return resp
        
    resp = make_response(render_template('mode-selection.html', title='py-game-mode'))
    resp.set_cookie('gameID', gameID)

    return resp

@app.route('/home', methods=['GET'])
def home():
    return render_template('home-page.html', title='py-unique-game-name')

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

@app.route('/language/<string:lang>', methods=['GET'])
def translation(lang):
    resp = make_response(send_from_directory('i18n', lang + '.json'))
    
    if lang in app.config['LANGUAGES']:
        resp.set_cookie('language', lang)

    return resp

@app.route('/languages', methods=['GET'])
def languages():
    return app.config['LANGUAGES']


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

# Set up supported languages
def langInit():
    app.config['LANGUAGES'] = dict()
    languages = dict()
    language_list = glob.glob("i18n/*.json")

    for lang in language_list:

        filename = lang.split('/')
        lang_code = filename[1].split('.')[0]

        with open(lang, 'r', encoding='utf8') as file:
            languages[lang_code] = json.loads(file.read())
            app.config['LANGUAGES'].update({lang_code: {'name': languages[lang_code]['']['name']}})

langInit()

if __name__ == "__main__":
    app.run(host='0.0.0.0')