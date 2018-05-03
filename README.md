# AI Class Projects
My programming assignments for CS470 Artificial Intelligence during the spring of 2018.

## Descriptions of each program:

### Maze Runner
#### Sonar Cloud: https://sonarcloud.io/dashboard?id=com.jgkawell%3Amaze-runner
This program finds the shortest solution to escape from a maze using a recursive algorithm. Per the user's choice, it can display the maze and the solution progress using either a text-based output or a graphical output. It utilizes depth-first search with a branch-and-cut limit that kills any path longer than the shortest known solution.

### Othello Player
#### Sonar Cloud: https://sonarcloud.io/dashboard?id=com.jgkawell%3Aothello-player
This program plays the game of Othello (Reversi) using a depth-first + iterative deepening technique that utilizes a heuristic running a mini-max anaylsis paired with alpha-beta cutoffs. The hueristic is based on weighting different positions on the board (i.e. corners are best, edges are good, and one space from an edge is bad, etc.). The game can be run with another program built on the same shell program found in the OthelloShell class.

### Shape Finder
#### Sonar Cloud: https://sonarcloud.io/dashboard?id=com.jgkawell%3Ashape-finder
A program that takes in grey-scale images with shapes overlaid on top and finds and labels the squares, rectangles, and circles in the image. The images must be converted from .bmp files into files with no extension by using the ConvertBMP.exe file. The files basic, given, and simple are example images to demo the program with.

### Tile Puzzle Solver
#### Sonar Cloud: https://sonarcloud.io/dashboard?id=com.jgkawell%3Atile-puzzle-solver
Solves the 15 Puzzle (https://en.wikipedia.org/wiki/15_puzzle) for variable board sizes. The program can be given a start state or can generate it's own shuffled start state. It then finds the shortest solution to the puzzle. It can be adjusted by the user to use different search parameters.
