package combat_game;

import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.util.Random;

public class Combat_game {

    // GLobal variable initializations
    static int roundNumber = 1;
    
    static int player1CharacterType = 0;
    static int player2CharacterType = 0;
    
    static boolean doubleDamageItemPlayer1 = false;
    static boolean doubleDamageItemPlayer2 = false;
    static boolean randomTypeChangePlayer1 = false;
    static boolean randomTypeChangePlayer2 = false;
    static int remainingItemsPlayer1 = 3;
    static int remainingItemsPlayer2 = 3;
    static boolean skipCoinFlipPlayer1 = false;
    static boolean skipCoinFlipPlayer2 = false;
    
    // Main method
    public static void main(String[] args) {
        
        Scanner input = new Scanner(System.in);
        Random random = new Random();

        System.out.println("Welcome to the Turn-Based Battle Simulator!");

        ArrayList<Integer> healthStatus = new ArrayList<>();
        healthStatus.add(100); // player1Health
        healthStatus.add(100); // player2Health
        
        //Initialization for character's type
        
        int player1Choice = 0;
        int player2Choice = 0;

        //Choosing character's type
            
        System.out.println("Prompting player to choose type.");
        player1Choice = chooseCharacterType( "Player 1");
        player2Choice = chooseCharacterType( "Player 2");
            
        player1CharacterType = player1Choice;
        player2CharacterType = player2Choice;

        System.out.println("Player 1 chose: " + getCharacterType(player1Choice));
        System.out.println("Player 2 chose: " + getCharacterType(player2Choice));

        // Gameplay Loop
        
        while (healthStatus.get(0) > 0 && healthStatus.get(1) > 0) {
            
            //Increase round number every turn.
            
            System.out.println("\n--- Round " + roundNumber + " ---");
            roundNumber++;

            // Item : Skip Coin Flip
            
            boolean player1First ;
            if (skipCoinFlipPlayer1) {
                System.out.println("Coin flip has been skipped. Player 1 goes first!");
                player1First = true;
                skipCoinFlipPlayer1 = false;
            } 
            else if (skipCoinFlipPlayer2) {
                System.out.println("Coin flip has been skipped. Player 2 goes first!");
                player1First = false;
                skipCoinFlipPlayer2 = false;
            } 
            
            // Regular Game Loop
            
            else {
                System.out.println("\nCoin flip to decide who acts first. Player 1, choose heads or tails (h/t):");
                char player1Call = input.next().toLowerCase().charAt(0);
                char coinResult = random.nextBoolean() ? 'h' : 't';
                System.out.println("Coin flip result: " + (coinResult == 'h' ? "Heads" : "Tails"));
                player1First = (player1Call == coinResult);
                System.out.println(player1First ? "Player 1 goes first!" : "Player 2 goes first!");
            }

            if (player1First) {
                if (takeTurn(input, random, healthStatus, player1Choice, player2Choice, 1) ) 
                    break;
                if (takeTurn(input, random, healthStatus, player1Choice, player2Choice, 2) ) 
                    break;
            } else {
                if (takeTurn(input, random, healthStatus, player1Choice, player2Choice, 2) ) 
                    break;
                if (takeTurn(input, random, healthStatus, player1Choice, player2Choice, 1) ) 
                    break;
            }

            // Save game state after each round
            try  {
                File file = new File("gameState.txt");
                PrintStream ps = new PrintStream(file);
                ps.println("Round: " + roundNumber + "\n");
                ps.println("Player 1 Health: " + healthStatus.get(0) + "\n");
                ps.println("Player 2 Health: " + healthStatus.get(1) + "\n");
                ps.println("Player 1 Character Type: " + getCharacterType(player1CharacterType) + "\n");
                ps.println("Player 2 Character Type: " + getCharacterType(player2CharacterType) + "\n");
                
            } catch (IOException e) {
                System.out.println("Error saving game state.");
            }
        }

        input.close();
    }
        // Method for Choosing Character's Type
    
