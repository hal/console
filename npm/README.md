Starts a local web server listening to http://localhost:3000. Please make sure to configure the allowed origins of the HTTP management endpoint.

- Standalone mode

        /core-service=management/management-interface=http-interface:list-add(name=allowed-origins,value=http://localhost:3000)
        reload

- Domain mode
 
        /host=master/core-service=management/management-interface=http-interface:list-add(name=allowed-origins,value=http://localhost:3000)
        reload --host=master

To install and run HAL.next use

```
npm install -g hal-next
hal-next
```

For more details see https://github.com/hal/hal.next.
