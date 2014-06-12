/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Raymond Kroon
 *
 */

package org.geowebcache.storage.versioned;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geowebcache.util.ApplicationContextProvider;
import org.geowebcache.util.GWCVars;
import org.springframework.web.context.WebApplicationContext;

/**
 * Saves version of repository on disk.
 *
 */
public class VersionRepository {
    private static Log log = LogFactory.getLog(org.geowebcache.storage.versioned.VersionRepository.class);

    public final static String GWC_VERSION_REPO_DIR = "GEOWEBCACHE_VERSION_REPO_DIR";

    public static String LAYER_VERSION_EXTENSION = "version";

    private final File repositoryDir;

    private Map<String, Long> versionCache;

    public VersionRepository(ApplicationContextProvider provider) {
        WebApplicationContext context = provider.getApplicationContext();
        String repositoryPath = GWCVars.findEnvVar(context, GWC_VERSION_REPO_DIR);
        repositoryDir = new File(repositoryPath);

        if (!repositoryDir.exists() && !repositoryDir.mkdirs()) {
            throw new IllegalArgumentException("path does not exist and could not be created");
        }

        init();
    }

    private void init() {

        versionCache = new HashMap<String, Long>();

        File[] versionFiles = repositoryDir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return FilenameUtils.getExtension(pathname.getPath()).equals(LAYER_VERSION_EXTENSION);
            }
        });

        for (File versionFile : versionFiles) {
            try {
                versionCache.put(FilenameUtils.getBaseName(versionFile.getName()), readVersion(versionFile));
            } catch (IOException ex) {
                log.warn("Could not parse mappingFile", ex);
            }
        }
    }

    private long readVersion(File versionFile) throws FileNotFoundException, IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(versionFile));
            return Long.parseLong(reader.readLine());
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private void writeVersion(String layerName, long version) throws FileNotFoundException {

        File versionFile = new File(repositoryDir, layerName + "." + LAYER_VERSION_EXTENSION);
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(versionFile);
            writer.println(version);
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Destroy method for Spring
     */
    public void destroy() {

    }

    private String filterLayerName(String layerName) {
        return layerName.replace(':', '_').replace(' ', '_');
    }

    public synchronized long getVersion(String layerName) {
        String filteredLayerName = filterLayerName(layerName);
        return versionCache.containsKey(filteredLayerName) ? versionCache.get(filteredLayerName) : 0;
    }

    public synchronized void setVersion(String layerName, long version) throws IOException {
        String filteredLayerName = filterLayerName(layerName);
        versionCache.put(filteredLayerName, version);
        writeVersion(filteredLayerName, version);
    }
}
