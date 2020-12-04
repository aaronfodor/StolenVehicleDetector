# Stolen Vehicle Detector Server

This is the server of the Stolen Vehicle Detector system. Clients can upload new reports, and download the current ones. The server provides an API for Android clients and manages registered users. It provides a stateless REST API, so a user needs to authenticate itself every time querying the server.

## How to run

Run the server locally with IntelliJ IDEA. Run `Application.kt/main`, then, to access the server, open *http://127.1.0.0:8080/*. To interact with the API, use the default user eg. in Postman (use the HTTP basic auth credentials - "email": "default_user@stolen_vehicle_detector", "password": "default_user_czka84").

## Architecture

The server has a three-layered architecture. Because it is responsible for API service and data storage, it does not have a separate View layer (only a simple HTML UI is available). 

Instead of the UI layer, there is the communication layer through which the API service can be accessed. Because it is a separate layer, it is more loosely coupled with other layers, reducing the chances of leaking information from other parts. User authentication is done with Http basic authentication. 

In the business logic layer, the Authenticator module checks requests and does not allow them to be executed when the required permissions are missing. The Interactor contains the main business logic. 

The data access layer contains the DAO classes responsible for handling their tables and providing a unified interface for retrieving/writing data.

![Picture3](https://user-images.githubusercontent.com/37120889/101178321-279c4f80-3649-11eb-85bd-6ae4218542c9.png)

## Database

Since I decided to use a self-created database for performance reasons, I briefly describe its main guidelines. It is a NoSQL variant with an in-memory approach. The tables store information in an object-oriented manner (instead of relational data). The table contents are in JSON format (like in the case of MongoDB). To encode/decode JSON files, the server uses the Gson library. 

There are tables of stolen vehicles, current reports, and user accounts. To these contents, there are history files(write only) as well. They store all items using a timestamp and a version number to support recovery and traceability. History tables are not stored in memory, and when a data table is updated, its corresponding history is automatically updated. There is a meta content storing size and timestamp information of the previous tables. Lastly, there is a Log table that records system logs (also write-only).

When accessing data, the database serves it from memory, making API responses fast because there is no need to wait for table I/O operations. The memory content is synchronized with the corresponding table in the background. It is a viable solution as a very large amount of data is never stored on the server (images are not uploaded). To validate this, I examined one item from the largest JSON object type (Report), which is precisely 173 bytes. Multiplied by 1 million, it turns out that the server needs 173 MB 49 memory, which is acceptable. It is a severe overestimation, though, as the stolen vehicles list obtained by web scraping typically has a few thousand items. That is the maximum number of records that the in-memory database ever has (if every stolen vehicle has been detected at once). As history content is only stored persistently, extensive API usage does not saturate the memory either.

![Picture4](https://user-images.githubusercontent.com/37120889/101178325-28cd7c80-3649-11eb-94ea-9201e67b02b3.png)

## API

The API is divided into five parts: Vehicles, Reports, Report history, Self, and Users. These names are also the corresponding API call prefixes. All parts have similar actions and a unified calling convention. All actions are subject to specific permission, which is evaluated every time before serving. There is also a status page describing the API. The following figures show the two most common server and client communication types.

![Picture1](https://user-images.githubusercontent.com/37120889/101178314-24a15f00-3649-11eb-9086-4a56f29bed29.png)
![Picture2](https://user-images.githubusercontent.com/37120889/101178318-266b2280-3649-11eb-93b6-83d3f495269c.png)

## Permission management

As the nature of the stored data (location and timestamp of stolen vehicles) could potentially allow abuses, there is strict role-based permission management in the system. Users with specific roles are eligible to execute various operations. 

There are ADMINISTRATOR, API_REGISTER, SELF_MODIFY, API_GET, and API_SEND permissions. 

- API_GET lets an authorized account to download reports. 
- API_SEND makes it possible to send recognitions to the server. 
- SELF_MODIFY is needed to prevent blacklisted users from deleting themselves and re-register. 
- An ADMINISTRATOR user can modify the server and any user’s permissions at any time. If someone’s behaviour is suspicious, an Admin can revoke permissions, delete a user, or deactivate and blacklist it. An ADMINISTRATOR can register a new user with specific permissions. 
- The default/guest user in the client application is an account with only an API_REGISTER role. This way, it is possible for newcomers to register their new accounts. If someone tries to use the application without signing in is, in fact, utilizes this user. As its only API permission is registration,  51 although someone can detect vehicles on-device, he/she cannot report them or see the actual reports. This API_REGISTER role prevents anyone outside the Android app from registering. The default user can create a new account with SELF_MODIFY, API_GET, and API_SEND permissions.
