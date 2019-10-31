// Copyright (c) Microsoft. All rights reserved.

using System;
using System.Diagnostics;
using System.Net.Http;
using System.Threading.Tasks;
using Windows.Devices.Bluetooth.Advertisement;
using Windows.Devices.Gpio;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Media;

namespace Blinky
{
    public sealed partial class MainPage : Page
    {
        private const int LED_PIN = 5;
        private GpioPin pin;
        private GpioPinValue pinValue;
        private DispatcherTimer timer;
        private SolidColorBrush redBrush = new SolidColorBrush(Windows.UI.Colors.Red);
        private SolidColorBrush grayBrush = new SolidColorBrush(Windows.UI.Colors.LightGray);
        private BluetoothLEAdvertisementWatcher watcher;

        public MainPage()
        {
            this.watcher = new BluetoothLEAdvertisementWatcher();
            InitializeComponent();

            timer = new DispatcherTimer();
            timer.Interval = TimeSpan.FromMilliseconds(500);
            timer.Tick += Timer_Tick;
            InitGPIO();
            if (pin != null)
            {
                timer.Start();
            }
            watcher.Received += Watcher_Received;
            watcher.Start();
        }
        private async void Watcher_Received(BluetoothLEAdvertisementWatcher sender, BluetoothLEAdvertisementReceivedEventArgs args)
        {
            Debug.WriteLine("Watcher_Received");
        }

        private void InitGPIO()
        {
            var gpio = GpioController.GetDefault();

            // Show an error if there is no GPIO controller
            if (gpio == null)
            {
                pin = null;
                GpioStatus.Text = "There is no GPIO controller on this device.";
                return;
            }

            pin = gpio.OpenPin(LED_PIN);
            pinValue = GpioPinValue.High;
            pin.Write(pinValue);
            pin.SetDriveMode(GpioPinDriveMode.Output);

            GpioStatus.Text = "GPIO pin initialized correctly.";

        }

        const string WebAPIURL = "http://adafruitsample.azurewebsites.net/TimeApi";
        public async Task<int> GetBlinkDelayFromWeb()
        {
            Debug.WriteLine("InternetLed::MakeWebApiCall");

            string responseString = "No response";

            try
            {
                using (HttpClient client = new HttpClient())
                {
                    // Make the call
                    responseString = await client.GetStringAsync(WebAPIURL);

                    // Let us know what the returned string was
                    Debug.WriteLine(String.Format("Response string: [{0}]", responseString));
                }
            }
            catch (Exception e)
            {
                Debug.WriteLine(e.Message);
            }

            int delay;

            if (!int.TryParse(responseString, out delay))
            {
                delay = 1000;
            }

            // return the blink delay
            return delay;
        }




        private void Timer_Tick(object sender, object e)
        {
            if (pinValue == GpioPinValue.High)
            {
                pinValue = GpioPinValue.Low;
                pin.Write(pinValue);
                LED.Fill = redBrush;
            }
            else
            {
                pinValue = GpioPinValue.High;
                pin.Write(pinValue);
                LED.Fill = grayBrush;
            }
        }
             

    }
}
