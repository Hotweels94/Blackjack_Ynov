# Blackjack online for mobile
## Installation

First, you will need to download this repository or clone it on your computer.

## How to play

To play our game, you will need to have :
- Android Studio
- A terminal
- Python
- Optional : A mobile phone

After this, in your terminal, in the folder of the repository, you will need to write this command to start the server : `python Blackjack_Ynov/server/server.py`.
Now, the server is running. In your terminal, you will see the ip address use to connect your player to the server.

![IP Address in the terminal](https://github.com/user-attachments/assets/8a61a9ff-e519-40c8-87df-82b82ce7883d)

You need to start the server BEFORE start a game !
In Android Studio, you will start the project and download it on the mobile emulator (or download it on your phone with Android Studio !).
OR you can download the .apk for your phone like a real app !
[DOWNLOAD THE APK HERE](/app/release/app-release.apk)
You will need to connect yourself to the game and write the IP address of the server.
After, you will need to wait 20 seconds to let the time for all players to connect. After the game will start.
Now you can play to our game (alone or with friends !).

## Rules 

Blackjack is a card game played between players and the dealer. The goal is to have a hand value as close to **21** as possible without exceeding it.

### Basic Rules:
- Each player starts with **two cards**.
- The dealer also has two cards.
- Cards from **2 to 10** are worth their face value.
- **J**, **Q**, and **K** are worth **10 points**.
- **Aces (A)** are worth **1 or 11**, depending on what benefits the hand most.

### Gameplay:
1. **Players' Turn**:
   - Each player can choose to:
     - **Hit** (draw a new card),
     - **Stand** (end their turn).
     - **Double** (draw a new card and multiply by 2 their bet).
   - If the total value exceeds **21**, the player **busts** and loses.

2. **Dealer's Turn**:
   - The dealer must **draw cards until reaching at least 17**.
   - If the dealer busts, all remaining players win.

3. **Winning**:
   - If your hand is **closer to 21 than the dealer’s**, you win.
   - If your hand equals **21 with the first two cards**, it’s a **Blackjack**.
   - If both you and the dealer have the same score, it’s a **draw** (*push*).
  
## Screenshots of the game

Connection Page : 

![Connection Page](https://github.com/user-attachments/assets/edec5a91-7415-4f19-a7f3-69030d60b94a)

Game UI :

![Game UI](https://github.com/user-attachments/assets/69bbfcaf-f47b-4f9f-9ea7-1ce7bb2f5ecc)
