#define player1DetectorPin 2 // Player 1 IR Sensor on this pin
#define player1LedPin 7 // Player 1 IR LED on this pin
//#define player1DetectorPin 2 // Player 2 IR Sensor on this pin
//#define player1LedPin 7 // Player 2 IR LED on this pin
#define serverPort 3000 // Server port

/* Includs */
#include <SPI.h>
#include <Ethernet.h>

/* Enum used to determine the goal scorer */
enum Player
{
  unknown,
  player1,
  player2
};

/* Globals */
byte mac[] = { 0x90, 0xA2, 0xDA, 0x0D, 0x0E, 0x96 }; // MAC Address of the ethernet shield.
IPAddress server(192,168,0,6); // IP address of the server as it does not have a fully qualified domain name
IPAddress ip(192,168,1,177); // Set the static IP address to use if the DHCP fails to assign
int player1Score = 0; // Player 1 goal score
int player2Score = 0; // Player 2 goal score
EthernetClient client; // ethernet client object
boolean gameStarted = false;

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
  Serial.println();
  
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
  Serial.println();
  
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
  
  while (!gameStarted)
  {
    Serial.println("No game currently started.");
    CheckGameState();
    delay(5000);
  }
  
  while (gameStarted)
  {
    if (ReadDetector(player1DetectorPin, player1LedPin) == 0)
    {
      goalScorer = player1;
      PostScore(player1);
      Serial.println("DETECTED 1.");
      delay(5000);
    }
    else
    {
      Serial.println("NOTHING 1.");
    }
  }
}

void PostScore(Player goalScorer)
{
  if (goalScorer == player1)
  {
    player1Score++;
  }
  else 
  {
    player2Score++;
  }
  
  String postData = "{\n\t\"player1\":\"" + String(player1Score) + "\",\n\t\"player2\":\"" + String(player2Score) + "\"\n}";
  
  client.println("POST /goal HTTP/1.1");
  client.println("Content-Type: application/json");
  client.print("Content-Length: ");
  client.println(postData.length());
  client.println();
  client.println(postData);
  
  if (player1Score == 7 || player2Score == 7)
  {
    player1Score = 0;
    player2Score = 0;
    gameStarted = false;
  }
}

/* Checks whether a game of air hockey live has been started by requesting a flag from the web server. */
void CheckGameState()
{
  String response;
  
  Serial.println("Creating HTTP request..");
  Serial.println();
  
  client.println("GET /gamestarted HTTP/1.1");
  client.println();
  
  while (client.available() > 0)
  {
    char responseChar = client.read();
    response += responseChar;
  }
  
  if (response.endsWith("true"))
  {
    gameStarted = true;
    Serial.println("Game started.");
  }
}

/* Reads the value from the infrared detector */
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