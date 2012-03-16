A Competitive AI for the 'Game of the Amazons'
==============================================

I started on this project for an Artificial Intelligence class I took in University.
If you don't know what the Game of the Amazons is, you can check it out here: http://en.wikipedia.org/wiki/Game_of_the_amazons
It turned out to be a really fun project, we ended up with a few unique features and winning first place to boot.

Features
--------

It's been a while between last looking at the project and putting it up on GitHub, but here is what I remember!

Algorithmic Details:

* Concurrent MinMax search with Alpha-Beta pruning for non-closed games.
* Concurrent State Space Search for games that become closed (player and opponent's pieces can not interact with one another).
* Action based searches, as opposed to state base searches.
** Instead of generating a game tree of successive boards, compact actions are generated instead (an action is that of moving a queen to a square and firing an arrow).
** Thus, states are stored as an initial board and action chain.
** This method greatly reduces the required resources to search a game tree.
** The MinMax and State Space searches operate by applying and undoing actions.
** Accordingly, the Successor Function produces all possible player actions for a given state, as opposed to all possible end states.
* The Evaluation Function is divided into two stages: early game and end game.
** Early game algorithm evaluates a Queen's 'limited mobility' for one and two-hop jumps.
** Early game algorithm also weights a Queen's escape routes.
** Late game is based on a Territory-Mobility (TM) algorithm.
** Territory (how close one's piece is to a square) is weighed against Mobility (how many moves it takes for one's piece to get to a square).
** Of course, blocked Queens are also taken into consideration.
* Cutoff test is a simple memory and time check - moves must be completed under 30 seconds.

Other Fun Facts:

* Impressive interactive GUI!
* Customizable runtime configurations.
* Networked HvH, AIvAI, and AIvH action.

One Problem
-----------

Because this project was developed for the sole purpose of the course, it doesn't have support for offline, Human vs AI games.
For the competition, everything was routed through a SmartFox server hosted somewhere in my Professors office.
Chances are, that server is long since dead, meaning this project is best used as a code sample or to inspire future projects.
I have plans in the future to whip out a build that enables you to play local games against the AI.
