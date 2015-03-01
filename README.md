# pimatic-location

To use this app you need to install the Plugin "pimatic-location".
You need to add a device to your Pimatic config for each smartphone that should report it's location.

Plugin:

Simply add 
```
    {
      "plugin": "location"
    },
```
to your plugin configuration.

For each Device (Smartphone) you want to 'track', you need to create a corresponding device in your config.
Example:
```
    {
      "id": "your-phone",
      "name": "your-phone",
      "class": "LocationDevice",
      "lat": "52.5200066",
      "long": "13.404954"
    },
```
The lat and long values correspond to the location you want the distance to be calculated.
You can use this website to get your longitude and latitude.
http://www.mapcoordinates.net/en

# Configuring the Application
Following Options are available:

- Host: The IP or Hostname of your Pimatic.
- Protocol: The Protocol to use to connect to Pimatic. (Default: http)
- Port: The Port to use to connect to Pimatic. (Default: 80)
- User: The Pimatic-User to use to connect to your Pimatic. (Default: Admin)
- Password: The Password corresponding to the Pimatic-User.
- Device-ID: The id of the Device you created for this Smartphone.
- Interval: The interval of location updates in milliseconds at which you prefer to receive location updates. (Default: 600000 (10 Minutes))
- Interval Limit: The lower limit of location updates in milliseconds you want to receive. (Default: 60000 (1 Minute))
- Priority: The priority of the location updates you want to receive. (Default: Balanced Power)
  * Balanced Power: Precision to within a city block, which is an accuracy of approximately 100 meters. With this setting, the location services are likely to use   * WiFi and cell tower positioning.
  * High: Request the most precise location possible. With this setting, the location services are more likely to use GPS.
  * Low: Use this setting to request city-level precision, which is an accuracy of approximately 10 kilometers.
  * No: Use this setting if you need negligible impact on power consumption, but want to receive location updates when available. With this setting, the app does not trigger any location updates, but receives locations triggered by other apps.

(Source: https://developer.android.com/training/location/receive-location-updates.html#location-request)
- Auto refresh: Starts the service that updates the smartphones location in background, based on your settings, to your Pimatic. (Default: On)
- Write logfile: Write to a logfile informations about location updates and service activity. (Default: Off)
- Report current address: Should your current address be shown in pimatic?
- Test & Save: Test and Save your Settings. Starts the location-Service if auto refresh is on.

# Tasker integration

You can use the Android App "Tasker" to send an intent to the application to set a new interval or a new priority.

Tasker example intent config for interval:
```
Send Intent [ 
 Action:android.intent.action.SEND 
 Cat:Default 
 Mime Type:text/plain 
 Data: 
 Extra:android.intent.extra.INTERVAL: 600000
 Extra: 
 Package:de.blackoise.pimaticlocation
 Class: 
 Target:Service ]
```

Tasker example intent config for interval limit:
```
Send Intent [ 
 Action:android.intent.action.SEND 
 Cat:Default 
 Mime Type:text/plain 
 Data: 
 Extra:android.intent.extra.INTERVALLIMIT: 600000
 Extra: 
 Package:de.blackoise.pimaticlocation
 Class: 
 Target:Service ]
```

Tasker example intent config for priority:
```
Send Intent [ 
 Action:android.intent.action.SEND 
 Cat:Default 
 Mime Type:text/plain 
 Data: 
 Extra:android.intent.extra.PRIORITY: 0
 Extra: 
 Package:de.blackoise.pimaticlocation
 Class: 
 Target:Service ]
```

Priorities:
0 = Balanced Power
1 = High
2 = Low
3 = No

# Screenshots
![Settings Window](https://raw.githubusercontent.com/Oitzu/pimatic-location/master/Android/screenshots/2015-02-06%2012.05.59.png)