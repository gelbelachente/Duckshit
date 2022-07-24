# Duckshit
A Modern Lightweight Kotlin Web Framework

## Implementation
- clone the github repository / select 'Use this template'
- start coding ( Don't touch anything inside the server folder! )

## run the server

```
fun main() {
   DuckshitServer(port?,adress?).init()
}
```
## url handling

```
 server.register("test/{id}/", listOf("GET")){
            //doSth
            val user : User? = it.user
            val id = it.params["id"]
           #return Response instance
       } 
```

### Authentication

- so far the only default available Authentication System is SessionAuthentication
```
 server.register("test/{id}/", listOf("GET"), listOf(SessionAuthentication(isAdmin = true)), loginUrl="/admin/auth/"){
         //doSth
       } 
   ```    

- You may use the public register(username,password) (to create a new user) and authenticate(username,password) (to validate user credentials) functions

- Moreover, you can login an user by calling response.login(username,password) to sign him in with the standard Authentication specified in Settings.std_auth

### Response
- There are different types of Responses
1. TextResponse -> takes a string parameter and returns a text, also useful for JSON
2. RedirectResponse -> takes a url to which the user will be redirected
3. HtmlResponse -> takes a HTML file and an optional context parameter and returns it (**So far there are no implementations for imgs/css/js from foreign files**)

### Url Matching
- Duckshit always tries to ensure that the most specific url is selected
- e.g.: for a request of '/test/test/' and two urls provided by the application: "/test/test/" and "test/{name}/"
- Duckshit will select the first one


## Database

- The Duckshit Database Interface is ORM

### first model

- create model class
- the OnDelete attribute specifies what should happen with the model when the Foreign Key is deleted; The default value is remove

```
class Book(@Sql("name") var name : String, @Sql("user",onDelete=OnDelete.REMOVE) var user : User,id : Int = 0) : Model(id)
```

- create a table in the database
```
DuckshitDatabase.instance.apply{
register<Book>()
}
```
- apply operations
 
```
add(Book("JuSoft Manual",user2))

all<Book>()
```
- Searches can be concatenated by a **\*** which means an AND Operation or by a **/** which means an OR Operations; brackets can be applied as usual
-
```
filter<Book>(Search("name != 'Walhalla'") * Search("id <= 2"))

size<Book>()

delete<Book>(search)

get<Book>(search)
getOrNull<Book>(search)

update(model)
```

## Serializer

```
val user = User("Mario","Pepe123")
val ser = ModelSerializer(listOf(user), fields=listOf("*"),many=false)
ser.resolve()
```
- returns a TextResponse with Json text

## Template Language / DSTL

- as mentioned above, you may insert a context (MutableMap) into a HtmlResponse Constructor
- the context contains elements that are accessable in the html file itself through the DSTL (Duckshit Template Language)

- insert variables and their attributes

```
<p> %%a%% </p>
<p> %%a.length%% </p>
```

- Conditions
```
%%if a == b%%
<p> a equals b </p>
%%else%%
<p> a not equals b </p>
%%endif%%
```

- Loops

```
%%for a in b.array%%
  %%a%%
%%endfor%%
```

- include ( may be used to implement css/js files into e.g. <style> attributes )

```
%%include 'res/parent.html'%%
```
  
## Html Forms

- may be used to render models to html or reverse  
  
```
  val form = ModelForm(User::class, display=listOf("*"), id=4, empty=false)  
  val htmlForm : String = form.render()
   
  # typically used for transforming Post Parameters into a Model
  val form2 = ModelDeserializer(User::class)
  val user = form2.deserialize(mapOf("username" to "a","password" to "b", "id" to 898))
```  

## Admin Page

- callable at domain/admin/
- Authentication with an admin account needed
- can be used to create, delete, manipulate objects

## Console Support

- may be used to call commands

```
fun main(){
   TerminalListener().listen()
}
```

### commands:
- ``` create-admin "username" "password" ``` to create an Admin


## Settings
- can be used to manipulate server mechanics
   - page_404 : a path to the standard 404 page (html file)
   - page_403 : a path to the standard 403 page (html file)








