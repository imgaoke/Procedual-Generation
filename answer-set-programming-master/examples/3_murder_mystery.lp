% This programs instantiates a murder mystery - it establishes the setting:
% Who is the murderer, who was where at time of murder, and some other clues to solving the case.
% With the power of ASP, it is easy to add constraints that make the mystery always solvable.

% Generate
% --------
person(adam; beatrice; charlie; david; elisa).
weapon(knife; bat; gun).
room(bathroom; living_room; study; kitchen).

% Exactly one of the people is the victim.
{ victim(X) : person(X) } = 1.
% Exactly one of the people is the murderer.
{ murderer(X) : person(X) } = 1.
% Exactly one of the weapons is the murder weapon.
{ murder_weapon(X) : weapon(X) } = 1.
% Exactly one of the rooms is the murder room.
{ murder_room(X) : room(X) } = 1.

% 2 to 3 people were in the room in the rough time.
2 { was_in_room(X, Y) : person(X), murder_room(Y) } 3.
% 2 to 3 people have access to the murder weapon.
2 { owns_item(X, Y) : person(X), murder_weapon(Y) } 3.

% the victim has relationships with 3 people in which one is the murder
{ in_relationship_with(X, Y) : victim(X), murderer(Y) } = 1.
{ in_relationship_with(X, Y) : victim(X), person(Y), not murderer(Y), not victim(Y) } = 2.

% For the 3 people who is in the relationship with the victim,
% the murderer knows all the relationships 
% but for the other two people A and B, A only knows the relationship between the victim and B, and B only knows the relationship between the victim and A
knows_who_with_who(X, Y, Z) :- murderer(X), victim(Y), person(Z), not murderer(Z), not victim(Z), in_relationship_with(Y, Z).
knows_who_with_who(X, Y, Z) :- person(X), not murderer(X), not victim(X), victim(Y), in_relationship_with(Y, X), person(Z), not murderer(Z), not victim(Z), in_relationship_with(Y, Z), Z != X.

% the 3 people will suspect who they know
1 { suspect(X, Z) : knows_who_with_who(X, Y, Z), murderer(X) } 1.
2 { suspect(X, Z) : knows_who_with_who(X, Y, Z), not murderer(X) } 2.

% Each person owns 1 to 2 items.
1 { owns_item(X, Y) : person(X), weapon(Y) } 2 :- person(X).
% Each person was in some room.
1 { was_in_room(X, Room) : room(Room) } 1 :- person(X).

% when asked about if was in room, the person who is not the murderer will tell the truth
when_asked_was_in_room(X, Y) :- person(X), not murderer(X), was_in_room(X, Y).
% when asked about if was in room, the murderer will tell the lie
1 { when_asked_was_in_room(X, Y) : room(Y), not murder_room(Y) } 1 :- person(X), murderer(X).

% the person who is not murderer will see everyone else but he/she may forget
{ who_saw_who(X, Y) } :- room(Room), was_in_room(X, Room), person(X), not murderer(X), was_in_room(Y, Room), person(Y), X != Y.
% the person who is the murderer will claim to see the person in the room he claimed to be in
who_saw_who(X, Y) :- room(Room), when_asked_was_in_room(X, Room), person(X), murderer(X) , was_in_room(Y, Room), person(Y), X != Y.

% Test
% ----
% Murderer and victim are not the same person.
:- murderer(X), victim(X).
% The murderer was in the murder room, and had access to the murder weapon.
was_in_room(X, Y) :- murderer(X), murder_room(Y).
owns_item(X, Y) :- murderer(X), murder_weapon(Y).