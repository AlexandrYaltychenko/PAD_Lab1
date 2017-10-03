# PAD LAB1


The sender/receiver implementation of broker is presented here. Working language is **Kotlin**. 

### Build

In order to build Kotlin with Gradle you should set up the kotlin-gradle plugin, apply it to your project and add kotlin-stdlib dependencies. Those actions may also be performed automatically in IntelliJ IDEA by invoking the Tools | Kotlin | Configure Kotlin in Project action (details: https://kotlinlang.org/docs/reference/using-gradle.html) 

**Gradle repositories:**
~~~
mavenCentral()
    repositories {
        jcenter()
    }
~~~

**Gradle dependencies:**
~~~
compile "org.jetbrains.kotlin:kotlin-stdlib-jre8:$kotlin_version"
compile 'org.jetbrains.kotlinx:kotlinx-coroutines-core:0.18'
compile group: 'com.google.code.gson', name: 'gson', version: '2.7'
~~~

**Kotlin experimental**
This project uses experimental kotlin coroutines, which are in development.
Add these lines to build.gradle to hide compilation warning:
~~~
kotlin {
    experimental {
        coroutines "enable"
    }
}
~~~

### Run

You can run the following instances:
+ Broker (BrokerRunner.kt)
+ ClassicSender (SenderRunner.kt)
+ NewSender (SenderRunner.kt)
+ ClassicReceiver (ReceiverRunner.kt)
+ NewReceiver (ReceiverRunner.kt)

### Description of the protocol

*The communication between the server and the client (sender or receiver) is implemented by messages.*

The first implementation of the protocol is based on data-class Message, which contains:
+ clientType (type of client: SENDER, RECEIVER, CLIENT)
+ clientUid (unique id of client)
+ payload (data field)

The improved implementation of the protocol (which supports routing) is based on data-class RoutedMessage, which contains:
+ clientType (type of client: SENDER, RECEIVER, CLIENT)
+ clientUid (unique id of client)
+ payload (data field)
+ topic (topic of message, used by broker for routing)

**Constants**
These constants are used in project:
- PORT_NUMBER = 14141 *(Number of tcp port)*
- HOST = "127.0.0.1" *(host ip for clients)*
- DEFAULT_QUEUE = "main" *(name of default queue)*
- IDLE_MESSAGE = "IDLE" *(payload of idle message)*
- CRON_INTERVAL = 5000 *(interval for cron job)*
- CRON_DELAY = 5000 *(delay before cron starts for the first time)*
- CLIENT_INTERVAL = 1000 *(interval between client reconnections)*

**Sender details**
NewSender sends routed messages with clientType = SENDER and the selected TOPIC
ClassicSender sends messages without clientType.
*In such case broker transforms message to routed message and sends it to the default queue*

**Receiver details**
NewReceiver receives the messages with selected topic
ClassicReceiver receives the messages from default queue

**Broker details**
Processing senders:
+ If no topic defined the message is sent to default queue
+ If there is no queue corresponding to the sender's message topic, new temporary queue is created

Processing receivers:
+ If no topic defined receiver gets messages from default queue
+ If defined topic does not exist the explicit error from the server is received

**Queue and Cron details**
+ All dynamically created queues are temporary. Permanent queues are created on initialization
+ Every CRON_INTERVAL queues get signal to make its cron job, in our case this job is backup. Every queue decides itself if backup is needed (special params are used: backUpItemsLimit, backUpInterval).

