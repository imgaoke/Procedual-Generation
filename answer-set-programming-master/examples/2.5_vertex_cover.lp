% ASP program that finds a hamiltonian cycle in a graph

% Generate
% --------

{ vertex_in_cover(U) } :- vertex(U).

% Define
% ------

edge_covered(X, Y) :- edge(X, Y), vertex_in_cover(X).
edge_covered(X, Y) :- edge(X, Y), vertex_in_cover(Y).

% Test
% -----
:- not edge_covered(X, Y), edge(X, Y).


% Instance
% ----------
vertex(0..5).
edge(0,1). edge(1,2). edge(2,3). edge(2,4). edge(3,4). edge(4,0). edge(4,5). edge(5,0).

#show vertex_in_cover/1.
