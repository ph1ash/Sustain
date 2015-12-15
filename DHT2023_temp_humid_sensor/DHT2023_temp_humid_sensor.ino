// Example testing sketch for various DHT humidity/temperature sensors
// Written by ladyada, public domain

#include "DHT.h"

#define DHTPIN 2     // what digital pin we're connected to

// Uncomment whatever type you're using!
//#define DHTTYPE DHT11   // DHT 11
#define DHTTYPE DHT22   // DHT 22  (AM2302), AM2321
//#define DHTTYPE DHT21   // DHT 21 (AM2301)

// Connect pin 1 (on the left) of the sensor to +5V
// NOTE: If using a board with 3.3V logic like an Arduino Due connect pin 1
// to 3.3V instead of 5V!
// Connect pin 2 of the sensor to whatever your DHTPIN is
// Connect pin 4 (on the right) of the sensor to GROUND
// Connect a 10K resistor from pin 2 (data) to pin 1 (power) of the sensor

// Initialize DHT sensor.
// Note that older versions of this library took an optional third parameter to
// tweak the timings for faster processors.  This parameter is no longer needed
// as the current DHT reading algorithm adjusts itself to work on faster procs.
DHT dht(DHTPIN, DHTTYPE);

int VH400 = 0; //Analog 
int MotorControl = 5;
byte motorState = 0;
float f;
float h;
float currentVwc;

void setup() {
  Serial.begin(9600);
  //Serial.println("DHTxx test!");

  dht.begin();
  pinMode(MotorControl, OUTPUT);
}

void loop() {
  // Wait a few seconds between measurements.

  delay(1000);
  
  // Read from DHT22
  readDHT22();
  
  // Read from VH400
  readVH400();
  
  // Compute heat index in Fahrenheit (the default)
  //float hif = dht.computeHeatIndex(f, h);
  // Compute heat index in Celsius (isFahreheit = false)
  //float hic = dht.computeHeatIndex(t, h, false);

  Serial.print("H:");
  Serial.print(h);
  //Serial.print(";");
  Serial.print("F:");
  Serial.print(f);
  Serial.print("fan:");
  Serial.print(motorState);
  Serial.println(";");
  /*Serial.print(" *C ");
   Serial.print(f);
   Serial.print(" *F\t");
   Serial.print("Heat index: ");
   Serial.print(hic);
   Serial.print(" *C ");
   Serial.print(hif);
   Serial.println(" *F");*/
}

void readDHT22()
{
  // Reading temperature or humidity takes about 250 milliseconds!
  // Sensor readings may also be up to 2 seconds 'old' (its a very slow sensor)
  h = dht.readHumidity();
  // Read temperature as Celsius (the default)
  //float t = dht.readTemperature();
  // Read temperature as Fahrenheit (isFahrenheit = true)
  f = dht.readTemperature(true); 

  if(f >= 70.0)
  {
    digitalWrite(MotorControl, HIGH);
    motorState = 1;
  }
  else
  {
    digitalWrite(MotorControl, LOW);
    motorState = 0;
  }

  // Check if any reads failed and exit early (to try again).
  if (isnan(h) || isnan(f) || isnan(f)) {
    Serial.println("Failed to read from DHT sensor!");
    return;
  }
}

float readVH400()
{
  float readIn = analogRead(VH400);
  Serial.print("Read In Value of VH400: ");
  Serial.println(readIn);
  readIn *= (0.00488f); // 5/1024 - doing the calculation here so the chip doesn't have to
  float vwc = 0;  
  
  if(readIn <= 1.1f)
  {
    vwc = (10 * readIn) - 1;
  }
  else if(readIn <= 1.3f)
  {
    vwc = (25 * readIn) - 17.5;
  }
  else if(readIn <= 1.82f)
  {
    vwc = (48.08 * readIn) - 47.5;
  }
  else if(readIn <= 2.2f)
  {
    vwc = (26.32 * readIn) - 7.89;
  }
  return vwc;
}

