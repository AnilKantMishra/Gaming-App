1 League: Represents a collection of teams that compete against each other. This entity can hold information like the league name and related teams.

2 Tournament: Represents a specific competition that can have multiple teams participating. This entity can include details like the tournament name, start and end dates, and its status (ongoing, completed, etc.).

3 Team: Represents a group of players participating in a league or tournament. This entity can include the team name and its members.

4 Participation: Represents the relationship between a user (or a team) and a tournament. It tracks whether a team has joined a tournament, their status, and potentially match results.

To create a functioning flow for managing League, Team, Participation, and Tournament entities in your application, follow these steps:

Create League: Create a league first.
Create Team: Create a team associated with that league.
Create Tournament: Create a tournament.
Create Participation: Link the team to the tournament, possibly using one of your existing customers