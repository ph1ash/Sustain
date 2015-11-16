

class DHT2023
{
  private float humidity;
  private float temperature;
  private Serial comLine;
  public DHT2023(float h, float f)
  {
    this.humidity = h;
    this.temperature = f;
  }
  
  public float getTemperature()
  {
    return this.temperature;
  }
  
  public float getHumidity()
  {
    return this.humidity;
  }
  
  public void setTemperature(float f)
  {
    this.temperature = f;
  }
  
  public void setHumidity(float h)
  {
    this.humidity = h;
  }
  
  public void setCOM(Serial com)
  {
    this.comLine = com;
  }
  
  public Serial getCOM()
  {
    return this.comLine;
  }
}