% ASP maze generator. It requires the following constants to be set (externally)

% size - size of the maze (both X and Y)
% walls_min - minimum of walls in the maze
% walls_max - maximum of walls in the maze
% max_distance = maximum possible length of shortest path goal
% min_distance = minimum possible length of shortest path to goal

% Here is an example setting
% Uncomment if not running from the python launcher.
% #const size=6.
% #const walls_min=1.
% #const walls_max=(size-1)*size*2.
% #const max_distance=size*size-1.
% #const min_distance=size*(size/2).

distance(0..max_distance).

% ====================
%  Defining the space
% ====================

% Describe grid
dim(0..size-1).
cell(X,Y) :- dim(X), dim(Y).

% Definitions of specific adjacency
left (X, Y, X2, Y ) :- dim(X), dim(Y), dim(X2), X = X2-1.
up   (X, Y, X,  Y2) :- dim(X), dim(Y), dim(Y2), Y = Y2-1.
right(X, Y, X2, Y ) :- left(X2, Y, X, Y).
down (X, Y, X,  Y2) :- up  (X, Y2, X, Y).

% Utility function to generalize adjacency
adjacent(X, Y, X2, Y2) :- left (X, Y, X2, Y2).
adjacent(X, Y, X2, Y2) :- right(X, Y, X2, Y2).
adjacent(X, Y, X2, Y2) :- up   (X, Y, X2, Y2).
adjacent(X, Y, X2, Y2) :- down (X, Y, X2, Y2).

% Sometimes it is good not to have symmetrical duplicates
adjacent_unique(X, Y, X2, Y2) :- left(X, Y, X2, Y2).
adjacent_unique(X, Y, X2, Y2) :- up(X, Y, X2, Y2).

% =============================
%  Generate the problem (maze)
% =============================

% There's exactly one start and one end
1 { start(X, Y) : dim(X), dim(Y) } 1.
1 { end(X, Y)   : dim(X), dim(Y) } 1.

% Place some amount of walls.
% Walls (from one side)
walls_min { wall_between(X, Y, X2, Y2) : adjacent_unique(X, Y, X2, Y2) } walls_max.
% Add symmetries of walls
wall_between(X, Y, X2, Y2) :- wall_between(X2, Y2, X, Y).

% Make a flood fill from start
distance_from_start(X, Y, 0) :- start(X, Y).
distance_from_start(X, Y, N+1) :-
    distance_from_start(X2, Y2, N),
    adjacent(X, Y, X2, Y2),
    not wall_between(X, Y, X2, Y2),
    distance(N),
    { distance_from_start(X, Y, N2) : distance(N2), N2 < N } = 0.

% Utility function to determine reachability
reachable(X, Y) :- distance_from_start(X, Y, _).

% Every square should be reachable
:- not reachable(X, Y), dim(X), dim(Y).
% End should not be reachable in less than min_distance steps
:- end(X, Y), distance_from_start(X, Y, N), N < min_distance.
