(function () {

    var core = hal.core.Core.getInstance();
    var myExtension = hal.core.ExtensionPoint.header("my-extension", "My Extension",
        function () {
            alert("Not yet implemented!");
        });
    core.extensionRegistry.register(myExtension);

})();
