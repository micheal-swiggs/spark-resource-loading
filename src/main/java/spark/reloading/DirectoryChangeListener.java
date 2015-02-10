package spark.reloading;

import java.util.Set;

public interface DirectoryChangeListener
{
    public void directoryChange(Set<String> pathsChanged);
}
