# server/server.py
import json
import socket
import sqlite3
import hashlib
from _thread import *
import sys
import time
from threading import Lock
from dealerCards import *
import time


hostname = socket.gethostname()
server = socket.gethostbyname(hostname)
print("IP : " + server)
port = 5555

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

try:
    s.bind((server, port))
except socket.error as e:
    print(e)
    sys.exit(1)

queue = []
match_timer_started = False
queue_lock = Lock()
s.listen(7)  # Number of players
print("Waiting for a connection, server started")
games = []

def init_db():
    conn = sqlite3.connect('users.db')
    c = conn.cursor()
    c.execute('''CREATE TABLE IF NOT EXISTS users
                 (pseudo TEXT PRIMARY KEY, password TEXT, balance INTEGER)''')
    conn.commit()
    conn.close()

def hash_password(password):
    return hashlib.sha256(password.encode()).hexdigest()

def register_user(pseudo, password):
    conn = sqlite3.connect('users.db')
    c = conn.cursor()
    hashed_password = hash_password(password)
    try:
        c.execute("INSERT INTO users (pseudo, password, balance) VALUES (?, ?, ?)", (pseudo, hashed_password, 1000))
        conn.commit()
        return "GOOD"
    except sqlite3.IntegrityError:
        return "Pseudo déjà utilisé"
    finally:
        conn.close()

def login_user(pseudo, password):
    conn = sqlite3.connect('users.db')
    c = conn.cursor()
    c.execute("SELECT password, balance FROM users WHERE pseudo=?", (pseudo,))
    result = c.fetchone()
    conn.close()
    if result and result[0] == hash_password(password):
        return {"status": "Done", "balance": result[1]}
    else:
        return "Pseudo ou mot de passe incorrect"




def start_new_game(players):
    deck = create_deck()
    dealer_hand = draw_cards(deck, 2)

    game = {
        "players": players,
        "dealer_hand": dealer_hand,
        "deck": deck
    }
    games.append(game)

    # Envoyer d'abord un message simple de démarrage
    start_msg = "start_game"
    
    # Puis envoyer les données JSON
    dealer_hand_data = {
        "type": "dealer_hand",
        "cards": dealer_hand
    }
    hand_json = json.dumps(dealer_hand_data)

    for player in players:
        try:
            player["conn"].sendall((start_msg + "\n").encode("utf-8"))
            time.sleep(0.1)  # Petit délai entre les messages
            player["conn"].sendall((hand_json + "\n").encode("utf-8"))
        except Exception as e:
            print(f"Erreur d'envoi à {player.get('pseudo', 'inconnu')} : {e}")

    print("Nouvelle partie lancée avec les joueurs :", [p["pseudo"] for p in players])
    print(" Main du croupier :", dealer_hand)

def threaded_client(conn):
    while True:
        try:
            data = conn.recv(4096)
            if not data:
                print("Aucune donnée reçue")
                break

            raw_message = data.decode('utf-8')
            print(f"Message brut reçu : {raw_message}")

            try:
                print("Coucou")
                message = json.loads(raw_message)

                #1 Connection of the user
                """ if message.get("type") == "login":
                    print("LOGIN LOGIN")
                    player_data = json.loads(data.decode("utf-8"))
                    player_data["conn"] = conn
                    print("PLAYER DATA : ", player_data)
                    queue.append(player_data)
                    response = f"Joueur {player_data['pseudo']} enregistré avec {player_data['balance']} €"
                    print(f"Joueur enregistré : {player_data['pseudo']}")
                    conn.sendall(response.encode('utf-8')) """

                #2 Cards
                # elif "cards" in message and "joueur":
                if "cards" in message and "joueur":
                    print("\n=== MAIN REÇUE JOUEUR ===")
                    print("Cartes :")
                    for card in message['cards']:
                        print(f"  - {card['rank']} de {card['suit']}")
                    print("==================\n")
                    conn.sendall(b"Main recue")

                #3 Dealer hand ask from the server
                elif message.get("type") == "request_dealer_hand":
                    if games:
                        dealer_hand_data = {
                            "type": "dealer_hand",
                            "cards": games[len(games)-1]["dealer_hand"]
                        }
                        conn.sendall((json.dumps(dealer_hand_data) + "\n").encode("utf-8"))
                        print("ENVOI FAIT")
                        print("MAIN CROUPIER 2  : ")
                        print(dealer_hand_data)

                #4 Continue the game
                elif message.get("type") == "continue":
                    pseudo = message.get("pseudo")
                    print(f"Joueur {pseudo} demande à rejouer")

                    for game in games:
                        for player in game["players"]:
                            player["conn"] = conn
                            queue.append(player)
                            conn.sendall(b"Ajoute a la queue pour une nouvelle partie")
                            break

                #5 Register
                elif message.get("type") == "register":
                    pseudo = message.get("pseudo")
                    password = message.get("password")
                    response = register_user(pseudo, password)
                    conn.sendall(response.encode('utf-8'))
                    print(f"Tentative d'inscription pour le pseudo : {pseudo}")
                    print(f"Réponse d'inscription : {response}")

                #6 Login
                elif message.get("type") == "login":
                    pseudo = message.get("pseudo")
                    password = message.get("password")
                    login_result = login_user(pseudo, password)

                    if isinstance(login_result, dict) and login_result.get("status") == "Done":
                        player_data = {
                            "pseudo": pseudo,
                            "balance": login_result["balance"],
                            "conn": conn
                        }
                        queue.append(player_data)
                        response = f"Joueur {pseudo} enregistré avec {login_result['balance']} €"
                    else:
                        response = login_result if isinstance(login_result, str) else "Erreur de connexion"

                    conn.sendall(response.encode('utf-8'))


                else:
                    print(f"Action reçue : {message}")
                    response = process_game_action(message.get("action", ""))
                    conn.sendall(response.encode("utf-8"))

            except json.JSONDecodeError:
                conn.sendall(b"Message non valide")

        except Exception as e:
            print(f"Erreur : {str(e)}")
            break

    conn.close()

def matchmaking():
    start_time = time.time()
    while True:
        if len(queue) >= 7 or (time.time() - start_time > 60 and len(queue) > 0):
            players_for_game = queue[0:7]
            queue[:] = queue[7:]
            start_new_game(players_for_game)
            start_time = time.time()
            print("OUAIS OUAIS OUAIS")
        time.sleep(1)

def process_game_action(action):
    # Logique des actions du jeu
    # et renvoyer l'état mis à jour
    return "action_processed:" + action

init_db()
start_new_thread(matchmaking, ())
while True:
    conn, addr = s.accept()
    print("Nouvelle connexion : ", addr)
    start_new_thread(threaded_client, (conn,))