    private static int chooseCharacterType(String player) {
    Scanner input = new Scanner(System.in);
    int choice = 0;
    
    while (true) {
        System.out.println(player + ", choose your character type: (1) Fire (2) Water (3) Grass (4) Normal");
        
        try {
            choice = input.nextInt();
            if (choice >= 1 && choice <= 4) {
                break; // Valid input, exit loop
            } else {
                System.out.println("Invalid choice. Please enter a number between 1 and 4.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter a number between 1 and 4.");
            input.next(); // Discard invalid input
        }
    }
    
    return choice;
}
    // Method for alternating between turns of each user.

    private static boolean takeTurn(Scanner input, Random random, ArrayList<Integer> healthStatus, int player1Choice, int player2Choice, int playerTurn) {
        String playerName = (playerTurn == 1) ? "Player 1" : "Player 2";

        // Check if random type change is active and notify the player
        if (playerTurn == 1 && randomTypeChangePlayer1) {
            
            player1CharacterType = getRandomTypeExcludingCurrent(random, player1CharacterType);
            System.out.println(playerName + "'s character type changed to: " + getCharacterType(player1CharacterType));
            randomTypeChangePlayer1 = false;
            
        } else if (playerTurn == 2 && randomTypeChangePlayer2) {
            
            player2CharacterType = getRandomTypeExcludingCurrent(random, player2CharacterType);
            System.out.println(playerName + "'s character type changed to: " + getCharacterType(player2CharacterType));
            randomTypeChangePlayer2 = false;
        }

        System.out.println("\n" + playerName + "'s Turn! Choose an action: (1) Run (2) Fight (3) Use Item");

        int action = input.nextInt();

        if (action == 1) {
            System.out.println(playerName + " chose to run. Game over.");
            System.exit(0);
            
            
        } else if (action == 2) {
            
            int attackerChoice = (playerTurn == 1) ? player1CharacterType : player2CharacterType;
            System.out.println("Choose your move: (1) " + getMoveName(attackerChoice, 1) + " (2) " + getMoveName(attackerChoice, 2) + " (3) " + getMoveName(attackerChoice, 3));
            int move = input.nextInt();

            int defenderChoice = (playerTurn == 1) ? player2CharacterType : player1CharacterType;

            int damage = calculateDamage(attackerChoice, move, defenderChoice);
            
            if ((playerTurn == 1 && doubleDamageItemPlayer1) || (playerTurn == 2 && doubleDamageItemPlayer2)) {
                
                int originalDamage = damage;
                damage *= 2;
                System.out.println(playerName + " deals double damage! Original damage: " + originalDamage + ", Double damage: " + damage);
                
                if (playerTurn == 1)
                    doubleDamageItemPlayer1 = false;
                else
                    doubleDamageItemPlayer2 = false;
            }
            
            int defenderHealthIndex = (playerTurn == 1) ? 1 : 0;
            healthStatus.set(defenderHealthIndex, healthStatus.get(defenderHealthIndex) - damage);
            System.out.println(playerName + " used " + getMoveName(attackerChoice, move) + " move and dealt " + damage + " damage.");

            if (healthStatus.get(defenderHealthIndex) <= 0) {
                System.out.println(playerName + " wins!");
                return true;
            }
            
            
        } else if (action == 3) {
            System.out.println("Choose an item: (1) Healing Item (2) Double Damage Item (3) Skip Coin Flip Item (4) Random Type Change Item");
            int itemChoice = input.nextInt();
            
            if (remainingItemsPlayer1 > 0 || remainingItemsPlayer2 > 0) {
                if (itemChoice == 1) {
                    
                    System.out.println(playerName + " used a healing item to heal 20 health.");
                    int playerHealthIndex = (playerTurn == 1) ? 0 : 1;
                    healthStatus.set(playerHealthIndex, healthStatus.get(playerHealthIndex) + 20);
                    
                } else if (itemChoice == 2) {
                    if (playerTurn == 1) 
                        doubleDamageItemPlayer1 = true;
                     else 
                        doubleDamageItemPlayer2 = true;
                    
                    System.out.println(playerName + " will deal double damage on the next move.");
                    
                } else if (itemChoice == 3) {
                    
                    if (playerTurn == 1) 
                        skipCoinFlipPlayer1 = true;
                     else 
                        skipCoinFlipPlayer2 = true;
                    
                    System.out.println(playerName + " will skip the coin flip on the next turn and act first.");
                    
                } else if (itemChoice == 4) {
                    
                    if (playerTurn == 1) {
                        randomTypeChangePlayer1 = true;
                        System.out.println(playerName + " will change character type in the next round.");
                    } else {
                        randomTypeChangePlayer2 = true;
                        System.out.println(playerName + " will change character type in the next round.");
                    }
                }

                // Decrease remaining item count
                if (playerTurn == 1) {
                    remainingItemsPlayer1--;
                } else {
                    remainingItemsPlayer2--;
                }

                // Display remaining item count
                System.out.println("Player 1 items remaining: " + remainingItemsPlayer1);
                System.out.println("Player 2 items remaining: " + remainingItemsPlayer2);
                
            } else 
                System.out.println(playerName + " has no items.");
            
        }

        // Display health status and item count
        
        System.out.println("Player 1 health: " + healthStatus.get(0));
        System.out.println("Player 2 health: " + healthStatus.get(1));

        return false;
    }
    
    // Method for calculating Damage

    public static int calculateDamage(int attackerType, int moveType, int defenderType) {
        int baseDamage;
        switch (moveType) {
            case 1:
                baseDamage = 10; // Move1
                break;
            case 2:
                baseDamage = 15; // Move2
                break;
            case 3:
                baseDamage = 20; // Move3
                break;
            default:
                baseDamage = 0;
                break;
        }

        double effectiveness = 1.0;
        
        if ((attackerType == 1 && defenderType == 3) || // Fire > Grass
                (attackerType == 2 && defenderType == 1) || // Water > Fire
                (attackerType == 3 && defenderType == 2)) { // Grass > Water
            
            effectiveness = 2.0;
            
        } else if ((attackerType == 1 && defenderType == 2) || // Fire < Water
                (attackerType == 2 && defenderType == 3) || // Water < Grass
                (attackerType == 3 && defenderType == 1)) { // Grass < Fire
            
            effectiveness = 0.5;
        }

        return (int) (baseDamage * effectiveness);
    }
    
    //Method for obtaining Character's Type

    static String getCharacterType(int choice) {
        switch (choice) {
            case 1:
                return "Fire";
            case 2:
                return "Water";
            case 3:
                return "Grass";
            case 4:
                return "Normal";
            default:
                return "Unknown";
        }
    }
    
        // Method for obtaining the attack or move.
    
    public static String getMoveName(int characterType, int move) {
        switch (characterType) {
            case 1: // Fire
                switch (move) {
                    case 1:
                        return "Flamethrower";
                    case 2:
                        return "Fire Blast";
                    case 3:
                        return "Ember";
                }
            case 2: // Water
                switch (move) {
                    case 1:
                        return "Water Gun";
                    case 2:
                        return "Surf";
                    case 3:
                        return "Bubble";
                }
            case 3: // Grass
                switch (move) {
                    case 1:
                        return "Razor Leaf";
                    case 2:
                        return "Vine Whip";
                    case 3:
                        return "Solar Beam";
                }
            case 4: // Normal
                switch (move) {
                    case 1:
                        return "Tackle";
                    case 2:
                        return "Scratch";
                    case 3:
                        return "Bite";
                }
            default:
                return "Unknown Move";
        }
    }
    
    //Item : Random Type

    public static int getRandomTypeExcludingCurrent(Random random, int currentType) {
            ArrayList<Integer> types = new ArrayList<>();
            types.add(1);
            types.add(2);
            types.add(3);
            types.add(4);
            types.remove(Integer.valueOf(currentType));

            int randomIndex = random.nextInt(types.size());
            
        return types.get(randomIndex);
    }
}