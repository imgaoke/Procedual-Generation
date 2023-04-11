from simple_clingo import SimpleClingo


def run():
    size = 6
    maze = Maze(size)
    settings = {
        'size': size,
        'walls_min': 1,
        'walls_max': (size-1) * size * 2,
        'min_distance': 20,
        'max_distance': size*size-1
    }

    clingo = SimpleClingo()
    clingo.load_path('examples/5_maze.lp')
    for name, value in settings.items():
        clingo.add_clause('#const {}={}.'.format(name, value))
    model = clingo.solve(1, verbose=True)[0]

    for wall_between in model['wall_between']:
        maze.set_wall_between(wall_between[0], wall_between[1], wall_between[2], wall_between[3])

    # Uncomment to show the flood fill
    # for distance_from_start in model['distance_from_start']:
    #     maze.set_char_at(distance_from_start[0], distance_from_start[1], str(distance_from_start[2] % 10))

    maze.set_char_at_coords(model['start'][0], 'S')
    maze.set_char_at_coords(model['end'][0], 'E')
    maze.print()


class Maze:
    """
    Simple class for manipulating with the maze as a grid, and printing the maze.
    """
    def __init__(self, size):
        # start with a bomberman-esque grid
        self.pix_size = 2 * size + 1

        self.maze_pix = [[' '] * self.pix_size for _ in range(self.pix_size)]
        for i in range(self.pix_size):
            self.maze_pix[0][i] = '#'
            self.maze_pix[self.pix_size - 1][i] = '#'
            self.maze_pix[i][0] = '#'
            self.maze_pix[i][self.pix_size - 1] = '#'
        for i in range(2, self.pix_size - 2, 2):
            for j in range(2, self.pix_size - 2, 2):
                self.maze_pix[i][j] = '#'

    def set_wall_between(self, x, y, x2, y2):
        self.set_char_between(x, y, x2, y2, '#')

    def set_empty_between(self, x, y, x2, y2):
        self.set_char_between(x, y, x2, y2, ' ')

    def set_char_between(self, x, y, x2, y2, char):
        pix_coords = self.to_pix_coords(x, y)
        if y == y2 and x2 == x + 1:  # right
            self.maze_pix[pix_coords[1]][pix_coords[0] + 1] = char
        elif y == y2 and x2 == x - 1:  # left
            self.maze_pix[pix_coords[1]][pix_coords[0] - 1] = char
        elif x == x2 and y2 == y + 1:  # down
            self.maze_pix[pix_coords[1] + 1][pix_coords[0]] = char
        elif x == x2 and y2 == y - 1:  # up
            self.maze_pix[pix_coords[1] - 1][pix_coords[0]] = char
        else:
            raise Exception("Invalid coordinates in setting wall ({}, {}, {}, {})".format(x, y, x2, y2))

    def set_char_at(self, x, y, ch):
        pix_coords = self.to_pix_coords(x, y)
        self.maze_pix[pix_coords[1]][pix_coords[0]] = ch

    def set_char_at_coords(self, coords, ch):
        self.set_char_at(coords[0], coords[1], ch)

    @staticmethod
    def to_pix_coords(x, y):
        return 1 + 2*x, 1+ 2*y

    def print(self):
        for row in self.maze_pix:
            print(''.join(row))


if __name__ == '__main__':
    run()
