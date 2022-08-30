package io.thundra.merloc.intellij.runtime;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author serkan
 */
public class RuntimeManager {

    public static final String RUNTIME_BUILT_IN_VERSION_PLACEHOLDER = "BUILT-IN";
    public static final String RUNTIME_LATEST_VERSION_PLACEHOLDER = "LATEST";
    public static final String RUNTIME_DEFAULT_VERSION = RUNTIME_BUILT_IN_VERSION_PLACEHOLDER;
    public static final String RUNTIME_LIB_NAME = "merloc-aws-lambda-runtime-embedded";
    public static final String RUNTIME_LATEST_VERSION_URI =
            "https://repo1.maven.org/maven2/io/thundra/merloc/" + RUNTIME_LIB_NAME + "/maven-metadata.xml";

    private static final String MERLOC_PLUGIN_ID = "io.thundra.merloc";
    private static final String MERLOC_PLUGIN_RESOURCES_DIR = "resources";
    private static final int DEFAULT_BUFFER_SIZE = 1024;

    public List<String> listRuntimeVersions() throws Exception {
        List<String> versions = new ArrayList<>();
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document document = builder.parse(RUNTIME_LATEST_VERSION_URI);

        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList nodeList = (NodeList) xPath.compile("/metadata/versioning/versions/version").
                evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            versions.add(node.getTextContent());
        }
        return versions;
    }

    public String getLatestVersion() throws Exception {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document document = builder.parse(RUNTIME_LATEST_VERSION_URI);

        XPath xPath = XPathFactory.newInstance().newXPath();
        return xPath.compile("/metadata/versioning/latest").evaluate(document);
    }

    public interface DownloadListener {

        default void onDownload(double percentage) {
        }

        default void onComplete() {
        }

        default void onError(Throwable error) {
        }

    }

    private void downloadRuntimeJar(File runtimeJarFile, String runtimeVersion,
                                    DownloadListener downloadListener) throws Exception {
        try {
            String runtimeJarURLValue =
                    String.format(
                            "https://repo1.maven.org/maven2/io/thundra/merloc/%s/%s/%s-%s.jar",
                            RUNTIME_LIB_NAME, runtimeVersion, RUNTIME_LIB_NAME, runtimeVersion);
            URL runtimeJarURL = new URL(runtimeJarURLValue);
            HttpURLConnection httpConnection = (HttpURLConnection) runtimeJarURL.openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.setRequestProperty("Content-type", "application/json");
            httpConnection.setUseCaches(false);
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);

            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream is = httpConnection.getInputStream();
                OutputStream os = new FileOutputStream(runtimeJarFile);
                int total = httpConnection.getContentLength();
                int completed = 0;
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int read;

                while ((read = is.read(buffer, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
                    os.write(buffer, 0, read);
                    completed += read;
                    if (downloadListener != null) {
                        downloadListener.onDownload((double) completed / (double) total);
                    }
                }

                if (downloadListener != null) {
                    downloadListener.onComplete();
                }
            } else {
                throw new IOException(String.format(
                        "Downloading runtime JAR file from %s failed with status code %d",
                        runtimeJarURLValue, httpConnection.getResponseCode()));
            }
        } catch (Exception e) {
            if (downloadListener != null) {
                downloadListener.onError(e);
            }
            throw e;
        }
    }

    private String getBuiltInRuntimeJarPath() {
        IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PluginId.getId(MERLOC_PLUGIN_ID));
        String resourcesPath = plugin.getPluginPath() + File.separator + MERLOC_PLUGIN_RESOURCES_DIR;
        File resourcesDir = new File(resourcesPath);
        if (resourcesDir.exists()) {
            for (File resource : resourcesDir.listFiles()) {
                if (resource.getName().startsWith(RUNTIME_LIB_NAME)) {
                    return resource.getAbsolutePath();
                }
            }
        }
        return null;
    }

    // TODO
    // Even though, currently we only use built-in runtime jar,
    // in the future, using external runtimes will be supported by downloading.
    // So lets keep download logic in this method for future use.
    public String getRuntimeJarPath(String runtimeVersion,
                                    DownloadListener downloadListener) throws Exception {
        if (RUNTIME_BUILT_IN_VERSION_PLACEHOLDER.equalsIgnoreCase(runtimeVersion)) {
            String builtInRuntimeJarPath = getBuiltInRuntimeJarPath();
            if (builtInRuntimeJarPath == null) {
                throw new IOException("Unable to find built-in runtime");
            }
            return builtInRuntimeJarPath;
        }

        if (RUNTIME_LATEST_VERSION_PLACEHOLDER.equalsIgnoreCase(runtimeVersion)) {
            try {
                runtimeVersion = getLatestVersion();
            } catch (Exception e) {
                throw new IOException("Unable to find runtime latest version: " + e.getMessage(), e);
            }
        }

        String userHome = System.getProperty("user.home");
        String localRuntimeJarDirectory = String.format("%s/.merloc/runtimes/java/%s", userHome, runtimeVersion);
        File localRuntimeJarDirectoryFile = new File(localRuntimeJarDirectory);
        if (!localRuntimeJarDirectoryFile.exists()) {
            if (!localRuntimeJarDirectoryFile.mkdirs()) {
                throw new IOException("Unable to create directory to put runtime JAR file: " + localRuntimeJarDirectory);
            }
        }

        String runtimeJarFileName = RUNTIME_LIB_NAME + ".jar";
        String localRuntimeJarPath = localRuntimeJarDirectory + File.separator + runtimeJarFileName;
        File localRuntimeJarFile = new File(localRuntimeJarPath);
        if (localRuntimeJarFile.exists()) {
            return localRuntimeJarFile.getAbsolutePath();
        }

        String tempRuntimeJarFileName = UUID.randomUUID().toString() + ".jar";
        String tempRuntimeJarPath = localRuntimeJarDirectory + File.separator + tempRuntimeJarFileName;
        File tempRuntimeJarFile = new File(tempRuntimeJarPath);

        downloadRuntimeJar(tempRuntimeJarFile, runtimeVersion, downloadListener);

        if (!tempRuntimeJarFile.renameTo(localRuntimeJarFile)) {
            throw new IOException("Unable to finalize creating runtime JAR file");
        }

        return localRuntimeJarFile.getAbsolutePath();
    }

    public String getRuntimeJarPath(String runtimeVersion) throws Exception {
        return getRuntimeJarPath(runtimeVersion, null);
    }

}
