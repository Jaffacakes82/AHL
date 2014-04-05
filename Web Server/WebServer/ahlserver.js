/***************************************************************************
* ahlserver.js - The Air Hockey - Live! Backbone.
* Author: Joseph Ellis
* Student Number: 10007329

This file is part of Air Hockey - Live!

Air Hockey - Live! is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Air Hockey - Live! is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Air Hockey - Live!  If not, see <http://www.gnu.org/licenses/>.
****************************************************************************/

/* Required packages to run the server. */
var express = require('express'); // Used to manage the HTTP server.
var mysql = require('mysql'); // Package used for handling MySQL queries.
var twitter = require('twit') // Package used to interact with twitters API.

/**********************************
* Initial set up, start server.
***********************************/
var app = express(); // Create the express object used for handling requests.
app.set('title', 'Air Hockey - Live!'); // Set the title of the express application.
app.use(express.json()); // Tell the express application we're using JSON throughout.
app.listen(3000); // Listen on port 3000.

/* Twitter API set up */
var tweet = new twitter({
    consumer_key:         'mlXD6Una34mefXBDWc32Q'
  , consumer_secret:      'oj8eVta5jpuKNGRlBIfTDsUcl4EJ7Z5o8T3OHgYABc'
  , access_token:         '2362907707-h2z0ddOeousb4sdbkPTxPIZgV7H1sHfC5STgYnB'
  , access_token_secret:  'E5s5civPTb04b9vxWB9ZTpxCfaLrF7bewcqk4yTIuT8xM'
})

/* MySQL database configuration. */
var db_config = {
	host: 'localhost',
    user: 'root',
    password: '',
    database: 'airhockeylive'
};

var connection = mysql.createConnection(db_config); // Create the connection object

/* Depending on the window size, send a number of carriage returns and newlines to 'clear' the terminal window */
var lines = process.stdout.getWindowSize()[1]; // Get the number of lines in the terminal window.

for(var i = 0; i < lines; i++) {
    console.log('\r\n');
}

/* Server welcome message */
console.log("-------------------------------------");
console.log("Air Hockey - Live! Server. Port: 3000");

/* Global Variables */
var gameId = false; // Set when a game is started from the android application, otherwise false.
var winningScore = 7; // The maximum score in a game of Air Hockey - Live!

/***********************************************
* GET: '/'
*
* Base address of the Air Hockey - Live! Server
************************************************/
app.get('/', function(req, res)
{
	res.send("Air Hockey - Live!"); //Send a simple string in the response as for now this page is widely unused.
});

/***********************************************
* GET: '/gamestarted'
*
* Used by the Arduino to determine whether a
* game of Air Hockey - Live! has been initiated
************************************************/
app.get('/gamestarted', function (req, res)
{
	console.log("Game start status requested.");
	res.send("ID:" + gameId); // Send either 'false' or the ID of a game of Air Hockey - Live! back to the Arduino.
	
	if (gameId != false)
	{
		gameId = false; // If gameId is not false, set it to false so as to allow for another game to started.
	}
});

/*************************************************************
* POST: '/startgame'
*
* Handles the request sent from the Android application
* to start a game. The information in the body is then parsed
* and the relevant game updated in the database.
**************************************************************/  
app.post('/startgame', function(req, res)
{
	/* Get the contents of the request body into variables */
	var id = req.body.ID;
	var player1 = req.body.Player1;
	var player2 = req.body.Player2;
	var state = req.body.State;
	var scorep1 = req.body.Player1Score;
	var scorep2 = req.body.Player2Score;
	
	if (id != null)
	{
		console.log("Starting game with ID: " + id);
		
		//Call the UpdateGame function to update database contents.
		UpdateGame(id, player1, player2, state, scorep1, scorep2, function (success) 
		{
			if (success)
			{
				console.log("Game Started Successfully.");
				gameId = id; // Set the 'gameId' variable to the ID of the game in question so the Arduino knows a game has been initiated.
				res.send("true"); // Send a success flag to the Android application so it can continue execution.
			}
			else
			{
				console.log("Failed to start game.");
				res.send("false");
			}
		});
	}
});

