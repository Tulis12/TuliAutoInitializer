package dev.tulis.autoinitializer;

import dev.tulis.autoinitializer.Annotations.Init;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class AutomaticInitializer {
    private final String packageName;
    private final LinkedHashMap<Class<?>, List<Object>> classes;
    private final Logger log = LoggerFactory.getLogger(AutomaticInitializer.class);

    private AutomaticInitializer(Builder builder) throws NoSuchMethodException, IllegalAccessException, RuntimeException {
        packageName = builder.getPackageName();
        classes = builder.getClasses();
        System.setProperty("org.slf4j.simpleLogger.log.org.reflections", "off");

        init();
    }

    private <T> T castObject(Object obj, Class<T> clazz) {
        return clazz.cast(obj);
    }

    public void init() throws NoSuchMethodException, IllegalAccessException, RuntimeException {
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> autoInitClasses = reflections.getTypesAnnotatedWith(Init.class);

        List<Class<?>> paramTypes = new ArrayList<>();
        List<Object> objects = new ArrayList<>();

        for(Map.Entry<Class<?>, List<Object>> entry : classes.entrySet()) {
            for(Object object : entry.getValue()) {
                paramTypes.add(entry.getKey());
                objects.add(castObject(object, entry.getKey()));
            }
        }

        for (Class<?> clazz : autoInitClasses) {
            Init init = clazz.getAnnotation(Init.class);

            if(init.initializeOnlyWith().length != 0) {
                List<Class<?>> paramTypesLocal = new ArrayList<>();
                List<Object> objectsLocal = new ArrayList<>();

                for(Class<?> initClazz : init.initializeOnlyWith()) {
                    if(!classes.containsKey(initClazz)) {
                        throw new MissingParameterException(
                                String.format(
                                        "You didn't provide a value for: %s (requested by: %s).",
                                        initClazz.getName(),
                                        clazz.getName()
                                )
                        );
                    }

                    for(Object object : classes.get(initClazz)) {
                        paramTypesLocal.add(initClazz);
                        objectsLocal.add(castObject(object, initClazz));
                    }
                }

                try {
                    Constructor<?> ctor = clazz.getDeclaredConstructor(
                            paramTypesLocal.toArray(new Class<?>[0])
                    );

                    ctor.newInstance(objectsLocal.toArray());

                    log.debug(
                            "Initialized: {}, but only with: {}.",
                            clazz.getSimpleName(),
                            Arrays.toString(paramTypesLocal.toArray())
                    );
                } catch (Exception e) {
                    log.warn(e.getMessage());
                }

                continue;
            }

            if(init.initializeWithoutParameters()) {
                try {
                    Constructor<?> ctor = clazz.getConstructor();
                    ctor.newInstance();
                } catch (Exception e) {
                    log.warn(e.getMessage());
                }

                continue;
            }

            try {
                Constructor<?> ctor = clazz.getDeclaredConstructor(paramTypes.toArray(new Class<?>[0]));
                ctor.newInstance(objects.toArray());

                log.debug("Initialized: {}", clazz.getSimpleName());
            } catch (NoSuchMethodException e) {
                throw new NoSuchMethodException(
                        String.format(
                                "Class %s has no constructor for: %s.",
                                clazz.getName(),
                                Arrays.toString(paramTypes.toArray())
                        )
                );
            } catch (InstantiationException e) {
                throw new RuntimeException(
                        String.format(
                                "Class %s is not a proper class (eg. is abstract or is a interface!).",
                                clazz.getName()
                        )
                );
            } catch (InvocationTargetException e) {
                throw new RuntimeException(
                        String.format(
                                "Class %s occurred an exception (%s) when being initialized.",
                                clazz.getName(),
                                e.getCause().fillInStackTrace()
                        )
                );
            } catch (IllegalAccessException e) {
                throw new IllegalAccessException(
                        String.format(
                                "Class %s has private or protected access to its constructor.",
                                clazz.getName()
                        )
                );
            }
        }
    }

    public static class Builder {
        private String packageName;
        private final LinkedHashMap<Class<?>, List<Object>> classes = new LinkedHashMap<>();

        public Builder setPackageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public Builder addInitVariable(Class<?> classInit, Object objectInit) {
            List<Object> objects = classes.getOrDefault(classInit, new ArrayList<>());
            objects.add(objectInit);
            classes.put(classInit, objects);

            return this;
        }

        public String getPackageName() {
            return packageName;
        }

        public LinkedHashMap<Class<?>, List<Object>> getClasses() {
            return classes;
        }

        public AutomaticInitializer run() throws NoSuchMethodException, IllegalAccessException, RuntimeException {
            return new AutomaticInitializer(this);
        }

        public AutomaticInitializer build() throws NoSuchMethodException, IllegalAccessException, RuntimeException {
            return run();
        }
    }
}