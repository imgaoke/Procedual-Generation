from simple_clingo import SimpleClingo

if __name__ == '__main__':
    # This is a minimalistic example showing how to load and run a clingo program using SimpleClingo
    clingo = SimpleClingo()

    # Load a program from a file
    clingo.load_path('examples/2_hamiltionian_cycle.lp')

    # Calling solve will produce models. You can also reduce the randomness of searching for models,
    # so that heuristics will be used more often -> less random solution, but faster
    models = clingo.solve(1, randomness=1.0, verbose=True)
    #print(models)
    model = models[0]
    print("ham_edge: ")
    print(model['ham_edge'])
    print("hampath_edge: ")
    print(model['hampath_edge'])
    print("hampath_reach: ")
    print(model['hampath_reach'])
