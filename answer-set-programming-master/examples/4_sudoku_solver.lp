% ASP program for solving sudoku puzzles.
% Sudoku instance has to be inserted externally by the initial(Row, Column, N) predicate

% Generate
% --------

% Proper ranges for rows, columns and cells
row(0..8).
column(0..8).
cell_value(1..9).

% The initial standing will be given by initial/3 (outside of this program)
starts_empty(Row, Col) :- row(Row), column(Col), not initial(Row, Col, _).

% Final sudoku value is equal to initial
solution(Row, Col, Val) :- initial(Row, Col, Val).

% Every other sudoku cell must be filled by algorithm (exactly one value)
1 { solution(Row, Col, Val) : cell_value(Val) } 1 :- starts_empty(Row, Col).

% Define
% ------

% Square index computation (and truncated function for enumerating all squares)
square(Row, Col, Square) :- row(Row), column(Col), Square = (Row / 3) * 3 + Col / 3.
square(Square) :- square(_, _, Square).

% Test
% ----

% In order, forbidding same values on rows, columns and squares
1 { solution(Row, Col, Val) : row(Row) } 1 :- column(Col), cell_value(Val).
1 { solution(Row, Col, Val) : column(Col) } 1 :- row(Row), cell_value(Val).
1 { solution(Row, Col, Val) : square(Row, Col, Square) } 1 :- square(Square), cell_value(Val).

% The actual instance of sudoku will be needed.