/************************************************************
* POST: '/goal'
*
* Handles the request from the Arduino that signifies a goal 
* being scored.
*
* This function updates the game scores in the database,
* determines a winner and also tweets the latest score.
************************************************************/
app.post('/goal', function (req, res)
{
	/* Get request body contents into variables */
	var score1 = req.body.player1;
	var score2 = req.body.player2;
	var relatedGameId = req.body.gameId;
	
	/* Create a data useable in the tweets. */
	var date = new Date();
	var dateString = String(date);
	var indexToParseDate = dateString.indexOf("GMT");
	dateString = dateString.substring(0, indexToParseDate);
	
	console.log("GAMEID: " + gameId);
	
	// Call the GetPlayerInfo function - main execution happens in the callback.
	GetPlayerInfo(relatedGameId, function (success, player1Id, player2Id, player1Twitter, player2Twitter)
	{
		if (success) // If the MySQL query ran without failure.
		{
			if (score1 == winningScore || score2 == winningScore) // Check to see if either players score is equal to 7.
			{
				if (score1 == winningScore)
				{
					// Use the twit package to post a tweet to the Air Hockey - Live! twitter account using variables set in the GetPlayerInfo function. 
					tweet.post('statuses/update', { status: dateString + ' Air Hockey - Live! ' + player1Twitter + ' just beat ' + player2Twitter + ' #airhockeylive' }, function(err, reply) {
					
						if (err)
						{
							console.log(err);
						}
					})
				}
				else
				{
					// Use the twit package to post a tweet to the Air Hockey - Live! twitter account using variables set in the GetPlayerInfo function.
					tweet.post('statuses/update', { status: dateString + ' Air Hockey - Live! ' + player2Twitter + ' just beat ' + player1Twitter + ' #airhockeylive' }, function(err, reply) {
					
						if (err)
						{
							console.log(err);
						}
					})
				}
				
				//Pass the relevant player ID's, GameID and scores to the ConfirmWin function to log the win in the database.
				ConfirmWin(relatedGameId, player1Id, player2Id, score1, score2, function (sucess)
				{
					if (success)
					{
						console.log("Win Recorded.");
					}
				});
			}
			else
			{
				// If a win has not been achieved, tweet the current score.
				tweet.post('statuses/update', { status: dateString + ' Air Hockey - Live! Current Score: ' + player1Twitter + ' ' + score1 + ' - ' + score2 + ' ' + player2Twitter + ' #airhockeylive' }, function(err, reply) {
					
					if (err)
					{
						console.log(err);
					}
				})
				
				// Update the relevant game in the database.
				UpdateScore(relatedGameId, score1, score2, function (success)
				{
					if (success)
					{
						console.log("Score updated.");
					}
				});
			}
		}
	});
	
	console.log("Player 1 score: " + score1);
	console.log("Player 2 score: " + score2);

	res.send(); // Send a blank response.
});

/**********************************************
* POST: '/register'
*
* Handles the registration of a new user,
* request made through the Android application.
***********************************************/
app.post('/register', function(req, res)
{
	/* Set variables from the request body. */
	var id = req.body.id;
	var username = req.body.username;
	var password = req.body.password;
	var name = req.body.name;
	var twitter = req.body.twitter;

	console.log("\n");
	console.log("\n");	
	
	/* Call the RegisterUser function, to handle the database query, passing the variables we just set */
	RegisterUser(id, username, password, name, twitter, function (success) 
	{
		if (success)
		{
			console.log("User registered.");
			res.send("true"); // If successful, send a success flag back to the application.
		}
		else
		{
			console.log("Failed to register user.");
			res.send("false");
		}
	});
	
});

/***************************************************
* POST: '/creategame'
*
* Handles the creation of a new game in the system.
* Request made from the Android application.
****************************************************/
app.post('/creategame', function (req, res)
{
	/* Set variables from the request body. */
	var id = req.body.ID; // Game ID
	var player1 = req.body.Player1; // Game Player1
	var state = req.body.State; // Game state
	
	console.log("Creating game with ID: " + id);
	
	// Pass the variables to the CreateGame function to execute the database query.
	CreateGame(id, player1, state, function (success)
	{
		if (success)
		{
			console.log("Game created with ID: " + id + " by player with ID: " + player1);
			res.send("true"); // Send success flag to the android application.
		}
		else
		{
			console.log("Failed to create game.");
			res.send("false");
		}
	});
});

