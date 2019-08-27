NetworkResponse RxJava2 Adapter
===============================

An `Adapter` which allows `NetworkResponse` objects to be returned from RxJava2 streams.

Available types:

* `Observable<NetworkResponse<T, U>>` where `T` is the body type and `U` the error type.
* `Flowable<NetworkResponse<T, U>>` where `T` is the body type and `U` the error type.
* `Single<NetworkResponse<T, U>>` where `T` is the body type and `U` the error type.
* `Maybe<NetworkResponse<T, U>>` where `T` is the body type and `U` the error type.


Usage
-----

Add `NetworkResponseRxJava2CallAdapterFactory` as a `CallAdapter` before the `RxJava2CallAdapterFactory` when building your `Retrofit` instance.
```java
Retrofit retrofit = new Retrofit.Builder()
    .baseUrl("https://example.com/")
    .addCallAdapterFactory(NetworkResponseRxJava2CallAdapterFactory.create())
    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
    .addConverterFactory(MoshiConverterFactory.create())
    .build();
```

Your service methods can now use any of the above types as their return type.
```java
interface MyService {
  @GET("/user")
  Single<NetworkResponse<User, Error>> getUser();
}
```

```java
userApi.getUser()
  .subscribe { response: NetworkResponse<User, Error> ->
    when (response) {
      is NetworkResponse.Success<User> -> {
          // A 2XX response that's guaranteed to have a body of type User.
      }
      is NetworkResponse.ServerError<Error> -> { 
          // A non-2XX response that may have an Error as its error body.
      }
      is NetworkResponse.NetworkError -> { 
          // A request that didn't result in a response from the server.
      }
    }
  }
```

License
-------

    Copyright 2019 Michiel Vermeersch

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
