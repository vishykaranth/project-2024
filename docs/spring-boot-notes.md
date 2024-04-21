## Springboot Notes 
### @RestController vs @Controller
#### @Controller:
- This annotation is a specialization of @Component, 
- which indicates that the class is a web controller.
- It is used primarily in Spring MVC applications.
- When you annotate a class with @Controller, 
  - it becomes capable of handling HTTP requests, 
  - but you need to use @ResponseBody on the method level 
  - if you want the method to return data directly as the response body.
- It’s often used when you want to create a controller that will return view templates (like JSP, Thymeleaf) and 
  - also for creating RESTful web services, where methods return data directly as a response.

#### @RestController:
- Introduced in Spring 4.0, 
- @RestController is a convenience annotation that combines @Controller and @ResponseBody.
- It’s a specialized version of @Controller and is used for creating RESTful web services.
- When you use @RestController, all the methods in the controller will automatically have @ResponseBody semantics, 
  - meaning they all return the response directly as JSON/XML and not a view template.
- It simplifies the controller implementation for REST API services by eliminating the need to annotate each method with @ResponseBody.
- In summary, use @Controller when you’re building a traditional web application and want to return view templates, and use @RestController when you’re building a RESTful web service that returns data directly in the form of JSON or XML.

#### When to use @Controller instead of @RestController
Here are scenarios where @Controller is preferred over @RestController: