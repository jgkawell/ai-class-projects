# ai-class-projects
My programming assignments for CS470 Artificial Intelligence.

## Descriptions of each program:

### Maze Runner
This program finds the shortest solution to escape from a maze using a recursive algorithm.

Per the user's choice, it can display the maze and the solution progress using either a text-based output or a graphical output.

It utilizes depth-first search with a branch-and-cut limit that kills any path longer than the shortest known solution.

### Shape Finder
A program that takes in grey-scale images with shapes overlaid on top and finds and labels the squares, rectangles, and circles in the image.

The images must be converted from .bmp files into files with no extension by using the ConvertBMP.exe file.

The files basic, given, and simple are example images to demo the program with.

### Tile Puzzle Solver
Solves the 15 Puzzle (https://en.wikipedia.org/wiki/15_puzzle) for variable board sizes.

The program can be given a start state or can generate it's own shuffled start state.

It then finds theshortest solution to the puzzle.

It can be adjusted by the user to use different search parameters.
