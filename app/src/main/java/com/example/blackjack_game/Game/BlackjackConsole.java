package com.example.blackjack_game.Game;

import org.json.JSONException;

import java.util.Scanner;

public class BlackjackConsole {
    private static Scanner scanner = new Scanner(System.in);
    private static GameLogic game;

    public static void main(String[] args) {
        start_game();
    }

    public static void start_game() {
        System.out.println("Salut, viens joeur au Blackjack aussi appelé 21!");
        System.out.print("Entre ton nom (get name auto): "); /* le faire automatiquement */
        String name = scanner.nextLine();
        
        double initialBalance = 1000.0;
        game = new GameLogic(name, initialBalance);

        boolean playing = true;  /* bocule du jeu */
        while (playing) {
            playRound();
            System.out.print("\nRejouer une partie? (y/n): ");
            String choice = scanner.nextLine().toLowerCase();
            playing = choice.equals("y");
        }

        System.out.println("\nMerci d'avoir joué! Solde final: $" + game.getPlayer().getBalance());
    }

    private static void playRound() {
        System.out.println("\nSolde actuel: $" + game.getPlayer().getBalance());
        System.out.print("Mise en jeu: $");
        double bet = Double.parseDouble(scanner.nextLine());

        try {
            game.startNewRound(bet);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        displayGameState(false);

        if (game.getPlayer().getHand().isBlackjack()) {
            System.out.println("Blackjack! Tu as gangé 1.5x ta mise.");
            game.determineWinner();
            displayFinalResult();
            return;
        }

        while (!game.getPlayer().getHand().isBusted()) {
            System.out.println("\nChoisi une action:");
            System.out.println("1. Hit");
            System.out.println("2. Stand");
            
            if (game.getPlayer().canDoubleDown()) {
                System.out.println("3. Double Down");
            }
            
            if (game.getPlayer().canSplit()) {
                System.out.println("4. Split");
            }

            System.out.print("Entrer votre choix: ");
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    game.playerHit();
                    break;
                case 2:
                    game.playerStand();
                    break;
                case 3:
                    if (game.getPlayer().canDoubleDown()) {
                        game.playerDoubleDown();
                    } else {
                        System.out.println("Double down impossible pour le moment.");
                        continue;
                    }
                    break;
                case 4:
                    if (game.getPlayer().canSplit()) {
                        System.out.println("Split not implemented in this version."); /* a faire j'aii pas compris */
                        continue;
                    } else {
                        System.out.println("Split impossible pour le moment..");
                        continue;
                    }
                default:
                    System.out.println("Choix incorrect. Réessayer");
                    continue;
            }

            displayGameState(false);

            if (game.isRoundOver()) {
                break;
            }
        }

        game.determineWinner();
        displayFinalResult();
    }

    private static void displayGameState(boolean showDealerHand) {
        System.out.println("\nMain du croupier:");
        if (showDealerHand) {
            System.out.println(game.getDealer());
        } else {
            System.out.println(game.getDealer().getPartialHand());
        }

        System.out.println("\nVotre main:");
        System.out.println(game.getPlayer().getHand());
        System.out.println("Current bet: $" + game.getPlayer().getCurrentBet());
    }

    private static void displayFinalResult() {
        System.out.println("\nMain finale:");
        displayGameState(true);

        int playerScore = game.getPlayer().getHand().calculateScore();
        int dealerScore = game.getDealer().calculateScore();

        System.out.println("\nResultat:");
        if (game.getPlayer().getHand().isBlackjack()) {
            System.out.println("Blackjack! Tu as gagné !");
        } else if (game.getPlayer().getHand().isBusted()) {
            System.out.println("Vous avez perdu ! Le croupier gagne.");
        } else if (game.getDealer().isBusted()) {
            System.out.println("Le croupier a perdu ! Vous gagnez !");
        } else if (playerScore > dealerScore) {
            System.out.println("Tu as gagné !");
        } else if (playerScore == dealerScore) {
            System.out.println("Push (égalité). Mise est remboursée.");
        } else {
            System.out.println("\"Le croupier a gagné.");
        }

        System.out.println("Nouveau solde: $" + game.getPlayer().getBalance());
    }
}