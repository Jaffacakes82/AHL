using System;
using System.Collections.Generic;
using System.IO.Ports;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace MiddleClient
{
    public class Arduino
    {
        /// <summary>
        /// Serial port
        /// </summary>
        private string SerialPort { get; set; }

        /// <summary>
        /// Baud rate
        /// </summary>
        private int BaudRate { get; set; }

        /// <summary>
        /// Create an instance of Arduino class with hard coded values
        /// </summary>
        public Arduino()
        {
            this.SerialPort = "COM13";
            this.BaudRate = 9600;
        }

        /// <summary>
        /// Create an instance of Arduino class, not hard coded
        /// </summary>
        /// <param name="serialPort">custom serial port</param>
        /// <param name="baudRate">custom baud rate</param>
        public Arduino(string serialPort, int baudRate)
        {
            this.SerialPort = "COM13";
            this.BaudRate = 9600;
        }

        /// <summary>
        /// Sends start signal to arduino to start measuring infrared stuff
        /// </summary>
        public virtual void SendStartSignal()
        {
            SerialPort arduinoPort = new SerialPort(this.SerialPort);
            arduinoPort.BaudRate = this.BaudRate;
            arduinoPort.Open();

            if (arduinoPort.IsOpen)
            {
                arduinoPort.Write("Start");
            }

            arduinoPort.Close();
        }

        /// <summary>
        /// Receives score data.
        /// </summary>
        public virtual void ReceiveScoreData()
        {
            SerialPort arduinoPort = new SerialPort(this.SerialPort);
            arduinoPort.BaudRate = this.BaudRate;
            arduinoPort.Open();

            while (arduinoPort.IsOpen)
            {
                Console.WriteLine(arduinoPort.ReadLine());
            }
        }
    }
}
