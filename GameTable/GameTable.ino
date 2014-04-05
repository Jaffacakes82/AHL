#define player1DetectorPin 2 // Player 1 IR Sensor on this pin
#define player1LedPin 7 // Player 1 IR LED on this pin
//#define player1DetectorPin 2 // Player 2 IR Sensor on this pin
//#define player1LedPin 7 // Player 2 IR LED on this pin
#define serverPort 3000 // Server port

/* Includes */
#include <SPI.h>
#include <Ethernet.h>

/* Enum used to determine the goal scorer */
enum Player
{ 
  unknown,
  player1,
  player2
};

/* Global Variables */
byte mac[] = { 0x90, 0xA2, 0xDA, 0x0D, 0x0E, 0x96 }; // MAC Address of the ethernet shield.
IPAddress server(192,168,0,3); // IP address of the server as it does not have a fully qualified domain name
IPAddress ip(192,168,1,177); // Set the static IP address to use if the DHCP fails to assign
int player1Score = 0; // Player 1 goal score
int player2Score = 0; // Player 2 goal score
EthernetClient client; // ethernet client object
boolean gameStarted = false; // Used to determine whether to run the sensors or not.
String gameId; // The Game ID of the game in progress

/* Function signatures */
int ReadDetector(int readPin, int triggerPin);
void CheckGameState();
void PostScore(Player goalScorer);

/* Initial set up */
void setup()
{
  //Set the player 1 IR detector to input mode
  pinMode(player1DetectorPin, INPUT);
  
  //Set the player 1 IR led to output mode
  pinMode(player1LedPin, OUTPUT);
  
  //Enable serial port with baud rate 9600
  Serial.begin(9600);
  
  // start the Ethernet connection:
  Serial.println("Attempting to enable ethernet port.");
  
  if (Ethernet.begin(mac) == 0) 
  {
    Serial.println("Failed to configure Ethernet using DHCP");
    // no point in carrying on, so do nothing forevermore:
    // try to congifure using IP address instead of DHCP:
    Ethernet.begin(mac, ip);
  }
  else
  {
    Serial.println("Ethernet enabled.");
  }
  
  //Establish client connection
  Serial.println("Attempting to connect to server..");
 
  if (client.connect(server, serverPort))
  {
    Serial.println("Connected to server.");
  }
  else
  {
    Serial.println("Connection failed");
  }
  
  //Small delay to ensure everything is ready before we begin main loop
  delay(100);
}

/* Main loop */
void loop()
{
  Player goalScorer = unknown;
  
  /* While the game isn't started keep checking the server. */
  while (!gameStarted)
  {
    CheckGameState();
    delay(3000);
  }
  
  /* While the game is started, we want to read our infrared sensors */
  while (gameStarted)
  {
    if (ReadDetector(player1DetectorPin, player1LedPin) == 0)
    {
      Serial.println("Infrared Detected.");
    }
    else
    {
      goalScorer = player1;
      PostScore(player1);
      Serial.println("NOTHING 1.");
      delay(10000);
    }
  }
}

/*************************************************************
* PostScore()
*
* Parameters:
* goalScorer - an enum value to determine who scored the goal
* so we can increment the correct score.
*************************************************************/
void PostScore(Player goalScorer)
{
  /* Determine whos score to increment. */
  if (goalScorer == player1)
  {
    player1Score++;
  }
  else 
  {
    player2Score++;
  }
  
  /* if the client isnt connected, stop all client activity and reconnect. */
  if (!client.connected())
  {
    client.stop();
    
    if (client.connect(server, serverPort))
    {
      Serial.println("Connected to server.");
    }
    else
    {
      Serial.println("Failed to connect to server.");
    }
  }
  
  /* Build our JSON object to post to the server. */
  String postData = "{\n\t\"player1\":\"" + String(player1Score) + "\",\n\t\"player2\":\"" + String(player2Score) + "\",\n\t\"gameId\":\"" + gameId + "\"\n}";
  
  /* Build the POST request and write it to the server. */
  client.println("POST /goal HTTP/1.1");
  client.println("Content-Type: application/json");
  client.print("Content-Length: ");
  client.println(postData.length());
  client.println();
  client.println(postData);
  
  /* If there's anything back from the server, read it char by char and print it out. */
  while (client.available() > 0)
  {
    char responseChar = client.read();
    Serial.print(responseChar);
  }
  
  /* If a player has won the game, set the scores back to 0 and set the gameStarted flag to false and erase the gameId */  
  if (player1Score == 7 || player2Score == 7)
  {
    player1Score = 0;
    player2Score = 0;
    gameStarted = false;
    gameId = "";
  }
}

/******************************************
* CheckGameState()
*
* Checks with the server whether a game has
* been started.
*******************************************/
void CheckGameState()
{
  String response;
  int indexWhereToCreateSubString = 0;
  
  if (!client.connected()) // if the client is not connected
  {
    client.stop(); // Stop all client activity
    
    if (client.connect(server, serverPort)) // Connect to the ahlserver using the provided IP and PORT.
    {
      Serial.println("Connected to server.");
    }
    else
    {
      Serial.println("Failed to connect to server.");
    }
  }
  
  /* Build the GET request that checks whether a game has been started. */
  client.println("GET /gamestarted HTTP/1.1");
  client.println("Connection: close");
  client.println();
  
  /* If there is data available from the server, read character by character and append to response string. */
  while (client.available() > 0)
  {
    char responseChar = client.read();
    response += responseChar;
  }
  
  /* The content of the response will contain a string preceded by "ID:", get the index at this location and create a substing from that index*/
  indexWhereToCreateSubString = response.indexOf("ID:") + 3;
  gameId = response.substring(indexWhereToCreateSubString); // This is the ID of the game that matches the record in the database, used in post requests.
  
  // Sometimes we get a blank response from the server.
  if (gameId != "")
  {
    // If the response is not equal to false, a game has been started. Set the gameStarted bool to true
    if (gameId != "false")
    {
      gameStarted = true;
      Serial.println(gameId);
      Serial.println("Game Started.");
    }
    else
    {
      Serial.println("Response was false.");
    }
  }
  else
  {
    Serial.println("Blank response.");
  }
}

/******************************************************
* ReadDetector()
*
* Returns either a 1 or a 0 depending if infrared
* is detected or not, this is a code sample provided
* by the Arduino community. (Arduino Playground, 2014) http://playground.arduino.cc/Main/PanasonicIrSensor#.U0BLzfldWSo
******************************************************/
int ReadDetector(int readPin, int triggerPin)
{
  int halfPeriod = 13; // One period at 38.5KHz is approx 26 microseconds
  int cycles = 38; // 26 Microseconds * 38 is more or less 1 millisecond
  int i;
  
  for (i = 0; i <= cycles; i++)
  {
    digitalWrite(triggerPin, HIGH);
    delayMicroseconds(halfPeriod);
    digitalWrite(triggerPin, LOW);
    delayMicroseconds(halfPeriod - 1);
  }
  
  return digitalRead(readPin);
}
