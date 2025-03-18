import socket
from _thread import *
import sys

server = "192.168.56.1" # My testing address
port = 5555

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

try:
    s.bind((server, port))
    
except socket.error as e:
    print(e)
    
s.listen(7) # Number of player (normally)
print("Waiting for a connection, server started")



def threaded_client(conn):
    reply = ""
    while True:
        try:
            data = conn.recv(2048)
            reply = data.decode("utf-8")
            
            if not data:
                print("Disconnected")
                break
            else:
                print("Recived: ", reply)
                print("Sending: ", reply)
                
            conn.sendall(str.encode(reply))
        except:
            break
        

while True:
    conn, addr = s.accept()
    print("Connected : ", addr)
    
    start_new_thread(threaded_client, (conn,))