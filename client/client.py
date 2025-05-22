import socket
from player import Player

server = "10.0.0.19"
port = 5555

def send_data(data):
    try:
        socket_client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        socket_client.connect((server, port))
        socket_client.sendall(data.encode('utf-8'))
        response = socket_client.recv(2048).decode('utf-8')
        socket_client.close()
        return response
    except socket.error as e:
        print(e)
    finally:
        socket_client.close()

player = Player(1, "Ryan", "ryvida@hotmail.fr", "1234", 1000, "online")
player_json = player.to_json()
print(send_data(player_json))
