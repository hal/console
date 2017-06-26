# HAL JavaScript API

The goal of the JavaScript API is to open HAL for JavaScript developers. The API can be used to write extensions which add features to the console. Another use case is to write SPAs which interact with the management interface. A monitoring tool which shows the statistics of all data-source pools across the domain could be an example of such an application.

The JavaScript API is divided into several modules which define a common namespace for classes that belong together. 

- `hal.config` - contains classes which hold information about the console and its environment.
- `hal.core` - provides access to the core classes of HAL.
- `hal.dmr` - deals with executing operations and the DMR API.
- `hal.meta` - provides access to the metadata of resources.
- `hal.mvp` - provides access to the model view presenter framework which is used in HAL (*NYI*).
- `hal.ui` - contains classes to build the user interface.

There are a couple of [examples](manual/example.html) which show the JavaScript API in action.