"""
FCM Android notification service.

Version: 1.0.0

Changelog:
1.0.0
    - begin versioning
"""

import json
import logging
import requests

SERVER_KEY = 'server_key'
DEFAULT_SERVER_KEY = 'AIzaSyDIGxzoJksF9b2ifmJmkuCzoMnp6YdYcX8'
ATTR_TOKEN = 'token'
FCM_POST_URL = 'https://fcm.googleapis.com/fcm/send'

ATTR_DATA = 'data'
ATTR_TITLE = 'title'
ATTR_COLOR = 'color'
ATTR_ACTION = 'action'
ATTR_ACTIONS = 'actions'
ATTR_TYPE = 'type'
ATTR_NOTIFICATION = 'notification'
ATTR_MESSAGE_TYPE = 'message_type'
ATTR_DISMISS = 'dismiss'
ATTR_TAG = 'tag'
ATTR_IMAGE = 'image'
ATTR_ICON = 'icon'

message = "hello 19"
message_type = ATTR_DATA

fcm_server_key = DEFAULT_SERVER_KEY
headers = {
    'Authorization': 'key=' + fcm_server_key,
    'Content-Type': 'application/json'
}

payload = {
    ATTR_DATA: {},
    ATTR_NOTIFICATION: {},
}

msg_payload = {
    'body': message,
    #'title': "default title",
    #'color': '#50C0F2',
    'group': 'test',
    #'tag': 12,
    'vibrate': False,
    'channel': 'ch2',
    'silent': False,
    #'dismiss': True,
}

# data = {
#
# }
#
# if data is not None:
#     if data.get(ATTR_MESSAGE_TYPE) is not None and data.get(ATTR_MESSAGE_TYPE) == 'notification':
#         message_type = ATTR_NOTIFICATION
#     if data.get(ATTR_COLOR) is not None:
#         msg_payload[ATTR_COLOR] = data.get(ATTR_COLOR)
#     if data.get(ATTR_ACTIONS) is not None:
#         msg_payload[ATTR_ACTIONS] = data.get(ATTR_ACTIONS)
#         #message_type = ATTR_DATA
#     if data.get(ATTR_IMAGE) is not None:
#         msg_payload[ATTR_IMAGE] = data.get(ATTR_IMAGE)
#         #message_type = ATTR_DATA
#     if data.get(ATTR_ICON) is not None:
#         msg_payload[ATTR_ICON] = data.get(ATTR_ICON)
#         #message_type = ATTR_DATA
#
#     if data.get(ATTR_TAG) is not None:
#         if isinstance(data.get(ATTR_TAG), int):
#             msg_payload[ATTR_TAG] = data.get(ATTR_TAG)
#             if data.get(ATTR_DISMISS) is not None:
#                 if isinstance(data.get(ATTR_DISMISS), bool):
#                     msg_payload[ATTR_DISMISS] = data.get(ATTR_DISMISS)
#                 else:
#                     _LOGGER.warning('%s is not a valid boolean, false will be used', data.get(ATTR_DISMISS))
#         else:
#             _LOGGER.warning('%s is not a valid integer, no tag will be used', data.get(ATTR_TAG))

payload[message_type] = msg_payload

targets = ['cKSiUGYEPSk:APA91bE4tuRsmC7ohgZwSTW_nZPwJCgPH5Xj3kjq2yuE7sHUsl4-sreSYop5t7DSD7aNyPrGNTggYtoDHAFg4o2PbdUsZ6sANP2G57TphhnekrgdaDz-aRfMkPSxnI3nMxYGIR-8zGFU']
target_tmp = []

for target in list(targets):
    target_tmp.append(target)

payload['registration_ids'] = target_tmp

response = requests.post(FCM_POST_URL, headers=headers,
                             json=payload, timeout=10)

print(response.status_code)
