Development package for writing HAL extensions. Follow these steps to setup a new extension project:

- Create a new yarn project
- Install a developer dependency for `hal-edk`:

      $ yarn install --save-dev hal-edk
    
  This will install the management console in `node_modules/hal-edk` and create two files in the project's root folder:
  
    - `index.html`: Starts the console and loads `extension.js`
    - `extension.js`: Registers a noop header extension
    
- Start a local web server and open `index.html`

Don't forget to add the URL of your local web server as allowed origin to the management model. 

For more details see https://github.com/hal/hal.next.
