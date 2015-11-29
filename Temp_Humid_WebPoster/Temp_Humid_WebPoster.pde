import processing.serial.*;
import http.requests.*;

private DHT2023 sensor = new DHT2023(0.0,0.0);
private char[] dataArray = new char[100];
private boolean flag = false;

private int delayTime = 10; //seconds (1000ms delay on Arduino) 
private int idx = 0;
private int delayCounter = 10;


void setup()
{
  printArray(Serial.list());
  sensor.setCOM(new Serial(this, Serial.list()[5],9600));
}

void draw()
{
  
  if(sensor.getCOM().available() > 0)
  {
    int data = sensor.getCOM().read();
    dataArray[idx] = char(data);
    idx++;
    if(char(data)=='\n')
    {
      flag = true;
      idx = 0;
    }
    
  }
  
  if(flag)
  {
    String convArray = String.valueOf(dataArray);
    String test[] = convArray.split("H:");
    String fanState = "0";
    //println(test[0]);
    if(test.length > 1)
    {
      sensor.humidity = Float.parseFloat(test[1].split("F:")[0]);
      sensor.temperature = Float.parseFloat((test[1].split("F:")[1]).split("fan:")[0]);
      fanState = ((test[1].split("fan:")[1]).split(";")[0]);
    }
    delayCounter++;
    if(delayCounter >= delayTime)
    { 
      int m = minute();  // Values from 0 - 59
      int h = hour();    // Values from 0 - 23
      
      String request = "http://ph1a5h.asuscomm.com/postTest.php?humid="+String.valueOf(sensor.humidity)+"&temp="+String.valueOf(sensor.temperature)+"&fan="+fanState;
      println(h+":"+m+"[REQUEST]:"+request);
      GetRequest get = new GetRequest(request);
      get.send();
      delayCounter = 0;
    }
    flag = false;
  }
}