/*******************************************************
* POST: '/gameslist'
*
* Returns a list of valid games that a player can join.
* Request made from the Android application.	
*******************************************************/
app.post('/gameslist', function (req,res)
{
	console.log("Getting games list.");
	
	// Fetch PlayerID from Request body.
	var playerId = req.body.ID;
	console.log("Player ID: " + playerId);
	
	// Call the FetchGamesList function, passing the playerId to use in the query.
	FetchGamesList(playerId, function (success, result)
	{
		if (success)
		{
			res.send(result); // Send the resulting array of games to the android application.
		}
		else
		{
			console.log(result);
			res.send(result);
		}
	});
});

/*********************************************************
* POST: '/login'
*
* Handles the login request from the Android application.
**********************************************************/
app.post('/login', function(req, res)
{
	/* Fetch the supposed user credentials from the request body */
	var username = req.body.username;
	var password = req.body.password;
	
	console.log("\n");
	console.log("Logging in user.");
	
	// Call the LoginUser function to see if a player exists with those credentials.
	LoginUser(username, password, function (success, result) 
	{
		if (success)
		{
			if (result.length == 0) // If no users
			{
				console.log("No user found.");
				res.send(result);
			}
			else
			{
				console.log("\nFound user information.");
				res.send(result); // Send player information back to the application.
			}
		}
		else
		{
			console.log(result);
			res.send(); // Send empty response if query failed to execute.
		}
	});
	
});

/********************************************************
* POST: '/getplayer'
*
* Fetch a player from the database from a given PlayerID. 
*********************************************************/
app.post('/getplayer', function (req, res)
{
	// Get PlayerID from request body.
	var id = req.body.ID;
	
	// Call the FetchPlayer function to handle database querying.
	FetchPlayer(id, function (success, result)
	{
		if (success)
		{
			if (result.length == 0) // If query succeeded but no results returned.
			{
				console.log("Unable to fetch player with id: " + id);
				res.send(result);
			}
			else
			{ 	
				res.send(result); // Send player information in the response to the application.
			}
		}
		else
		{
			console.log(result);
			res.send();
		}
	});
});

/************************************************
* POST: '/getgame'
* 
* Fetch game information from the database using
* a provided ID.
************************************************/
app.post('/getgame', function (req, res)
{
	var id = req.body.ID; // GameID
	
	// Call FetchGame function to handle database query.
	FetchGame(id, function (success, result)
	{
		if (success)
		{
			if (result.length == 0) // If successful query execution but no results returned.
			{
				console.log("Unable to fetch game with id: " + id);
				res.send(result);
			}
			else
			{
				res.send(result); // Send game information to the application.
			}
		}
		else
		{
			console.log(result);
			res.send();
		}
	});
});

/***********************************************
* POST: '/updategame'
*
* Using the information provided in the request
* update a game record in the database.
************************************************/
app.post('/updategame', function (req,res)
{
	/* Set variables from request body items */
	var id = req.body.ID;
	var player1 = req.body.Player1;
	var player2 = req.body.Player2;
	var state = req.body.State;
	var scorep1 = req.body.Player1Score;
	var scorep2 = req.body.Player2Score;
	
	if (id != null)
	{
		console.log("Updating game with ID: " + id);
		
		// Call the UpdateGame function, passing our variables to update the game record.
		UpdateGame(id, player1, player2, state, scorep1, scorep2, function (success) 
		{
			if (success)
			{
				console.log("Game Updated Successfully.");
				res.send("true"); // Let the application know the game was updated successfully.
			}
			else
			{
				console.log("Failed to update game.");
				res.send("false");
			}
		});
	}
});

