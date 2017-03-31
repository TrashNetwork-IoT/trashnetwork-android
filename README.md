# trashnetwork-cleaning-android
The Android client of TrashNetwork project, used for cleaning job management.

## Build Properties

Before building, you should set the following properties in `gradle.properties`:

+ `TN_HTTP_API_BASE_URL_V1`: The base URL of HTTP API(v1 version) that client will use to interact with web server. The web server that this URL points to should running project [trashnetwork-web](https://github.com/TrashNetwork/trashnetwork-web).
+ `TN_MQTT_BROKER_URL`: The URL of MQTT broker that client will interact with. The MQTT broker should running project [trashnetwork-mqttbroker](https://github.com/TrashNetwork/trashnetwork-mqttbroker).
+ `TN_KEYSTORE_FILE`: The keystore file used to generate signed APK.
+ `TN_KEYSTORE_PASSWORD`: Password of `TN_KEYSTORE_FILE`.
+ `TN_KEYSTORE_ALIAS_NAME`: An alias that exist in `TN_KEYSTORE_FILE`.
+ `TN_KEYSTORE_ALIAS_PASSWORD`: Password of `TN_KEYSTORE_ALIAS_NAME`.
+ `AMAP_LBS_API_KEY`: The API key of AMap, for more details about AMap SDK, visit [http://lbs.amap.com](http://lbs.amap.com)

