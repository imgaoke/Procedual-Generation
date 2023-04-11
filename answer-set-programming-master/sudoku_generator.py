from simple_clingo import SimpleClingo
from sudoku_solver import print_sudoku


def run():
    sudoku = generate_sudoku(print_time=True)
    print_sudoku(sudoku)


def generate_sudoku(print_time=False):
    # Generating a sudoku is a difficult task
    # First, we're going to solve an empty sudoku to get a random solved grid
    clingo = SimpleClingo()
    clingo.load_path('examples/4_sudoku_solver.lp')

    # This part is easy, and as such, we can use randomness = 1.0
    # Suppress warning that initial/3 is not defined
    model = clingo.solve(1, verbose=True, suppress_warnings=True)[0]

    # Now we'll set this as the target grid
    clingo.reset()
    for row, column, n in model['solution']:
        clingo.add_clause('solution({},{},{}).'.format(row, column, n))

    # If you want, you can try to limit the number of clues in assignment :)
    # However, what are the min and max clues in minimal sudokus is a hard topic
    # https://cs.stackexchange.com/a/81901

    # clingo.add_clause(':- 22 { initial(_, _, _) }.')
    # clingo.add_clause(':- { initial(_, _, _) } 30.')

    # Let's proceed to the second step - generating clues for given grid
    clingo.load_path('examples/sudoku_generator.lp')
    # This is a hard-model, setting randomness to 0 -> always use heuristics -> much faster solving
    models = clingo.solve(1, randomness=0.0, verbose=True)
    model = models[0]

    if print_time:
        clingo.print_solving_time()

    sudoku = [[0] * 9 for _ in range(9)]
    for row, column, n in model['initial/3']:
        sudoku[row][column] = n
    return sudoku


if __name__ == '__main__':
    run()
