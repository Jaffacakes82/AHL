using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;

namespace MiddleClient
{
    public class Client
    {
        /// <summary>
        /// The constant query string to check if the game has started
        /// </summary>
        private const string query = "/gamestarted";

        /// <summary>
        /// Constant string for the default host of the server
        /// </summary>
        private const string defaultHost = "localhost";

        /// <summary>
        /// Constant string for the default port of the server
        /// </summary>
        private const string defaultPort = "3000";

        public Client()
        {
            PageUri = new Uri("http://" + defaultHost + ":" + defaultPort + query);
            ReadyToStart = false;
        }

        /// <summary>
        /// Initialises a new instance of the WebClient class
        /// </summary>
        /// <param name="host">specified host name</param>
        /// <param name="port">Specified port</param>
        public Client(string host, int port)
        {
            PageUri = new Uri("http://" + host + port + query);
            ReadyToStart = false;
        }

        /// <summary>
        /// Determines whether to start the game on the arduino
        /// </summary>
        public virtual bool ReadyToStart { get; set; }

        /// <summary>
        /// The URI to perform the get request on.
        /// </summary>
        private Uri PageUri { get; set; }

        /// <summary>
        /// Send data to the web server.
        /// </summary>
        public virtual void SendData(string data)
        {
        }

        /// <summary>
        /// Triggers the task that sends a get request to the server to determine whether the game is ready to start
        /// </summary>
        /// <returns></returns>
        public virtual bool StartChecking()
        {
            while (!this.ReadyToStart)
            {
                Task checkStartGame = new Task(CheckGameHasStarted);
                checkStartGame.Start();
                checkStartGame.Wait();
            }

            return this.ReadyToStart;
        }

        /// <summary>
        /// Receive data from the web server to be sent to the arduino
        /// </summary>
        public void CheckGameHasStarted()
        {
            using (HttpClient client = new HttpClient())
            using (HttpResponseMessage response = client.GetAsync(PageUri).Result)
            using (HttpContent content = response.Content)
            {
                string result = content.ReadAsStringAsync().Result;

                if (!string.IsNullOrEmpty(result))
                {
                    this.ReadyToStart = bool.Parse(result);
                }
            }
        }
    }
}
