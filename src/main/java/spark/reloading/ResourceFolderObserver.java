package spark.reloading;

import java.util.Set;
import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.LinkOption;
import java.nio.file.Paths;

public class ResourceFolderObserver implements DirectoryChangeListener
{
    private String sourcePath;
    private String targetPath;

    public ResourceFolderObserver(String srcPath, String tgtPath)
    {
        sourcePath = srcPath;
        targetPath = tgtPath;
    }

    public void watch() throws IOException
    {
        ObserveDirectory od = new ObserveDirectory(Paths.get(sourcePath), true, this);
        od.processEvents();
    }

    public void directoryChange(Set<String> paths)
    {
        for(Object pth: paths.toArray())
        {
            String sPath = (String)pth;
            String fPath = sPath.replaceFirst("^"+sourcePath, "");
            String tPath = targetPath + fPath;
            if(new File(sPath).exists())
            {
                try
                {
                    Files.copy(
                        new File(sPath).toPath(),
                        new File(tPath).toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES,
                        LinkOption.NOFOLLOW_LINKS);
                }
                catch (Exception e){ throw new RuntimeException(e); }
            }
        }
    }
}
