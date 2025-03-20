import json

class Player:
    def __init__(self, id, username, email, password, balance, status):
        self.id = 0
        self.username = ""
        self.email = ""
        self.password = ""
        self.balance = 0
        self.status = ""
        
    def to_json(self):
        return json.dumps({
            "id": self.id,
            "username": self.username,
            "email": self.email,
            "password": self.password,
            "balance": self.balance,
            "status": self.status
        })
