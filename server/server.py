import json
import socket
from _thread import *
import sys
import time
from threading import Lock
from dealerCards import *

hostname = socket.gethostname()
server = socket.gethostbyname(hostname)
print("IP : " + server)
port = 5555

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

try:
    s.bind((server, port))
    
except socket.error as e:
    print(e)
    
queue = []
match_timer_started = False
queue_lock = Lock()
s.listen(7) # Number of player
print("Waiting for a connection, server started")
games = []

def start_new_game(players):
    deck = create_deck()
    dealer_hand = draw_cards(deck, 2)

    game = {
        "players": players,
        "dealer_hand": dealer_hand,
        "deck": deck
    }
    games.append(game)

    dealer_hand_data = {
        "type": "dealer_hand",
        "cards": dealer_hand
    }
    hand_json = json.dumps(dealer_hand_data)

    for player in players:
        try:
            message = f"start_game\n{hand_json}\n"
            player["conn"].sendall(message.encode("utf-8"))
        except Exception as e:
            print(f"Erreur d'envoi à {player.get('username', 'inconnu')} : {e}")

    print("✅ Nouvelle partie lancée avec les joueurs :", [p["username"] for p in players])
    print("🃏 Main du croupier :", dealer_hand)

def threaded_client(conn):
    while True:
        try:
            data = conn.recv(4096)
            if not data:
                print("not data")
                break

            raw_message = data.decode('utf-8')
            print(f"Message brut reçu : {raw_message}")

            try:
                message = json.loads(raw_message)
                
                #1 Connection of the user
                if "username" in message and "password" in message:
                    player_data = json.loads(data.decode("utf-8"))
                    player_data["conn"] = conn
                    queue.append(player_data)
                    response = f"Joueur {player_data['username']} enregistré avec {player_data['balance']} €"  
                    conn.sendall(response.encode('utf-8'))
                
                #2 Cards
                elif "cards" in message and "joueur":
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
                    username = message.get("username")
                    print(f"Joueur {username} demande à rejouer")

                    for game in games:
                        for player in game["players"]:  
                            player["conn"] = conn
                            queue.append(player)
                            conn.sendall(b"Ajoute a la queue pour une nouvelle partie")
                            break

                
                else:
                    print(f"Action reçue : {message}")
                    response = process_game_action(message.get("action", ""))
                    conn.sendall(response.encode("utf-8"))

            except json.JSONDecodeError:
                conn.sendall(b"Message non valide")

        except Exception as e:
            print(f"Erreur : {str(e)}")
            break
  
        
def matchmaking():
    start_time = time.time()
    while True:
        if len(queue) >= 7 or (time.time() - start_time > 10 and len(queue) > 0):
            players_for_game = queue[0:7]
            queue[:] = queue[7:]
            start_new_game(players_for_game)
            start_time = time.time()
            print("OUAIS OUAIS OUAIS")
        time.sleep(1)

def process_game_action(action):
    #logique actions du jeu
    # et renvoyer l'état mis à jour
    return "action_processed:" + action

start_new_thread(matchmaking, ())
while True:
    conn, addr = s.accept()
    print("Connected : ", addr)
    
    start_new_thread(threaded_client, (conn,))