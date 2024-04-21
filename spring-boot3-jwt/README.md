# spring-boot3-jwt

## Create Token 
- http://localhost:8080/products/authenticate
~~~shell
curl --location 'http://localhost:8080/products/authenticate' --header 'Content-Type: application/json' --data '{ "username" : "Basant", "password" : "Pwd1"}'
~~~

## API : /products/welcome
~~~shell
curl --location 'http://localhost:8080/products/welcome' --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJCYXNhbnQiLCJpYXQiOjE3MTM2Nzg2NzcsImV4cCI6MTcxMzY4MDQ3N30.Nr6w7wp37ACwaiZlY27BmFy4j6A-SAgOhV8HgxXpTyk'
~~~

## API : /products/all
~~~shell 
curl --location 'http://localhost:8080/products/all' --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJCYXNhbnQiLCJpYXQiOjE3MTM2Nzk2MDUsImV4cCI6MTcxMzY4MTQwNX0.0bYFInCwQXofH10BP_IoNO2Bw-j9IHNEUNRd0vCTsBs'
~~~

## API 03 : http://localhost:8080/products/1, Role : ROLE_USER,ROLE_ADMIN
~~~shell 
curl --location 'http://localhost:8080/products/1' --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJKb2huIiwiaWF0IjoxNzEzNjgwNDkzLCJleHAiOjE3MTM2ODIyOTN9.dyih8FrCHXRE6zo9h8FHHqwRZTR-waA2pbB_AXJ61Ng'
~~~

## API 03 : http://localhost:8080/products/1, Role : ROLE_ADMIN
~~~shell 
curl --location 'http://localhost:8080/products/1' --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJCYXNhbnQiLCJpYXQiOjE3MTM2ODA2MzQsImV4cCI6MTcxMzY4MjQzNH0.qHIyBBNGBOaMsTa3vQqk4Z_4dFLfeNHQeI3SfZ7KBBI'
~~~

## SetUp 
- Insert into postgres database 
~~~shell

CREATE TABLE public.user_info (
	id int8 NULL,
	"name" varchar NULL,
	email varchar NULL,
	"password" varchar NULL,
	roles varchar NULL
);
  
INSERT INTO public.user_info (id,"name",email,"password",roles) VALUES
	 (2,'John','John','Pwd2','ROLE_USER,ROLE_ADMIN,HR'),
	 (1,'Basant','Basant','Pwd1','ROLE_ADMIN');

~~~