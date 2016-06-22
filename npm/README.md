Next version of the WildFly management console. See https://github.com/hal/hal.next for more infos.

Before running the console you need to configure the allowed origins of the HTTP management endpoint.

- Standalone mode

        /core-service=management/management-interface=http-interface:list-add(name=allowed-origins,value=http://localhost:3000)
        reload

- Domain mode
 
        /host=master/core-service=management/management-interface=http-interface:list-add(name=allowed-origins,value=http://localhost:3000)
        reload --host=master

After that install and run using 

```
npm install hal-next
npm run hal-next
```
