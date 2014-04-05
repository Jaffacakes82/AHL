/***********************************

/************************
* Required packages.
************************/
var express = require('express');
var mysql = require('mysql');
var twitter = require('twit')

/**********************************
* Initial set up, start server.
***********************************/
var app = express();
app.set('title', 'Air Hockey - Live!');
app.use(express.json());
app.listen(3000);

/*************************
* Twitter app set up 
*************************/
var tweet = new twitter({
    consumer_key:         'mlXD6Una34mefXBDWc32Q'
  , consumer_secret:      'oj8eVta5jpuKNGRlBIfTDsUcl4EJ7Z5o8T3OHgYABc'
  , access_token:         '2362907707-h2z0ddOeousb4sdbkPTxPIZgV7H1sHfC5STgYnB'
  , access_token_secret:  'E5s5civPTb04b9vxWB9ZTpxCfaLrF7bewcqk4yTIuT8xM'
})

/****************************
* Database configuration
****************************/
var db_config = {
	host: 'localhost',
    user: 'root',
    password: '',
    database: 'airhockeylive'
};

var connection = mysql.createConnection(db_config);

/***********************
* Clear terminal window
***********************/
var lines = process.stdout.getWindowSize()[1];

for(var i = 0; i < lines; i++) {
    console.log('\r\n');
}

console.log("-------------------------------------");
console.log("Air Hockey - Live! Server. Port: 3000");

/*************************
* Global Variable Declarations.
*************************/
var gameId = false;
var winningScore = 7;

/***********************
* BASE 
************************/
app.get('/', function(req, res)
{
	res.send("Air Hockey - Live!");
});

/*********************************
* HANDLE GAME STARTED GET REQUEST
**********************************/
app.get('/gamestarted', function (req, res)
{
	console.log("Game start status requested.");
	res.send("ID:" + gameId);
	
	if (gameId != false)
	{
		gameId = false;
	}
});

/***************************
* HANDLE START GAME REQUEST
****************************/
app.post('/startgame', function(req, res)
{
	var id = req.body.ID;
	var player1 = req.body.Player1;
	var player2 = req.body.Player2;
	var state = req.body.State;
	var scorep1 = req.body.Player1Score;
	var scorep2 = req.body.Player2Score;
	
	if (id != null)
	{
		console.log("Starting game with ID: " + id);
		
		UpdateGame(id, player1, player2, state, scorep1, scorep2, function (success) 
		{
			if (success)
			{
				console.log("Game Started Successfully.");
				gameId = id;
				res.send("true");
			}
			else
			{
				console.log("Failed to start game.");
				res.send("false");
			}
		});
	}
});

