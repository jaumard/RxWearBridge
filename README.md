# RxWearBridge

Small library to support data synchronisation and messages between Android and Android Wear devices with Rx interface

It's easy to use, first install the library:

```groovy
implementation 'com.jaumard:rxwearbridge:1.0.0'
```

Under your main build gradle you need to add this:

```groovy
allprojects {
    repositories {
        maven { url "http://dl.bintray.com/jaumard/maven" }
        //...other repo here
    }
}
```

## Basic usage
TODO

## Examples
This library come with a small example app, feel free to check it to see all the above cases.

## Deployment to bintray
```
./gradlew clean bintrayUpload
```

## Licence

[MIT](https://github.com/jaumard/RxWearBridge/blob/master/LICENSE)
