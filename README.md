# Resource Folder Reloading

This plugin fixes that annoying problem of needing to restart Spark every time a change is made to a html/css/js or template file.  

This plugin is only suited for development purposes.

### Usage

Add the following dependency to your `pom.xml`:

    <dependency>
        <groupId>com.sparkjava</groupId>
        <artifactId>spark-resource-reloading</artifactId>
        <version>1.0.0</version>
    </dependency>


Add the following to your main method:

    import spark.reloading.ResourceFolderObserver;
    
    public class App{
        public static void main(String[] args)
        {
            get(“/ping”, (request, response) -> “/pong”; );

            ResourceFolderObserver resourcesObserver = new ResourceFolderObserver(“src/main/resources”, “target/classes”);
            Runnable observerTask = () -> { resourcesObserver.watch(); };
            new Thread(observerTask).start();
        }
    }
