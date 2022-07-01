## Overview
By default [SpringDoc](https://springdoc.org/#does-springdoc-openapi-support-jersey) does not have Jersey support for generating documentation in Swagger. 

However, with this starter for Spring Boot, documentation generation for Jersey is added. 
Nothing supernatural is required from the developer. Just add a starter (without SpringDoc, it's already included in ``pom.xml``), 
register Jersey Resources and make them Spring Components.

## To Run
To get started, you will need to add this starter to your project and write the 
[configuration that SpringDoc requires](https://springdoc.org/#migrating-from-springfox) if you want. 
SpringDoc runs without config or any Swagger annotations.  

Probably, I should create a dependency for Maven or Gradle, but now this is not there, 
so just download the project and create your own .jar and then add it to project.
Perhaps a little later I will create a dependency :)

## Enabling/Disabling starter
The starter is automatically enabled if it finds an ``Application.class`` bean in the ApplicationContext. 
But, if you want to turn it on or off explicitly, then a property has been added for this:
```yaml
springdoc:
  jax-rs:
    enabled: [true|false]
```
```properties
springdoc.jax-rs.enabled=[true|false]
```
If it is disabled, then you are using pure SpringDoc without any additional manipulations with Jersey Resources.

## How it works
The starter intervenes in getting all the endpoints from Spring and adds the endpoints from Jersey as if they were provided by Spring. 
The starter reads Jax-Rs annotations on methods and classes. And then it creates a HandlerMethod object as if Spring created it.
However, this option does not work with method parameters, so the starter creates synthetic annotations for the parameters.

For example:
```java
@Service
@Path("/example/")
public class ExampleResource {

    @GET
    @Path("/word")
    @Produces(MediaType.TEXT_PLAIN)
    public String getMessage(@QueryParam("word") @DefaultValue("default") String word) {
        return word;
    }
    
    @POST
    @Path("/{word}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getMessage(@PathParam("word") String word) {
        return word;
    }

}
```
During documentation generation, it turns into:
```java
@Service
@Path("/example/")
@ResponseBody
public class ExampleResource {

    @GET
    @Path("/word")
    @Produces(MediaType.TEXT_PLAIN)
    public String getMessage(@QueryParam("word") @DefaultValue("default")
                             @RequestParam(value = "word", defaultValue = "default") String word) {
        return word;
    }

    @POST
    @Path("/{word}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getMessage(@PathParam("word") @PathVariable(value = "word") String word) {
        return word;
    }

}
```

But don't worry. These are synthetic annotations only for documentation generation. 
They do not participate anywhere else and are only needed so that SpringDoc can read them and generate documentation based on them.

After that, SpringDoc checks all received methods for Swagger or @ResponseBody annotations.
* In case there are Swagger annotations, they are used to generate tags and descriptions for endpoints by scheme.
* In the absence of Swagger annotations, SpringDoc generates documentation based on the class name (``ExampleResource -> "example-resource"``).

In the first case, SpringDoc reads the ``@Tag`` annotation above the class, and in the second, 
the starter generates a proxy with the addition of the ``@ResponseBody`` annotation 
(because if SpringDoc does not find ``@Tag``, it looks for ``@ResponseBody``).

And the starter also works with the SpringDoc plugin and creates an ``openapi.json`` file with Jersey Resources.

## Warning
I'm not sure if this works with Actuator endpoints. Tested with REST and it works perfectly.
