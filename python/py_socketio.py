import socketio
from flask import Flask
from src.nltk_command import nltk_command
import eventlet
from flask_cors import CORS
from src.py_mysql import levelPie

sio = socketio.Server(cors_allowed_origins="*")
app = Flask(__name__)
CORS(app, resources={r"/my-page/*": {"origins": "*"}})
app.wsgi_app = socketio.WSGIApp(sio, app.wsgi_app)

@app.route('/my-page/level/image/<username>', methods=['GET'])
def level_pie_image(username):
    return levelPie(username)

@sio.event
def connect(sid, environ):
    print(f"Client connected: {sid}")

@sio.event
def my_message(sid, data):
    command = data.get("command")
    word = data.get("word")
    if command=="lemmatizer" :
        pos = data.get("pos")
        response = process_command(command, word, pos)
    else :
        response = process_command(command, word)
    sio.emit('reply', {"option" : command, "response": response}, room=sid)


@sio.event
def disconnect(sid):
    print(f"Client disconnected: {sid}")

if __name__ == '__main__':
    eventlet.wsgi.server(eventlet.listen(('0.0.0.0', 5000)), app)