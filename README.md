# PipelinR demo app

This Spring Boot app demonstrates the basic use of [PipelinR](https://github.com/sizovs/PipelinR) in a service layer.

The starting point is [ApplicationBootstrap](src/main/java/lightweight4j/ApplicationBootstrap.java).

The application is requires Java 11+.

#### Running application with IDE
`ApplicationBootstrap` has a main method. Just run it and have fun.
 
#### Running application with Gradle
```
./gradlew bootRun
```

#### Running tests
There is a single test [BecomeAMemberSpec](src/test/groovy/lightweight4j/features/membership/BecomeAMemberSpec.groovy) that demonstrate command execution via http.

