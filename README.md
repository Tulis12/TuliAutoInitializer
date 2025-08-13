<div align="center">
    <a href="https://jitpack.io/#Tulis12/TuliAutoInitializer" target="_blank">
        <img src="img.png" width="594" alt="@TuliAutoInitializer">
    </a>
</div>

<div align="center">
    <img alt="GitHub Created At" src="https://img.shields.io/github/created-at/Tulis12/TuliAutoInitializer">
    <img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/Tulis12/TuliAutoInitializer">
    <a href="https://jitpack.io/#Tulis12/TuliAutoInitializer" target="_blank"><img alt="Jitpack" src="https://jitpack.io/v/Tulis12/TuliAutoInitializer.svg"></a>
    <img alt="GitHub Release Date" src="https://img.shields.io/github/release-date/Tulis12/TuliAutoInitializer">
    <img alt="Made by Tulis" src="https://img.shields.io/badge/Made_by-Tulis-blue">
</div>

## What is it?
It's a Java project for initializing classes automatically, 
a little like Spring uses Beans. You can avoid a mess like this:

```java
new Command();
new Command2();

new Event();
new Event2();
```

and instead use `@Init` annotation to initialize the class (using your constructor configuration) automatically:

```java
new AutomaticInitializer.Builder()
        .setPackageName("(the main package to look for classes)")
        .addInitVariable(Integer.class, 123) // You can add as many as you want
        .addInitVariable(MyMainClass.class, mainClassObject) // Or none if you want the default constructor
        .run();
```

and for the class:

```java
@Init
public class Test {
    public Test(Integer i, MyMainClass myMainClass) {
        System.out.println("Hi from test! My int value: " + i);
        myMainClass.registerEvent(this); // You can initialize the event here for example!
    }
}
```

## Installation
Install TuliAutoInitializer by Maven or Gradle using Jitpack, or my self-hosted gitea (on git.tulisiowice.top):

### Repos:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

```xml
<repositories>
    <repository>
        <id>gitea</id>
        <url>https://git.tulisiowice.top/api/packages/Tulis/maven</url>
    </repository>
</repositories>
```

### The package:

```xml
<dependency> // Jitpack
    <groupId>com.github.Tulis12</groupId>
    <artifactId>TuliAutoInitializer</artifactId>
    <version>latest-version</version> // If you don't know it, check the Jitpack badge!
</dependency>
```

```xml
<dependency> // Gitea
	<groupId>dev.tulis</groupId>
	<artifactId>TuliAutoInitializer</artifactId>
	<version>latest-version</version>
</dependency>
```

## All annotation's parameters

### `initializeWithoutParameters = true`
Tries to use the default constructor of class (the one without parameters), defaults to false (using the configuration of the builder).

```java
@Init(initializeWithoutParameters = true)
public class Test {
    public Test() {
        System.out.println("Will use this instead!");
    }

    public Test(Integer i1, Integer i2) {
        System.out.println("Won't use this!");
    }
}
```

The Initializer won't force use of default constructor (for example, if its private), will instead throw `NoSuchMethodException`, `IllegalAccessException` respectively.

### `initializeOnlyWith = { Integer.class, MyMainClass.class }`
Will use only those two, even if there are more. In case of multiple values with the same class, will take the one first added in the builder, defaults to an empty array (will use the configuration of builder).

```java
@Init(initializeOnlyWith = { Integer.class, MyMainClass.class })
public class Test {
    public Test(Integer integer, MyMainClass myMainClass) {
        System.out.println("Will use this, even if in builder there is more.");
    }

    public Test(Integer integer, String string, MyMainClass myMainClass) {
        System.out.println("Won't use this!");
    }
}
```

Remember, if there is no class (with a value) provided in the builder, but you will try to use it here, the builder will throw `MissingParameterException`.

## License
Released under the MIT license.