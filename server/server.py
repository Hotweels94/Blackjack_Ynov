import json
import socket
from _thread import *
import sys
import time
from dealerCards import *

server = "10.0.0.19" # My testing address
port = 5555

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

try:
    s.bind((server, port))
    
except socket.error as e:
    print(e)
    
queue = []
s.listen(7) # Number of player (normally)
print("Waiting for a connection, server started")
games = []


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
                    
                elif message.get("type") == "request_dealer_hand": 
                    if games:
                        dealer_hand_data = {
                            "type": "dealer_hand",
                            "cards": games[0]["dealer_hand"]
                        }
                        conn.sendall((json.dumps(dealer_hand_data) + "\n").encode("utf-8"))
                        print("ENVOI FAIT")

                
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
            game = queue[0:7]
            queue[:] = queue[7:]

            deck = create_deck()
            hand = draw_cards(deck, 2)

            # Stocker la game dans games
            games.append({
                "players": game,
                "dealer_hand": hand
            })

            dealer_hand_data = {
                "type": "dealer_hand",
                "cards": hand
            }
            handJson = json.dumps(dealer_hand_data)

            for player in game:
                try:
                    player["conn"].sendall(str.encode("start_game\n"))
                    player["conn"].sendall((handJson + "\n").encode("utf-8"))
                except:
                    pass
            start_time = time.time()

def process_game_action(action):
    #logique actions du jeu
    # et renvoyer l'état mis à jour
    return "action_processed:" + action

start_new_thread(matchmaking, ())
while True:
    conn, addr = s.accept()
    print("Connected : ", addr)
    
    start_new_thread(threaded_client, (conn,))