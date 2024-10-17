# Gaming Application REST API's (Java)

### Create League

   - Input: League name, description, and other relevant details.
   - Output: A new League entity stored in the database.
### Create Team
   - Input: Team name, list of members, and the league it will belong to.
   - Output: A new Team entity linked to the specified League. 
### Create Tournament
   - Input: Tournament name, start and end dates, status (e.g., upcoming, ongoing, completed), and associated league.
   - Output: A new Tournament entity that includes the linked League. 
### Create Participation
   - Input: Team ID, Tournament ID, and participation status (e.g., registered, active, eliminated).
   - Output: A new Participation entity that establishes the relationship between the Team and the Tournament, including any match results if applicable.
### Update Entities as Needed
   - League: Update league details or add/remove teams.
   - Team: Update team members or details.
   - Tournament: Change status, update dates, or modify participating teams.
   - Participation: Update the participation status, match results, or any relevant details.
### Retrieve and Display Information
   - League: View all associated teams and tournaments.
   - Tournament: View participating teams, match schedules, and results.
   - Team: View team members and their participation status in tournaments.
   - Participation: Track the history and results of each team’s participation in tournaments.