/*****************************************************
* HANDLE GOAL REQUEST
******************************************************/
app.post('/goal', function (req,res)
{
	var score1 = req.body.player1;
	var score2 = req.body.player2;
	var relatedGameId = req.body.gameId;
	var date = new Date();
	var dateString = String(date);
	
	var indexToParseDate = dateString.indexOf("GMT");
	dateString = dateString.substring(0, indexToParseDate);
	
	console.log("GAMEID: " + gameId);
	
	GetPlayerInfo(relatedGameId, function (success, player1Id, player2Id, player1Twitter, player2Twitter)
	{
		if (success)
		{
			if (score1 == winningScore || score2 == winningScore)
			{
				if (score1 == winningScore)
				{
					tweet.post('statuses/update', { status: dateString + ' Air Hockey - Live! ' + player1Twitter + ' just beat ' + player2Twitter + ' #airhockeylive' }, function(err, reply) {
					
						if (err)
						{
							console.log(err);
						}
					})
				}
				else
				{
					tweet.post('statuses/update', { status: dateString + ' Air Hockey - Live! ' + player2Twitter + ' just beat ' + player1Twitter + ' #airhockeylive' }, function(err, reply) {
					
						if (err)
						{
							console.log(err);
						}
					})
				}
				
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
				tweet.post('statuses/update', { status: dateString + ' Air Hockey - Live! Current Score: ' + player1Twitter + ' ' + score1 + ' - ' + score2 + ' ' + player2Twitter + ' #airhockeylive' }, function(err, reply) {
					
					if (err)
					{
						console.log(err);
					}
				})
				
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

	res.send();
});


/****************************
* HANDLE GAME XML GET REQUEST
*****************************/
app.get('/game', function(req,res)
{
	if (scoreJson == null)
	{
		res.send("Game not started.");
	}
	else
	{
		res.send(scoreJson);
	}
});

/******************************
* HANDLE REGISTER POST REQUEST
*******************************/
app.post('/register', function(req, res)
{
	var id = req.body.id;
	var username = req.body.username;
	var password = req.body.password;
	var name = req.body.name;
	var twitter = req.body.twitter;

	console.log("\n");
	console.log("\n");	
	
	RegisterUser(id, username, password, name, twitter, function (success) 
	{
		if (success)
		{
			console.log("User registered.");
			res.send("true");
		}
		else
		{
			console.log("Failed to register user.");
			res.send("false");
		}
	});
	
});

/*******************
* Post: Create Game
********************/
app.post('/creategame', function (req, res)
{
	var id = req.body.ID;
	var player1 = req.body.Player1;
	var state = req.body.State;
	
	console.log("Creating game with ID: " + id);
	
	CreateGame(id, player1, state, function (success)
	{
		if (success)
		{
			console.log("Game created with ID: " + id + " by player with ID: " + player1);
			res.send("true");
		}
		else
		{
			console.log("Failed to create game.");
			res.send("false");
		}
	});
});

/**************************************************
* GET LIST OF AVAILABLE GAMES FOR A GIVEN PLAYERID	
***************************************************/
app.post('/gameslist', function (req,res)
{
	console.log("Getting games list.");
	
	var playerId = req.body.ID;
	console.log("Player ID: " + playerId);
	
	FetchGamesList(playerId, function (success, result)
	{
		if (success)
		{
			res.send(result);
		}
		else
		{
			console.log(result);
			res.send(result);
		}
	});
});

/***************************
* HANDLE LOGIN POST REQUEST
****************************/
app.post('/login', function(req, res)
{
	var username = req.body.username;
	var password = req.body.password;
	
	console.log("\n");
	console.log("Logging in user.");
	
	LoginUser(username, password, function (success, result) 
	{
		if (success)
		{
			if (result.length == 0)
			{
				console.log("No user found.");
				res.send(result);
			}
			else
			{
				console.log("\nFound user information.");
				res.send(result);
			}
		}
		else
		{
			console.log(result);
			res.send();
		}
	});
	
});

/**************************
* GET PLAYER POST REQUEST 
***************************/
app.post('/getplayer', function (req, res)
{
	var id = req.body.ID;
	
	FetchPlayer(id, function (success, result)
	{
		if (success)
		{
			if (result.length == 0)
			{
				console.log("Unable to fetch player with id: " + id);
				res.send(result);
			}
			else
			{
				res.send(result);
			}
		}
		else
		{
			console.log(result);
			res.send();
		}
	});
});

/**************************
* GET GAME POST REQUEST 
***************************/
app.post('/getgame', function (req, res)
{
	var id = req.body.ID;
	
	FetchGame(id, function (success, result)
	{
		if (success)
		{
			if (result.length == 0)
			{
				console.log("Unable to fetch game with id: " + id);
				res.send(result);
			}
			else
			{
				res.send(result);
			}
		}
		else
		{
			console.log(result);
			res.send();
		}
	});
});

/******************
* Post: Update Game
*******************/
app.post('/updategame', function (req,res)
{
	var id = req.body.ID;
	var player1 = req.body.Player1;
	var player2 = req.body.Player2;
	var state = req.body.State;
	var scorep1 = req.body.Player1Score;
	var scorep2 = req.body.Player2Score;
	
	if (id != null)
	{
		console.log("Updating game with ID: " + id);
		
		UpdateGame(id, player1, player2, state, scorep1, scorep2, function (success) 
		{
			if (success)
			{
				console.log("Game Updated Successfully.");
				res.send("true");
			}
			else
			{
				console.log("Failed to update game.");
				res.send("false");
			}
		});
	}
});

function ConfirmWin(gameId, player1Id, player2Id, player1Score, player2Score, callback)
{
	var sql;

	if (player1Score == winningScore)
	{
		sql = 'UPDATE game SET Player1Score = ?, Player2Score = ?, State = ?, Winner = ? WHERE game.ID = ?';
		var inserts = [ player1Score, player2Score, 'FINISHED', player1Id, gameId ];
		sql = mysql.format(sql, inserts);
	}
	else
	{
		sql = 'UPDATE game SET Player1Score = ?, Player2Score = ?, State = ?, Winner = ? WHERE game.ID = ?';
		var inserts = [ player1Score, player2Score, 'FINISHED', player2Id, gameId ];
		sql = mysql.format(sql, inserts);
	}
	
	var query = connection.query(sql, function (err, result)
	{
		if (err)
		{
			console.log(err)
		}
		else
		{
			callback(true);
		}
	});
}

function UpdateScore(gameId, player1Score, player2Score, callback)
{
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
			callback(true);
		}
	});
}

function GetPlayerInfo(gameId, callback)
{
	var sql = 'CALL GetPlayerInfo(?);';
	var inserts = [ gameId ];
	sql = mysql.format(sql, inserts);
	
	console.log(sql);
	
	var query = connection.query(sql, function (err, result)
	{
		if (err)
		{
			console.log(err);
			callback(false, "", "", "", "");
		}
		else
		{	
			if (result[0][0].Twitter != "@" && result[0][1].Twitter != "@")
			{
				callback(true, result[0][0].ID, result[0][1].ID, result[0][0].Twitter, result[0][1].Twitter);
			}
			else if (result[0][0].Twitter == "@" && result[0][1].Twitter != "@")
			{
				callback(true, result[0][0].ID, result[0][1].ID, result[0][0].Username, result[0][1].Twitter);
			}
			else if (result[0][0].Twitter != "@" && result[0][1].Twitter == "@")
			{
				callback(true, result[0][0].ID, result[0][1].ID, result[0][0].Twitter, result[0][1].Username);
			}
			else
			{
				callback(true, result[0][0].ID, result[0][1].ID, result[0][0].Username, result[0][1].Username);
			}
		}
	});
}

/********************
CREATE GAME FUNCTION
********************/
function CreateGame(id, player1, state, callback)
{
	var sql = 'INSERT INTO game (ID, Player1, State) VALUES (?,?,?)'
	var inserts = [ id, player1, state ];
	
	sql = mysql.format(sql, inserts);
	
	var query = connection.query(sql, function (err, result)
	{
		if (err)
		{
			console.log(err);
			callback(false);
		}
		else
		{
			callback(true);
		}
	});
}
/***********************
* FETCH PLAYER FUNCTION
************************/
function FetchPlayer(id, callback)
{
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
			callback(true, result);
		}
	});
}

/**********************
* FETCH GAME FUNCTION 
**********************/
function FetchGame(id, callback)
{
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
			callback(true, result);
		}
	});
}

/****************************
* FETCH GAMES LIST FUNCTION
****************************/
function FetchGamesList(playerId, callback)
{
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
			callback(true, result);
		}
	});
}

/********************************************************************
* REGISTER USER FUNCTION
*********************************************************************/
function RegisterUser(id, username, password, name, twitter, callback)
{
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
			callback(true);
		}
	});
}

/********************************************************************
* REGISTER USER FUNCTION
*********************************************************************/
function UpdateGame(id, player1, player2, state, scorep1, scorep2, callback)
{
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
			callback(true);
		}
	});
}

/********************************************************************
* LOGIN USER FUNCTION
*********************************************************************/
function LoginUser(username, password, callback)
{
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
			callback(true, result);
		}
	});
}