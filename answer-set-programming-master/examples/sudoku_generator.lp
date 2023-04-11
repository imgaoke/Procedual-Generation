% This program is more difficult than the rest. You're welcome and encouraged to read it, but it isn't mandatory

% Inspired by https://asparagus.cs.uni-potsdam.de/encoding/show/id/12739 (very few comments)

% It involves forms of meta-programming in ASP
% This program generates initial clues for given puzzle
% Outside of the program, solution/3 will be loaded, giving solution to the sudoku

column(0..8).
row(0..8).
cell_value(1..9).

cell(Row, Col) :- row(Row), column(Col).
square(Row, Col, (Row / 3) * 3 + Col / 3) :- cell(Row, Col).
square(N) :- square(_, _, N).
same_coords(X, Y, X, Y) :- cell(X, Y).

% Generate some initial clues for the puzzle
{ initial(Row, Col, N) } 1 :- solution(Row, Col, N).

% Both version of shortcuts as positive, so we don't have to always specify cell / row / column
starts_empty(Row, Col) :- not initial(Row, Col, _), cell(Row, Col).
starts_nonempty(Row, Col) :- initial(Row, Col, _).

% ==========================
%  Part 1 - Unique solution
% ==========================

% 1. Generate a counter-example candidate
counterexample(Row, Col, Val) : cell_value(Val) :- starts_empty(Row, Col).
counterexample(Row, Col, Val) :- initial(Row, Col, Val).

% 2. If counter-example isn't valid, generate bot
%   a) check that the counter-example is different from main solution
same(Row, Col) :- solution(Row, Col, N), counterexample(Row, Col, N).
bot :- same(Row, Col) : cell(Row, Col).

%   b) check that the counter-example still fullfills sudoku constraints
bot :- 2 { counterexample(Row, Col, N) : row(Row) }, column(Col), cell_value(N).
bot :- 2 { counterexample(Row, Col, N) : column(Col) }, row(Row), cell_value(N).
bot :- 2 { counterexample(Row, Col, N) : square(Row, Col, S) }, square(S), cell_value(N).

% 3. If bot is generated, fill the whole counterexample space
counterexample(Row, Col, Val) :- bot, row(Row), column(Col), cell_value(Val).

% 4. Add a constraint that bot must be generated
:- not bot.

% ================================
%  Part 2 - Minimal initial clues
% ================================

% The global idea is this:
% Each initial clue in a minimal sudoku can be changed, s.t. the result is a valid sudoku.
% (Trivial proof by contradiction).

% Thus we, will construct all these alternate solutions, with changing the clue at MRow, MCol
% in a predicate alt_solution(MRow, MCol, Row, Col, N)

{ alt_solution(MRow, MCol, Row, Col, N) : cell_value(N) } = 1 :- starts_nonempty(MRow, MCol), cell(Row, Col).
% Alternate solution must be different at coordinates MRow, MCol
:- initial(MRow, MCol, N), alt_solution(MRow, MCol, MRow, MCol, N).

% Other clues still must be valid
:- starts_nonempty(MRow, MCol), initial(Row, Col, N), not same_coords(Row, Col, MRow, MCol), not alt_solution(MRow, MCol, Row, Col, N).

% All alternate solution must still follow rules of sudoku (exactly 1 of each N in row, column and square)
{ alt_solution(MRow, MCol, Row, Col, N) : column(Col) } = 1
    :- starts_nonempty(MRow, MCol), row(Row), cell_value(N).
{ alt_solution(MRow, MCol, Row, Col, N) : row(Row) } = 1
    :- starts_nonempty(MRow, MCol), column(Col), cell_value(N).
{ alt_solution(MRow, MCol, Row, Col, N) : square(Row, Col, S) } = 1
    :- starts_nonempty(MRow, MCol), square(S), cell_value(N).
