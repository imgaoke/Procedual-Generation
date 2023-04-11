from simple_clingo import SimpleClingo


def run():
    sudoku = [
        [0, 0, 0,   7, 0, 0,   0, 2, 0],
        [0, 4, 0,   0, 0, 0,   6, 0, 0],
        [0, 8, 1,   0, 0, 0,   0, 0, 0],

        [0, 0, 0,   8, 0, 0,   0, 1, 0],
        [0, 9, 0,   0, 0, 0,   0, 0, 0],
        [3, 0, 0,   5, 0, 6,   0, 0, 0],

        [5, 0, 0,   0, 0, 0,   0, 9, 0],
        [2, 0, 0,   0, 6, 0,   7, 0, 0],
        [0, 0, 0,   0, 9, 0,   0, 8, 0],
    ]
    solutions = solve_sudoku(sudoku, print_time=True)

    if len(solutions) == 0:
        print("No solution exists.")
        return
    if len(solutions) > 1:
        print("More than one solution found - this is an incorrect sudoku puzzle!")
        print("Showing possible solutions:")

    print_sudoku(solutions[0])


def solve_sudoku(sudoku, print_time=False, n_solutions=2):
    clingo = SimpleClingo()
    clingo.load_path('examples/4_sudoku_solver.lp')

    # We need to add all the known information as an initial/3 predicate
    for r, row in enumerate(sudoku):
        for c, value in enumerate(row):
            if value == 0:
                continue
            clingo.add_clause('initial({},{},{}).'.format(r, c, value))

    # Try to solve the sudoku! By default tries to find 2 models, so you can determine,
    # whether the sudoku has a unique solution
    models = clingo.solve(n_solutions, randomness=0.0, verbose=True)

    # Then we have to decode the solution/3 predicate
    solutions = []
    for model in models:
        sudoku_solution = [[0] * 9 for _ in range(9)]

        for row, column, n in model['solution']:
            sudoku_solution[row][column] = n
        solutions.append(sudoku_solution)

    return solutions


def print_sudoku(sudoku):
    print()
    for r, row in enumerate(sudoku):
        row_format = '012|345|678'
        print(''.join(map(lambda i: str(row[int(i)]) if i.isdigit() else i, row_format)))
        if r == 2 or r == 5:
            print('---+---+---')
    print()


if __name__ == '__main__':
    run()