/***************************************************************
* ConfirmWin()
*
* Parameters:
* gameId - ID of the game to update in the database.
* player1Id - ID of player 1 to set the win field if they won.
* player2Id - ID of player 2 to set the win field if they won.
* player1Score - The final score of player 1.
* player2Score - The final score of player 2.
* callback - The callback function to run once processing done.
***************************************************************/
function ConfirmWin(gameId, player1Id, player2Id, player1Score, player2Score, callback)
{
	var sql;

	if (player1Score == winningScore)
	{
		sql = 'UPDATE game SET Player1Score = ?, Player2Score = ?, State = ?, Winner = ? WHERE game.ID = ?'; // The SQL statement to run against the database to set Player1 as winner.
		var inserts = [ player1Score, player2Score, 'FINISHED', player1Id, gameId ]; // As seen above, '?' are used as place holders for formatting purposes, this array holds their values.
		sql = mysql.format(sql, inserts); // Use the mysql package to format the SQL string appropriately.
	}
	else
	{
		sql = 'UPDATE game SET Player1Score = ?, Player2Score = ?, State = ?, Winner = ? WHERE game.ID = ?';
		var inserts = [ player1Score, player2Score, 'FINISHED', player2Id, gameId ];
		sql = mysql.format(sql, inserts);
	}
	
	// Run the SQL query against our MySQL database.
	var query = connection.query(sql, function (err, result)
	{
		if (err)
		{
			console.log(err)
		}
		else
		{
			callback(true); // If no errors, pass true as the callback parameter.
		}
	});
}

/***************************************************************
* UpdateScore()
*
* Parameters:
* gameId - ID of the game to update in the database.
* player1Score - The current score of player 1.
* player2Score - The current score of player 2.
* callback - The callback function to run once processing done.
***************************************************************/
function UpdateScore(gameId, player1Score, player2Score, callback)
{
	/* Build and format SQL string to update the scores of a given game. */
	var sql = 'UPDATE game SET Player1Score = ?, Player2Score = ? WHERE game.ID = ?'
	var inserts = [ player1Score, player2Score, gameId ];
	sql = mysql.format(sql, inserts);
	
	var query = connection.query(sql, function (err, result)
	{
		if (err)
		{
			console.log(err)
		}
		else
		{
			callback(true); // If query executed successfully, pass true to the callback.
		}
	});
}

/***************************************************************
* GetPlayerInfo()
*
* Parameters:
* gameId - ID of the game to get player information for.
* callback - The callback function to run once processing done.
***************************************************************/
function GetPlayerInfo(gameId, callback)
{
	var sql = 'CALL GetPlayerInfo(?);'; // Execute a stored procedure stored in the database.
	var inserts = [ gameId ]; // Pass the gameID as a parameter to the stored procedure.
	sql = mysql.format(sql, inserts);
	
	console.log(sql);
	
	// Run the SQL query against the database.
	var query = connection.query(sql, function (err, result)
	{
		if (err)
		{
			console.log(err);
			callback(false, "", "", "", ""); // If err, pass 'false' and nothing to the callback function.
		}
		else
		{	
			/* The GetPlayerInfo() stored procedure returns two sets of data, 
			the information we want to extract is found in a JSONArray found at the first index of the resultant array */
			if (result[0][0].Twitter != "@" && result[0][1].Twitter != "@") // Determine whether the player has a twitter username set.
			{
				callback(true, result[0][0].ID, result[0][1].ID, result[0][0].Twitter, result[0][1].Twitter); // Both do, so pass their ID's and Twitter usernames to the callback function.
			}
			else if (result[0][0].Twitter == "@" && result[0][1].Twitter != "@") // Determine whether the player has a twitter username set.
			{
				callback(true, result[0][0].ID, result[0][1].ID, result[0][0].Username, result[0][1].Twitter); // Player1 doesn't, pass their username instead
			}
			else if (result[0][0].Twitter != "@" && result[0][1].Twitter == "@") // Determine whether the player has a twitter username set.
			{
				callback(true, result[0][0].ID, result[0][1].ID, result[0][0].Twitter, result[0][1].Username);
			}
			else
			{
				callback(true, result[0][0].ID, result[0][1].ID, result[0][0].Username, result[0][1].Username); // Neither have a Twitter username set, pass their usernames to tweet instead.
			}
		}
	});
}

/***************************************************************
* CreateGame()
*
* Parameters:
* id - ID of the game to create a record for.
* player1 - The ID of player1 to set in the game record.
* state - The state of the game.
* callback - The callback function to run once processing done.
***************************************************************/
function CreateGame(id, player1, state, callback)
{
	/* Build SQL statement */
	var sql = 'INSERT INTO game (ID, Player1, State) VALUES (?,?,?)'
	var inserts = [ id, player1, state ];
	sql = mysql.format(sql, inserts);
	
	// Run the SQL query
	var query = connection.query(sql, function (err, result)
	{
		if (err)
		{
			console.log(err);
			callback(false);
		}
		else
		{
			callback(true); // Game created successfully.
		}
	});
}

