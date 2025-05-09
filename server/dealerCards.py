import random

def create_deck():
    ranks = ['TWO', 'THREE', 'FOUR', 'FIVE', 'SIX', 'SEVEN',
             'EIGHT', 'NINE', 'TEN', 'JACK', 'QUEEN', 'KING', 'ACE']
    suits = ['HEARTS', 'DIAMONDS', 'CLUBS', 'SPADES']
    
    deck = [{'rank': rank, 'suit': suit} for rank in ranks for suit in suits]
    return deck

def draw_cards(deck, count=2):
    return random.sample(deck, count)