import json
import socket
from _thread import *
import sys
import time

server = "192.168.56.1" # My testing address
port = 5555

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

try:
    s.bind((server, port))
    
except socket.error as e:
    print(e)
    
queue = []
s.listen(7) # Number of player (normally)
print("Waiting for a connection, server started")


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
                
                # Cas 1: Connexion initiale du joueur
                if "username" in message and "password" in message:
                    player_data = json.loads(data.decode("utf-8"))
                    player_data["conn"] = conn
                    queue.append(player_data)
                    print(f"Joueur reçu : {player_data}")
                    response = f"Joueur {player_data['username']} enregistré avec {player_data['balance']} €"  
                    conn.sendall(response.encode('utf-8'))
                
                # Cas 2: Réception d'une main
                elif "cards" in message:
                    print("\n=== MAIN REÇUE ===")
                    print(f"Joueur : {message.get('username', 'inconnu')}")
                    print("Cartes :")
                    for card in message['cards']:
                        print(f"  - {card['rank']} de {card['suit']}")
                    print("==================\n")
                    conn.sendall(b"Main recue")
                
                #Autres
                else:
                    print(f"Action reçue : {message}")
                    response = process_game_action(message.get("action", ""))
                    conn.sendall(response.encode("utf-8"))

            except json.JSONDecodeError:
                print(f"Message non-JSON : {raw_message}")
                conn.sendall(b"Message non valide")

        except Exception as e:
            print(f"Erreur : {str(e)}")
            break
  
        
def matchmaking():
    start_time = time.time()
    while True:
        if len(queue) >= 7 or (time.time() - start_time > 90 and len(queue) > 0):
            game = queue[0:7]
            queue[:] = queue[7:]
            for player in game:
                try:
                    player["conn"].sendall(str.encode("start_game\n"))  
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