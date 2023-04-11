from simple_clingo import SimpleClingo
import re
import time


def run():
    game = MysteryGame()
    game.start(skip_name=True)


class MysteryGame:
    """Class controlling the whole game of murder mystery.
    It contains a model of the mystery instantiated by ASP program examples/3_murder_mystery.lp and provides interaction
    with it.

    The inner working is centered around self.current_actions, which contains info about such action - mainly which
    method to call when invoked.
    """
    class ActionInfo:
        """Metadata about a possible action by player."""
        action_pattern = re.compile('([^<>]+)')
        arg_pattern = re.compile('<([^>]+)>')

        def __init__(self, command, method_name, help_text, automatic_validation=True):
            """
            :param command: The command as it should be written.
                            Should start with "action text" followed by a number of "<subjects>"
            :param method_name: Method of the MysteryGame class that should be called when action is performed
            :param help_text: Help text about the action
            :param automatic_validation: If True, automatically validates that subjects are valid by seeing if the
                                         <subject>/1 predicate in mystery model exists with given argument
            """
            self.command = command
            # Parsing the command to extract "base" and "args"s
            self.command_base = self.action_pattern.match(command).group(0).rstrip()
            self.argument_names = self.arg_pattern.findall(command)
            self.method_name = method_name
            self.help_text = help_text
            self.automatic_validation = automatic_validation

        def matches(self, user_input):
            """Checks if user input is a call to this action"""
            return user_input.startswith(self.command_base)

        def extract_args(self, user_input):
            """Extracts arguments from user input. Returns string """
            args = user_input[len(self.command_base) + 1:]
            # Remove empty and strip whitespace
            args = [x for x in map(lambda s: s.strip(), args.split(' ')) if x != '']
            if len(args) != len(self.argument_names):
                return "Invalid number of arguments for action"
            return args

    # Actions available in the default context
    # Add new actions here, maybe set automatic_validation = False (if you don't want them to be checked against model)
    default_actions = [
        ActionInfo('?', '__help', 'shows this help'),
        ActionInfo('list rooms', '__list_rooms', 'lists all rooms in the mansion'),
        ActionInfo('list people', '__list_people', 'lists all people in the mansion'),
        ActionInfo('examine <room>', '__examine_room', 'examines given room'),
        ActionInfo('question <person>', '__question_person', 'starts questioning a given person'),
        ActionInfo('accuse <person>', '__accuse_person', '(final answer) accuses person of being the murderer'),
        ActionInfo('quit', '__quit', 'quits the game')
    ]

    # Actions available while questioning somebody
    questioning_actions = [
        ActionInfo('?', '__help', 'shows this help'),
        ActionInfo('where were you', '__where_were_you', 'asks the person where he was at time of murder'),
        ActionInfo('do you own <weapon>', '__do_you_own', 'asks the person if he owns a specific item'),
        ActionInfo('stop', '__reset_context', 'stop questioning'),
        ActionInfo('who did you see', '__who_did_you_see', 'ask the person who he saw in the room he/she was in'),
        ActionInfo('who do you think might have done this', '__who_do_you_think_might_have_done_this', 'ask the person who he think might be the murderer')
    ]

    def __init__(self):
        # Instantiate a model - the murder case
        clingo = SimpleClingo()
        clingo.load_path('examples/3_murder_mystery.lp')
        self.model = clingo.solve()[0]

        self.player_name = "Anonymous"
        self.ended = False
        self.actions = self.default_actions
        self.context = {}
        self.n_actions = 0

    def start(self, skip_name=False):
        """Starts a game of murder-mystery - writes some info and initiates main loop."""
        print("Welcome to the murder mystery game!")
        if not skip_name:
            player_name = input("Please, enter your name: ").strip()
            if len(player_name) > 0:
                self.player_name = player_name
        print("May luck be on your side, {}!\n".format(self.player_name))

        print("Tragedy! A murder has occurred last night. {} was found dead in {} this morning.\n".format(
                self.model['victim'][0], self.model['murder_room'][0]
              ),
              "Several friends (or are they?) were staying in this mansion, and nobody came in or out since.\n",
              "You, a detective, were called in to solve this mystery.\n",
              "Ask questions, examine places, and figure out who dunnit!\n",
              sep='')

        print("Type ? for help!")
        while not self.ended:
            self.__main_loop()
        print("Thanks for playing!")

    def __main_loop(self):
        """Main loop which reacts to player input. Finds proper action and calls it."""
        user_input = input()

        matching_actions = list(filter(lambda action: action.matches(user_input), self.actions))
        if len(matching_actions) == 0:
            print("Invalid command! Please type '?' for help.")
        elif len(matching_actions) > 1:
            # This probably shouldn't happen if the rules are well designed... But just in case.
            print("Multiple actions possible! Please be more specific.")
        else:
            self.__perform_action(matching_actions[0], user_input)

    def __perform_action(self, action, user_input):
        """Calls an action with parameters from user_input. Provides automatic validation of arguments if turned on."""
        args = action.extract_args(user_input)

        # Extract args can return a string error
        if isinstance(args, str):
            print(args)
            return

        # Automatic validation - each named argument must be instantiated in the model
        if action.automatic_validation:
            for (arg, arg_name) in zip(args, action.argument_names):
                if arg not in self.model[arg_name]:
                    print("{} is not a valid {}".format(arg, arg_name))
                    return

        if action.command not in ['?', 'quit', 'stop']:
            self.n_actions += 1

        # Call method with proper arguments
        method_name = action.method_name
        if method_name[:2] == '__':
            method_name = '_MysteryGame{}'.format(method_name)

        getattr(self, method_name)(*args)

    # ========================
    #  Actual actions section
    # ========================

    def __help(self):
        print("Possible commands:")
        print('\n'.join(["{} = {}".format(action.command, action.help_text) for action in self.actions]))

    def __reset_context(self):
        print("You can once again do anything you wish.")
        self.actions = self.default_actions
        self.context = {}

    def __quit(self):
        print("You leave the mansion, survivors blankly staring as you leave them with the murderer ...")
        self.ended = True

    def __list_rooms(self):
        print("These are the accessible rooms in the mansion:")
        print(", ".join(self.model['room']))

    def __list_people(self):
        print("These are the people in the mansion:")
        people = map(lambda x: '{}(dead)'.format(x) if x == self.model['victim'][0] else x, self.model['person'])
        print(", ".join(people))

    def __examine_room(self, room):
        if room == self.model['murder_room'][0]:
            print("On the ground lies the body of {}, seems killed using a {}. Otherwise, normal {}.".format(
                self.model['victim'][0],
                self.model['murder_weapon'][0],
                room
            ))
        else:
            print("There is nothing exceptional in this room.")

    def __question_person(self, person):
        if person == self.model['victim'][0]:
            print("You cannot speak to dead people!")
            return

        print("Questioning {}".format(person))
        self.context['person'] = person
        self.actions = self.questioning_actions

    def __accuse_person(self, person):
        print("You have accused {}! Let's see if they're the murderer!".format(person))
        murderer = self.model['murderer'][0]

        # Add drama
        for _ in range(3):
            time.sleep(1)
            print('.')
        time.sleep(1)

        if person == murderer:
            print("Good job, {}! You have accused the actual murderer!".format(self.player_name))
            print("It only took you {} steps.".format(self.n_actions))
            if self.n_actions <= 4:
                print("It seems you're good at guessing...")
        else:
            print("{}, you picked the wrong person! The actual murderer was {}.".format(self.player_name, murderer))
        self.ended = True
        time.sleep(1)

    # =============================
    #  Questioning actions section
    # =============================

    def __where_were_you(self):
        person = self.context['person']
        #print(list(filter(lambda was_in_room: was_in_room[0] == person, self.model['when_asked_was_in_room'])))
        room = list(filter(lambda was_in_room: was_in_room[0] == person, self.model['when_asked_was_in_room']))[0][1]
        print(self.model['when_asked_was_in_room'])
        print(self.model['was_in_room'])
        print(self.model['in_relationship_with'])
        print(self.model['knows_who_with_who'])
        print(self.model['accuse'])
        filterObject = filter(lambda was_in_room: was_in_room[0] == person, self.model['when_asked_was_in_room'])
        #print(list(filter(lambda was_in_room: was_in_room[0] == person, self.model['when_asked_was_in_room'])))
        #for i in filterObject:
        #    print(i)
        print("I was in the {} the whole time.".format(room))
    def __who_did_you_see(self):
        person = self.context['person']
        print(self.model['who_saw_who'])
        whos = list(filter(lambda who_saw_who: who_saw_who[0] == person, self.model['who_saw_who']))
        print(whos)

        if (len(whos) == 0):
            print("I saw nobody but myself")
            return

        for i in range(len(whos) - 1):
            print("I saw " + whos[i][1] + " and ", end = "")
        
        if (len(whos) == 1):
            print("I saw " + whos[-1][1])
        else:
            print(whos[-1][1])
    
    def __who_do_you_think_might_have_done_this(self):
        person = self.context['person']
        print(self.model['suspect'])
        who = list(filter(lambda suspect: suspect[0] == person, self.model['suspect']))
        victim = self.model['victim'][0]

        print(who)
        

        if (len(who) == 0):
            print("I don't know")
            return
        
        print(who[0][1] +", because I think he/she is jealous of the relationship between me and the " + victim)


    def __do_you_own(self, weapon):
        person = self.context['person']
        owns_weapon = len(
            list(filter(lambda has_item: has_item[0] == person and has_item[1] == weapon, self.model['owns_item']))
        ) > 0
        print("Yes." if owns_weapon else "No.")


if __name__ == '__main__':
    run()
