/********************************************************************/
var express = require('express');
var xml = require('xmlbuilder');
var mysql = require('mysql');
/********************************************************************/
/********************************************************************/
var app = express();
app.set('title', 'Air Hockey - Live!');
app.use(express.json());
app.listen(3000);
/********************************************************************/
/********************************************************************/
var db_config = {
  host: 'localhost',
    user: 'root',
    password: '',
    database: 'airhockeylive'
};

var connection = mysql.createConnection(db_config);
/********************************************************************/
/********************************************************************/
var lines = process.stdout.getWindowSize()[1];

for(var i = 0; i < lines; i++) {
    console.log('\r\n');
}
/********************************************************************/
/********************************************************************/
console.log("-------------------------------------");
console.log("Air Hockey - Live! Server. Port: 3000");
/********************************************************************/
/********************************************************************/
var date = new Date();
var feed;
var gameStarted = false;
/********************************************************************
* BASE 
*********************************************************************/
app.get('/', function(req, res)
{
	res.send("Air Hockey - Live!");
});

/********************************************************************
* HANDLE GAME STARTED GET REQUEST
*********************************************************************/
app.get('/gamestarted', function (req,res)
{
	res.send(gameStarted);
});

/********************************************************************
* HANDLE START GAME GET REQUEST
*********************************************************************/
app.get('/startgame', function(req, res)
{
	gameStarted = true;
	InitiateGame();
	RefreshRSS(res, 0, 0);
});

/********************************************************************
* HANDLE ADD ITEM GET REQUEST (THIS SHOULD BE A POST)
*********************************************************************/
app.get('/goal', function (req,res)
{
	var score1 = req.query.player1score;
	var score2 = req.query.player2score;
	
	console.log("Player 1 score: " + score1);
	console.log("Player 2 score: " + score2);
	
	RefreshRSS(res, score1, score2);
});


/********************************************************************
* HANDLE RSS GET REQUEST
*********************************************************************/
app.get('/game', function(req,res)
{
	if (feed == null)
	{
		res.send("Game not started.");
	}
	else
	{
		res.send(feed);
	}
});

/********************************************************************
* HANDLE REGISTER POST REQUEST
*********************************************************************/
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

/************************************************
* GET LIST OF AVAILABLE GAMES					*
************************************************/
app.get('/gameslist', function (req,res)
{
	console.log("Getting games list.");
	
	FetchGamesList(function (success, result)
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

/********************************************************************
* HANDLE LOGIN POST REQUEST
*********************************************************************/
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

/****************************************
* FETCH GAMES LIST FUNCTION
*****************************************/
function FetchGamesList(callback)
{
	var sql = 'SELECT * FROM game WHERE (State = \'OPEN\')'
	
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
* REFRESH RSS FUNCTION
*********************************************************************/
function RefreshRSS(response, player1Score, player2Score)
{
	feed = xml.create('game');
	
	feed.ele('player1').ele('score', player1Score);
	feed.ele('player2').ele('score', player2Score);
		
	feed = feed.end({ pretty: true, indent: '\t' , newline: '\n' });
	
	response.send(feed);
}	


/********************************************************************
* INITIATE GAME FUNCTION
*********************************************************************/
function InitiateGame()
{
	gameStarted = true;
}
