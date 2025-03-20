import json
import socket
from _thread import *
import sys

server = "127.0.0.1" # My testing address
port = 5555

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

try:
    s.bind((server, port))
    
except socket.error as e:
    print(e)
    
s.listen(7) # Number of player (normally)
print("Waiting for a connection, server started")



def threaded_client(conn):
    while True:
        try:
            data = conn.recv(2048)
            
            if not data:
                print("Disconnected")
                break
            player_data = json.loads(data.decode("utf-8"))
            print(f"Joueur reçu : {player_data}")

            # Réponse au client
            response = f"Joueur {player_data['username']} enregistré avec {player_data['balance']} €"
            conn.sendall(response.encode('utf-8'))
            
        except:
            break
        

while True:
    conn, addr = s.accept()
    print("Connected : ", addr)
    
    start_new_thread(threaded_client, (conn,))