/***************************************************************
* FetchPlayer()
*
* Parameters:
* id - The ID of the player whose information to get from the DB.
* callback - The callback function to run once processing done.
***************************************************************/
function FetchPlayer(id, callback)
{
	/* Build SQL query */
	var sql = 'SELECT * FROM player WHERE (id = ?)';
	var inserts = [ id ];
	sql = mysql.format(sql, inserts);
	
	var query = connection.query(sql, function (err, result)
	{
		if (err)
		{
			console.log(err);
			callback(false, result);
		}
		else
		{
			callback(true, result); // Query ran, return resultant rows if any.
		}
	});
}

/***************************************************************
* FetchGame()
*
* Parameters:
* id - ID of the game to fetch the record for.
* callback - The callback function to run once processing done.
***************************************************************/
function FetchGame(id, callback)
{
	/* Build SQL query */
	var sql = 'SELECT * FROM game WHERE (id = ?)';
	var inserts = [ id ];
	
	sql = mysql.format(sql, inserts);
	
	var query = connection.query(sql, function (err, result)
	{
		if (err)
		{
			console.log(err);
			callback(false, result);
		}
		else
		{
			callback(true, result); // Query ran, return game record.
		}
	});
}

/*********************************************************************
* FetchGamesList()
*
* Parameters:
* playerId - The playerID of the player to get appropriate games for.
* callback - The callback function to run once processing done.
**********************************************************************/
function FetchGamesList(playerId, callback)
{
	// Build the SQL query.
	var inserts = [ 'OPEN', playerId, playerId, 'FINISHED' ];
	var sql = 'SELECT * FROM game WHERE (State = ? OR ((Player1 = ? OR Player2 = ?) AND State != ?))';
	
	sql = mysql.format(sql, inserts);
	
	var query = connection.query(sql, function (err, result)
	{
		if (err)
		{
			console.log(err);
			callback(false, result);
		}
		else
		{
			callback(true, result); // Query ran, return suitable games if any.
		}
	});
}

/*********************************************************************
* RegisterUser()
*
* Parameters:
* id - The ID of the player to create
* username - The username of the player to create
* password - The users password
* name - the users real name
* twitter - the users twitter username
* callback - The callback function to run once processing done.
**********************************************************************/
function RegisterUser(id, username, password, name, twitter, callback)
{
	// Build SQL
	var sql = 'INSERT INTO Player VALUES(?,?,?,?,?)'
	var inserts = [ id, username, password, name, twitter ];
	
	sql = mysql.format(sql, inserts);
	
	var query = connection.query(sql,  function (err, result)
	{
		if (err)
		{
			console.log(err);
			callback(false);
		}
		else
		{
			callback(true); // Query ran, pass a successful flag to the callback.
		}
	});
}

/*****************************************
* UpdateGame()
*
* Parameters:
* id - The ID of the game to update.
* player1 - The ID of Player1 in the game
* player2 - The ID of Player2 in the game
* state - The state of the game
* scorep1 - Player 1's score
* scorep2 - Player 2's score
* callback - Callback function
******************************************/
function UpdateGame(id, player1, player2, state, scorep1, scorep2, callback)
{
	/* Build SQL query */
	var sql = 'UPDATE game SET Player1 = ?, Player2 = ?, State = ?, Player1Score = ?, Player2Score = ? WHERE ID = ? '
	var inserts = [ player1, player2, state, scorep1, scorep2, id ];
	
	sql = mysql.format(sql, inserts);
	
	var query = connection.query(sql,  function (err, result)
	{
		if (err)
		{
			console.log(err);
			callback(false);
		}
		else
		{
			callback(true); // Query ran, game updated.
		}
	});
}

/*****************************************
* LoginUser()
*
* Parameters:
* username - The username of the user trying to log in 
* password - The password of the user trying to login
* callback - Callback function
******************************************/
function LoginUser(username, password, callback)
{
	/* Build SQL */
	var sql = 'SELECT * FROM Player WHERE (Username = ? AND Password = ?)'
	var inserts = [ username, password ];
	
	sql = mysql.format(sql, inserts);
	
	var query = connection.query(sql, function (err, result)
	{
		if (err)
		{
			console.log(err);
			callback(false, err);
		}
		else
		{
			callback(true, result); // Query ran, return a user record if any.
		}
	});
}