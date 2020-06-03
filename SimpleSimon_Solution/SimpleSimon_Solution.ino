#include <Adafruit_CircuitPlayground.h>
#include <Adafruit_Circuit_Playground.h>

/////////////////////////////////////////////////////
// Circuit Playground Simple Simon
/////////////////////////////////////////////////////
//CLASS ACTIVITY LECTURE 6. May 7th 2020
////////////////////////////////////////
#include <Adafruit_CircuitPlayground.h>

//Game configuration variables
#define MAX_SEQUENCE    31 //The maximun allowed sequence lenght
#define GUESS_TIMEOUT   3000
//////////////////////////////////////


//Implementation parameters
#define NO_BUTTON       -1
#define FAILURE_TONE    100
#define SEQUENCE_DELAY  800
#define DEBOUNCE        250
#define CAP_THRESHOLD   10 //Sensitivity for the user board press

uint8_t simonSequence[MAX_SEQUENCE];
uint8_t sequenceLength;
uint8_t currentStep;
unsigned long startGuessTime;
int8_t guess;
bool game_won = false;
bool game_lost = false;


//Parameters to detect the board shake
float threshold = 6;
unsigned long shakeInterval = 3000;
float startAccelMag = 0;
unsigned long lastShakeTime = 0;


struct button {
  uint8_t capPad[2];
  uint8_t pixel[3];
  uint32_t color;
  uint16_t freq;
} simonButton[] = {
  { {3, 2},    {0, 1, 2},  0x00FF00,   415 }, // GREEN
  { {0, 1},    {2, 3, 4},  0xFFFF00,   252 }, // YELLOW
  { {12, 6},  {5, 6, 7},  0x0000FF,   209 }, // BLUE
  { {9, 10},  {7, 8, 9},  0xFF0000,   310 }, // RED
};

///////////////////////////////////////////////////////////////////////////////

float getAccelMagnitude() {
  float X = 0;
  float Y = 0;
  float Z = 0;

  float dX = 0;
  float dY = 0;
  float dZ = 0;

  for (int i = 0; i < 10; ++i) {
    dX = CircuitPlayground.motionX();
    dY = CircuitPlayground.motionY();
    dZ = CircuitPlayground.motionZ();

    X += dX;
    Y += dY;
    Z += dZ;
  }

  X /= 10;
  Y /= 10;
  Z /= 10;

  return sqrt(X * X + Y * Y + Z * Z);
}




boolean isShake() {
  float currentAccelMag = getAccelMagnitude();
  if ((currentAccelMag - startAccelMag) < threshold) {
    return false;
  }

  unsigned long now = millis();
  if ((now - lastShakeTime) < shakeInterval) {
    // don't give shake gestures too frequently
    return false;
  }

  Serial.println("Shake detected");
  lastShakeTime = now;
  return true;
}

///////////////////////////////////////////////////////////////////////////////
void newGame() {
   CircuitPlayground.clearPixels();
  CircuitPlayground.setPixelColor(0, 0xFFFFFF);

  //Get the acceleration magnitude
  startAccelMag = getAccelMagnitude();

  //Sit here until right button press or board shaked
  while (!CircuitPlayground.rightButton() ) {
    if (isShake()) {
      //Blink the neopixel pixel
      blink_new_grame(0, 3);
      break;
    } else {
      //delay(DEBOUNCE);
    }
  }
  startGame();
}

///////////////////////////////////////////////////////////////////////////////
void startGame(){
  Serial.println("START GAME NOW");
  randomSeed(millis());
  sequenceLength = 2;
  // Populate the game sequence
  for (int i = 0; i < sequenceLength; i++) {
    simonSequence[i] = random(4);
  }

  // We start with the first step in the sequence
  currentStep = 1;
}
///////////////////////////////////////////////////////////////////////////////
void blink_new_grame(int pixel, int ntimes) {
  for (int i = 0; i < ntimes; i++) {
    CircuitPlayground.setPixelColor(pixel, 0xFFFFFF);
    delay(500);
    CircuitPlayground.setPixelColor(pixel, 0x000000);
    delay(500);
  }
}



///////////////////////////////////////////////////////////////////////////////
void indicateButton(uint8_t b, uint16_t duration) {
  CircuitPlayground.clearPixels();
  for (int p = 0; p < 3; p++) {
    CircuitPlayground.setPixelColor(simonButton[b].pixel[p], simonButton[b].color);
  }
  CircuitPlayground.playTone(simonButton[b].freq, duration);
  CircuitPlayground.clearPixels();
}
///////////////////////////////////////////////////////////////////////////////
void showSequence() {

  uint16_t toneDuration = 420;

  // Play back sequence up to current step
  for (int i = 0; i < currentStep; i++) {
    delay(50);
    indicateButton(simonSequence[i], toneDuration);
  }
}

///////////////////////////////////////////////////////////////////////////////
uint8_t getUserTurn() {
  for (int b = 0; b < 4; b++) {
    for (int p = 0; p < 2; p++) {
      if (CircuitPlayground.readCap(simonButton[b].capPad[p]) > CAP_THRESHOLD) {
        indicateButton(b, DEBOUNCE);
        return b;
      }
    }
  }
  return NO_BUTTON;
}

///////////////////////////////////////////////////////////////////////////////
void gameLost() {

  for (int i = 0; i < 3; i++) {
    for (int led = 0 ; led < 10; led++) {
      CircuitPlayground.setPixelColor(led, 255, 0, 0);
    }
    delay(1000);
    CircuitPlayground.clearPixels();
    delay(1000);
  }
  newGame();

}

///////////////////////////////////////////////////////////////////////////////
void gameWon() {

  //Blink neopxels
  for (int pixel = 1; pixel < 10; pixel++) {
    CircuitPlayground.setPixelColor(pixel, CircuitPlayground.colorWheel(10 * pixel));
  }
  delay(500);
  CircuitPlayground.clearPixels();
  delay(500);
  for (int pixel = 1; pixel < 10; pixel++) {
    CircuitPlayground.setPixelColor(pixel, CircuitPlayground.colorWheel(10 * pixel));
  }
  delay(500);
CircuitPlayground.clearPixels();
  newGame();

}


///////////////////////////////////////////////////////////////////////////////
void setup() {
    Serial.begin(9600);
    
  CircuitPlayground.begin();
  CircuitPlayground.setAccelRange(LIS3DH_RANGE_8_G);
  newGame();


}

///////////////////////////////////////////////////////////////////////////////
void loop() {
  game_lost = false;
  game_won = false;
  // Show sequence up to current step
  showSequence();


  // Read player button presses
  for (int s = 0; s < currentStep; s++) {
    startGuessTime = millis();
    guess = NO_BUTTON;
    while ((millis() - startGuessTime < GUESS_TIMEOUT) && (guess == NO_BUTTON)) {
      guess = getUserTurn();

    }

    if (guess != simonSequence[s]) {
      game_lost = true;
      Serial.println("game lost");
      gameLost();
    }
  }

  if (!game_lost) {
    currentStep++;
    if (currentStep > sequenceLength) {
      delay(SEQUENCE_DELAY);
      Serial.println("game won");
      game_won = true;
      gameWon();

    }
    if (!game_won && !game_lost) {
      delay(SEQUENCE_DELAY);
    }
  }
}
