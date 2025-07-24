package dev.tulis.autoinitializer;

import dev.tulis.autoinitializer.Annotations.GenericInitializer;
import dev.tulis.autoinitializer.Annotations.InitializeCommand;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AutomaticInitializer {
    String packageName;
    Class<?> mainClass;
    Object mainClassObject;
    private final Logger logger = Logger.getLogger("dev.tulis.autoinitializer");
    private boolean loggerEnabled = true;

    public AutomaticInitializer(String packageName, Class<?> mainClass, Object mainClassObject) {
        this.packageName = packageName;
        this.mainClass = mainClass;
        this.mainClassObject = mainClassObject;

        Logger.getLogger("org.reflections").setLevel(Level.OFF);
    }

    private <T> T castObject(Object obj, Class<T> clazz) {
        return clazz.cast(obj);
    }

    public void setLoggerEnabled(boolean enabled) {
        loggerEnabled = enabled;
        if(!enabled) logger.setLevel(Level.OFF); else logger.setLevel(Level.WARNING);
    }

    public boolean isLoggerEnabled() {
        return loggerEnabled;
    }

    public void initializeCommands() {
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> autoInitClasses = reflections.getTypesAnnotatedWith(InitializeCommand.class);

        for (Class<?> clazz : autoInitClasses) {
            try {
                Constructor<?> ctor = clazz.getDeclaredConstructor(mainClass);
                ctor.newInstance(castObject(mainClassObject, mainClass));

                InitializeCommand annotation = clazz.getAnnotation(InitializeCommand.class);

                logger.info("Zainicjalizowano komendę: " + annotation.commandName());
            } catch (Exception e) {
                logger.warning(e.getMessage());
            }
        }
    }

    public void initializeGenerics() {
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> autoInitClasses = reflections.getTypesAnnotatedWith(GenericInitializer.class);

        for (Class<?> clazz : autoInitClasses) {
            try {
                Constructor<?> ctor = clazz.getDeclaredConstructor(mainClass);
                ctor.newInstance(castObject(mainClassObject, mainClass));

                logger.info("Zainicjalizowano klasę ogólną: " + clazz.getSimpleName());
            } catch (Exception e) {
                logger.warning(e.getMessage());
            }
        }
    }
}