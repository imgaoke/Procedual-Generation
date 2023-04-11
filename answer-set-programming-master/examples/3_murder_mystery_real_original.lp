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

% Each person owns 1 to 2 items.
1 { owns_item(X, Y) : person(X), weapon(Y) } 2 :- person(X).
% Each person was in some room.
1 { was_in_room(X, Room) : room(Room) } 1 :- person(X).

% Test
% ----
% Murderer and victim are not the same person.
:- murderer(X), victim(X).
% The murderer was in the murder room, and had access to the murder weapon.
was_in_room(X, Y) :- murderer(X), murder_room(Y).
% Everybody owns at least a couple items.
owns_item(X, Y) :- murderer(X), murder_weapon(Y).

