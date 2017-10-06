# PAD LAB1


The publisher/subscriber implementation of broker is presented here. Working language is **Kotlin**. 

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
+ CustomExternalPublisher (PublisherRunner.kt)
+ CustomExternalSubscriber (SubscriberRunner.kt)

### Description of the protocol

*The communication between the server and the client (sender or receiver) is implemented by messages.*

The improved implementation of the protocol (which supports routing) is based on data-class RoutedMessage, which contains:
+ clientType (type of client: PUBLISHER, SUBSCRIBER, SERVER)
+ clientUid (unique id of client)
+ payload (data field)
+ topic (topic of message, used by broker for routing)
+ messageType (CONNECT, DISCONNECT, NORMAL, LAST_WILL, ERROR)

**Constants**

These constants are used in project:
- PORT_NUMBER = 14141 *(Number of tcp port)*
- HOST = "127.0.0.1" *(host ip for clients)*
- DEFAULT_QUEUE = "main" *(name of default queue)*
- IDLE_MESSAGE = "IDLE" *(payload of idle message)*
- CRON_INTERVAL = 5000 *(interval for cron job)*
- CRON_DELAY = 5000 *(delay before cron starts for the first time)*
- CLIENT_INTERVAL = 1000 *(interval between client reconnections)*
- PUBLISHER_INTERVAL = 15000 *(interval between publisher reconnections)*

**Subscriber details**

NewSender sends routed messages with clientType = SENDER and the selected TOPIC
ClassicSender sends messages without clientType.
*In such case broker transforms message to routed message and sends it to the default queue*

**Publisher details**

In the beginning of session publisher sends CONNECTION message. After getting success response it may define its last will sending LAST_WILL message. Last will can be modified later.

Publisher sends new NORMAL messages with pre-defined topic every PUBLISHER_INTERVAL. In case of planned disconnection it sends special DISCONNECT message.

**Broker details**

Broker contains of two big parts: PublisherPool and SubscriberPool.
The first one is implemented by separate DefaultPublisherPool, the second is implemented directly by the broker class (but it  can be easy decoupled).

*PublisherPool* manages dictionary (uid, publisher) of publishers and periodically checks registered publishers in system for death. In case of death of the publisher, its last will will be broadcasted to all subscribers with corresponding topic.

*SubscriberPool* manages composite structure of routes (every route represents a composite structure with lists of its subscribers and sub-routes).

**Persistance details**

+ All dynamically created routes are temporary. Permanent queues are created on initialization
+ Broker tries to build its permanent route ierarchy at initialization by parsing permanent.json, which contains tree of permanent routes.

**Cron details**

+ Every CRON_INTERVAL queues get signal to make its cron job, in our case this job is backup. Every route decides itself if backup is needed (special params are used: backUpItemsLimit, backUpInterval).
+ Every CRON_INTERVAL system tries to detect dead publishers and process them.

