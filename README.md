##### Author: Hunter Spragg
* Version: February 2023


##### Purpose
To demonstrate using advanced Java features along with good
object-oriented design to create a simple Client-Server 
JSON protocol.
The goal of which is to Demonstrate a modular way of approaching
Java server protocols.
* Please run `gradle Server` and `gradle Client` together.

##### Usage:
By default, the server will run on port 8888. <br>
And the Client will connect to localhost on port 8888. <br>
To change the port, use the `-Pport=<int>` flag. <br>
To change the host, use the `-Phost=<string>` flag. <br>
* For example, `gradle Server -Pport=9999` will run the server on port 9999.
* For example, `gradle Client -Phost=localhost -Pport=9999` will connect to the server on port 9999.

### Protocol Specification:
The protocol is a simple JSON protocol. <br>
In order to implement the protocol, you must send
an integer containing the length of the JSON string you 
are sending. (4 bytes)<br>
Then you must send the JSON string itself.<br>
##### Client-To-Server:
```json
{
  "operation": 1
}
```
* `operation` is the operation ID you wish the server to perform. (Integer)
  * See below for the full list of Protocols.



## Supported Protocols:
* `1` - "Hypotenuse", returns the hypotenuse of a right triangle.
##### Example of Shutdown Protocol:
```json
{
  "operation": 0
}
```
* `0` - "Shutdown", shuts down the connection between the client and server.
##### Example of Hypotenuse Protocol:
#### Client-To-Server:
```json
{
    "operation": 1,
    "a": 5,
    "b": 10.5
}
```
* `operation` Represents the operation ID. (1)
* `a` Represents the first side of the triangle. (Number)
* `b` Represents the second side of the triangle. (Number)

#### Server-To-Client:
```json
{
    "operation": 1,
    "a": 5,
    "b": 10.5,
    "result": 11.629703349613008
}
```
* `operation` Represents the operation ID. (1)
* `a` Represents the first side of the triangle. (Number)
* `b` Represents the second side of the triangle. (Number)
* `result` Represents the result of the operation. (Double)


##### Error Response Format:
```json
{
    "error": -1,
    "message": "Error Message"
}
```
* `error` Represents the error code. (Integer)
* `message` Represents the error message. (String)

#### Error Codes:
* -1	- Internal Server/Client Error
  * This error is used when the server or client encounters a java error.
* 0 	- Malformed Json
  * This error is used when the server or client receives malformed JSON string.
* 1 	- Unsupported common.Operation
  * This error is used when the server or client receives an unsupported operation ID.
* 2 	- Illegal Argument Type
  * This error is used when the server or client receives an argument of the wrong type.
* 3 	- Missing Required Argument Type
  * This error is used when the server or client receives a missing argument.
