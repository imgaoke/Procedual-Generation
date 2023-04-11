% ASP Program that solves the graph coloring problem
% It uses just a small graph and three colors

% Generate
% --------

vertex(0..3).
color(r;g;b).
% edge(0,1). edge(1,2). edge(2,0).
edge(0,1). edge(1,2). edge(2,3). edge(3,0).
% edge(2,0). edge(1,3).
% Generate exactly one color for each vertex
1 { colored(V, C) : color(C) } 1 :- vertex(V).

% Test
% ----

% Two vertices connected by an edge may not have the same color
:- edge(X, Y), colored(X, C), colored(Y, C).

#show colored/2.