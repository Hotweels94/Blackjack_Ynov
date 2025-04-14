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
            data = conn.recv(2048)
            
            if not data:
                print("Disconnected")
                break
            player_data = json.loads(data.decode("utf-8"))
            player_data["conn"] = conn
            queue.append(player_data)
            print(f"Joueur reçu : {player_data}")

            response = f"Joueur {player_data['username']} enregistré avec {player_data['balance']} €"
                
            conn.sendall(response.encode('utf-8'))
            
        except Exception as e:
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

start_new_thread(matchmaking, ())
while True:
    conn, addr = s.accept()
    print("Connected : ", addr)
    
    start_new_thread(threaded_client, (conn,))