int outpin = 4;
volatile int flag = 1;

void setup()
{
  // Serial.begin(9600);
  attachInterrupt(0,zero_crossing,CHANGE);
  pinMode(outpin,OUTPUT);
}

void loop()
{
  if(flag == 0)
  {
    delay(2);
    digitalWrite(outpin,HIGH);
    delay(1);
    digitalWrite(outpin,LOW);
    // Serial.println("Trigger");
    flag = 1;
  }
}


void zero_crossing()
{
  digitalWrite(outpin,LOW);
  flag = 0;
}
