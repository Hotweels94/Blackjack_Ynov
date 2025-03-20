import json

class Player:
    def __init__(self, id, username, email, password, balance, status):
        self.id = id
        self.username = username
        self.email = email
        self.password = password
        self.balance = balance
        self.status = status
        
    def to_json(self):
        return json.dumps({
            "id": self.id,
            "username": self.username,
            "email": self.email,
            "password": self.password,
            "balance": self.balance,
            "status": self.status
        })
