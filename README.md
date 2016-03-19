# HAL.Next

Fresh start of the HAL management console using the latest frameworks / libraries. 

- Java 8
- GWT 3.x (JsInterop, Elemental)
- Latest GWTP build
- PatternFly

## Demo

The latest version of HAL.Next can be found at https://hal.github.io/hal.next/. Please make sure to add this domain as allowed origin in WildFly.

- standalone mode: 

        /core-service=management/management-interface=http-interface:list-add(name=allowed-origins,value=https://hal.github.io)
        reload
    
- domain mode:

        /host=master/core-service=management/management-interface=http-interface:list-add(name=allowed-origins,value=https://hal.github.io)
        reload --host=master
