package spark.reloading;

import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.attribute.*;
import java.io.*;
import java.util.*;
import java.nio.file.LinkOption;

public class ObserveDirectory
{

    private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private final boolean recursive;
    private boolean trace = false;
    private DirectoryChangeListener dirChangeCallback;

    public ObserveDirectory(Path dir, boolean recursive, DirectoryChangeListener dc) throws IOException
    {
         this.watcher = FileSystems.getDefault().newWatchService();
         this.keys = new HashMap<WatchKey,Path>();
         this.recursive = recursive;
         this.dirChangeCallback = dc;

         if (recursive) {
             registerAll(dir);
         } else {
             register(dir);
         }

         // enable trace after initial registration
         this.trace = true;
    }

    /*
    * Register the given directory.
    */
    private void register(Path dir) throws IOException
    {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }

    /*
     * Register the given directory, and all its sub-directories.
     */
    private void registerAll(final Path start) throws IOException
    {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException
            {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
    /**
     * Process all events for keys queued to the watcher
     */
    public void processEvents()
    {
        for (;;) {

            //wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            try{
                Thread.sleep(100);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }

            Set<String> pathsChanged = new HashSet<String>();
            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                // print out event
                pathsChanged.add(child.toString());
                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (recursive && (kind == ENTRY_CREATE)) {
                    try {
                        if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                            registerAll(child);
                        }
                    } catch (IOException x) {
                    // ignore to keep sample readbale
                    }
                }
            }

            dirChangeCallback.directoryChange(pathsChanged);

            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }
}
