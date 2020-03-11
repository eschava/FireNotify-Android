# FireNotify-Android
Android client for push notifications through Firebase service (FCM)

# Screenshots

![Main application screen](https://raw.githubusercontent.com/eschava/FireNotify-Android/master/screenshots/screenshot2_censored.jpg "Main application screen")
![Sample notification](https://raw.githubusercontent.com/eschava/FireNotify-Android/master/screenshots/screenshot1.jpg "Sample notification")


# API

API is based on the Firebase JSON API

Example (for Python):

```python
import requests

headers = {
    'Authorization': 'key=' + AUTH_KEY,
    'Content-Type': 'application/json'
}

payload = {
    'registration_ids': [CLIENT_TOKEN_ID],
    'data':  {
        'text':  "Test Notification",
        'title': "Test"
    },
}

response = requests.post('https://fcm.googleapis.com/fcm/send', headers=headers,
                         json=payload, timeout=10)
                         
print(response.status_code)
```

# Notification keys
(keys of data parameter):
 - *text* - text of the notification
 - *title* - title
 - *group* - name of the group if notifications have to be grouped
 - *id* - numeric or string identifier of notification, not required. Notification having ID replaces another notification having the same ID
 - *dismiss* - only if ID is specified. Use "true" to remove previous notification having the same ID
 - *silent* - "true" if notification should be silent
 - *time* - UNIX timestamp with time when event happened. Is set to current time by default
 - *showTime* - "false" to hide time
 - *local* - "false" to do not transfer notification to wearable devices
 - *subText* - additional notification text  
 - *color* - RGB color (e.g. "#50C0F2")
 - *colorized* - specifies whether notification should be colorized (true/false)
 - *persistent* - "true" for notifications which cannot be closed by user
 - *image* - URL of the custom image
 - *icon* - URL of the custom image
 - *actions* - optional array of actions data (up to 4 elements). Each action could contain the next parameters:
      - *title* - text for action button
      - *dismiss* - "true" if executing action removes the notification
      - *data* - JSON object that is sent as callback message data when action is executed
      - *to* - FCM token of the client for sending callback message
 
