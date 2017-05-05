var myExtension = hal.core.Extension.header("my-extension", "My Extension",
    function () {
        alert("Not yet implemented!");
    });

hal.core.Core.getInstance().extensionRegistry.register(myExtension);
