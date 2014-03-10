using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace MiddleClient
{
    class Program
    {
        static void Main(string[] args)
        {
            Client client = new Client();
            Arduino arduino = new Arduino();

            bool readyToStart = client.StartChecking();

            if (readyToStart)
            {
                Console.WriteLine("READY WOO.");
                Console.ReadLine();
                //arduino.SendStartSignal();
            }

            //arduino.ReceiveScoreData();
        }
    }
}
