## Setup
To build and run the app, you will need:
1. An Android 10 or later device with Huawei Mobile Services installed (doesn't need to be a Huawei device)
2. Android Studio
3. Huawei Developer account. You can register it for free [at the official website](https://developer.huawei.com/consumer/en/)
4. Azure Developer Account. You can register it for free [as well](https://azure.microsoft.com/en-us)
5. [OpenAI](https://openai.com) key (can be replaced by Azure OpenAI key).

### Installation steps
1. Install Android studio and open the project. You will need to set the App ID and your Huawei Developer information in the build.gradle file, you can follow the instruction [on website](https://developer.huawei.com/consumer/en/doc/development/hiai-Guides/config-agc-0000001050990353).

2. Set your OpenAI key/Azure OpenAI key and endpoint in the src/main/java/com.ClarifAI.main.sample/src/LLMInteractionModule/LLMService.java
3. Set your Azure Speech Subscription key at src/main/java/com.ClarifAI.main.sample/src/textualModule/AzureSTTService.java
4. Set your HMS API key at src/main/java/com.ClarifAI.main.sample/src/textualModule/GeocodingService.java
5. Build the app. Select EyesGPT from the Main Menu.

### Supported functionality on tested devices
The functionality is fully supported on our AR Glasses prototype. We have also tested our app on Huawei P30, Mate 30, TCL RayNeo x2, INMO Air, with some of the devices lacking support for IMU activity recognition or Reverse Geocoding services. 

### Feedback
Since the app is a prototype, we don't guarantee support of your device, however, we will be very grateful if you leave an issue describing the problem experienced. We also don't guarantee the stability of the OpenAI, Azure and HMS, but we will keep the project up-to-date with their APIs.
