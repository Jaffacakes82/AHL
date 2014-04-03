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
var gameStarted = false;
var scoreJson = null;

/***********************
* BASE 
************************/
app.get('/', function(req, res)
{
	res.send("Air Hockey - Live!");
});

/*********************************
* HANDLE GAME STARTED GET REQUEST
***********************************/
app.get('/gamestarted', function (req,res)
{
	console.log("Game start status requested.");
	res.send(gameStarted);
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
				gameStarted = true;
				res.send("true");
			}
			else
			{
				console.log("Failed to start game.");
				res.send("false");
			}
		});
	}
	
	console.log(gameStarted);
	
	RefreshScore(scorep1, scorep2);
});

/*****************************************************
* HANDLE ADD ITEM GET REQUEST
******************************************************/
app.post('/goal', function (req,res)
{
	var score1 = req.body.player1;
	var score2 = req.body.player2;
	
	console.log("Player 1 score: " + score1);
	console.log("Player 2 score: " + score2);
	
	RefreshScore(score1, score2);
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
			}
			else
			{
				res.send(result);
			}
		}
		else
		{
			console.log(result);
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
			}
			else
			{
				res.send(result);
			}
		}
		else
		{
			console.log(result);
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
	var inserts = [ 'OPEN', playerId, playerId ];
	var sql = 'SELECT * FROM game WHERE (State = ? OR Player1 = ? OR Player2 = ?)';
	
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

/********************************************************************
* REFRESH XML FUNCTION
*********************************************************************/
function RefreshScore(player1Score, player2Score)
{
	var scores = [];
	
	scores.push({"player1score":player1Score});
	scores.push({"player2score":player2Score});
	
	scoreJson = JSON.stringify(scores);
}