% ASP program that finds a hamiltonian cycle in a graph

% Generate
% --------

{ ham_edge(U, V) } :- edge(U, V).
{ hampath_edge(U, V) } :- ham_edge(U, V).

% Define
% ------

reachable(X) :- vertex(X), ham_edge(0, X).
reachable(Y) :- reachable(X), ham_edge(X, Y), edge(X, Y), hampath_edge(X, Y).

hampath_reach(U, W) :- hampath_edge(U, V), hampath_edge(V, W).
hampath_reach(U, W) :- hampath_reach(U, V), hampath_edge(V, W).
hampath_reach(U, W) :- hampath_edge(U, V), hampath_reach(V, W).
hampath_reach(U, W) :- hampath_reach(U, V), hampath_reach(V, W).

% Test
% -----

:- 2 {ham_edge(X, Y) : edge(X, Y)}, vertex(X).
:- 2 {ham_edge(X, Y) : edge(X, Y)}, vertex(Y).
:- not reachable(X), vertex(X).
:- hampath_reach(X, X).


% Instance
% ----------
vertex(0..5).
edge(0,1). edge(1,2). edge(2,3). edge(2,4). edge(3,4). edge(4,0). edge(4,5). edge(5,0).

#show ham_edge/2.
