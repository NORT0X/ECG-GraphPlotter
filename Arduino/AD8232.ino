#include <LiquidCrystal_I2C.h>


LiquidCrystal_I2C lcd(0x27, 2, 16);
void setup() 
{

Serial.begin(9600);
pinMode(10, INPUT); // Setup for leads off detection LO +
pinMode(11, INPUT); // Setup for leads off detection LO -
lcd.init();
lcd.backlight();
lcd.setCursor(1,0);
lcd.print("Tehnicka Skola");
lcd.setCursor(5, 1);
lcd.print("Sabac");
}

void loop() 
{

  if((digitalRead(10) == 1)||(digitalRead(11) == 1))
  {
  Serial.println('!');
  }
  else
  {
  Serial.println(analogRead(A1));
  }

//Wait for a bit to keep serial data from saturating

 if(Serial.available() > 0)
 {
  char re = Serial.read();

  switch(re)
  {
    case 'E':
    start();
    break;
  }   
  
 }
 delay(10);
}

void start()
{
  while(1)
  {

    Serial.print('s');
    
    if((digitalRead(10) == 1)||(digitalRead(11) == 1))
  {
  Serial.println('!');
  }
  else
  {
  Serial.print(floatMap(analogRead(A1),0,1023,0,5),2);
  }
    delay(20);

    if(Serial.available()>0)
    {
      if(Serial.read()== 'Q') return;
    }
  }
}
float floatMap(float x, float inMin, float inMax, float outMin, float outMax)
{
  return(x-inMin)*(outMax-outMin)/(inMax-inMin)+outMin;
}
