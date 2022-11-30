package io.thundra.merloc.intellij.runtime;

/**
 * @author serkan
 */
public final class RuntimeConfig {

    private final String brokerURL;
    private final String connectionName;
    private final String apiKey;
    private final String runtimeVersion;

    private RuntimeConfig(String brokerURL, String connectionName,
                          String apiKey, String runtimeVersion) {
        this.brokerURL = brokerURL;
        this.connectionName = connectionName;
        this.apiKey = apiKey;
        this.runtimeVersion = runtimeVersion;
    }

    public String brokerURL() {
        return brokerURL;
    }

    public String connectionName() {
        return connectionName;
    }

    public String apiKey() {
        return apiKey;
    }

    public String runtimeVersion() {
        return runtimeVersion;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String brokerURL;
        private String connectionName;
        private String apiKey;
        private String runtimeVersion;

        private Builder() {
        }

        public Builder brokerURL(String brokerURL) {
            this.brokerURL = brokerURL;
            return this;
        }

        public Builder connectionName(String connectionName) {
            this.connectionName = connectionName;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder runtimeVersion(String runtimeVersion) {
            this.runtimeVersion = runtimeVersion;
            return this;
        }

        public RuntimeConfig build() {
            return new RuntimeConfig(brokerURL, connectionName, apiKey, runtimeVersion);
        }

    }

}
