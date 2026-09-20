package com.falcon.car.data.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Capability record for a family of vehicle communication interfaces.
 *
 * <p>The UI renders whatever is in here, so supporting a new VCI means adding a
 * profile rather than touching a screen. Protocol coverage is a map and not a
 * flag: an adapter can be {@link SupportLevel#LIMITED} on a bus it reaches only
 * through a manufacturer adapter or a paid unlock.
 */
public final class AdapterProfile {

    /** Filter buckets on the device list. An adapter can sit in several. */
    public enum Category {
        OBD2, HEAVY_DUTY, J2534
    }

    private final String brand;
    private final List<String> models;
    private final Set<Transport> transports;
    private final Map<Protocol, SupportLevel> protocols;
    private final Set<ConnectorType> connectors;
    private final Set<Category> categories;
    private final SupportLevel overall;

    private AdapterProfile(Builder builder) {
        this.brand = builder.brand;
        this.models = Collections.unmodifiableList(builder.models);
        this.transports = Collections.unmodifiableSet(builder.transports);
        this.protocols = Collections.unmodifiableMap(builder.protocols);
        this.connectors = Collections.unmodifiableSet(builder.connectors);
        this.categories = Collections.unmodifiableSet(builder.categories);
        this.overall = builder.overall;
    }

    public String getBrand() {
        return brand;
    }

    public List<String> getModels() {
        return models;
    }

    /** First model of the family, used where a single name has to stand in. */
    public String getPrimaryModel() {
        return models.isEmpty() ? brand : models.get(0);
    }

    public Set<Transport> getTransports() {
        return transports;
    }

    public Map<Protocol, SupportLevel> getProtocols() {
        return protocols;
    }

    public SupportLevel supportFor(Protocol protocol) {
        SupportLevel level = protocols.get(protocol);
        return level == null ? SupportLevel.UNSUPPORTED : level;
    }

    public Set<ConnectorType> getConnectors() {
        return connectors;
    }

    public boolean isIn(Category category) {
        return categories.contains(category);
    }

    public SupportLevel getOverallSupport() {
        return overall;
    }

    public static Builder builder(String brand) {
        return new Builder(brand);
    }

    public static final class Builder {
        private final String brand;
        private final List<String> models = new java.util.ArrayList<>();
        private final Set<Transport> transports = EnumSet.noneOf(Transport.class);
        private final Map<Protocol, SupportLevel> protocols = new EnumMap<>(Protocol.class);
        private final Set<ConnectorType> connectors = EnumSet.noneOf(ConnectorType.class);
        private final Set<Category> categories = EnumSet.noneOf(Category.class);
        private SupportLevel overall = SupportLevel.SUPPORTED;

        private Builder(String brand) {
            this.brand = brand;
        }

        public Builder models(String... names) {
            java.util.Collections.addAll(models, names);
            return this;
        }

        public Builder transports(Transport... values) {
            java.util.Collections.addAll(transports, values);
            return this;
        }

        public Builder protocols(SupportLevel level, Protocol... values) {
            for (Protocol protocol : values) {
                protocols.put(protocol, level);
            }
            return this;
        }

        public Builder connectors(ConnectorType... values) {
            java.util.Collections.addAll(connectors, values);
            return this;
        }

        public Builder categories(Category... values) {
            java.util.Collections.addAll(categories, values);
            return this;
        }

        public Builder overall(SupportLevel level) {
            this.overall = level;
            return this;
        }

        public AdapterProfile build() {
            return new AdapterProfile(this);
        }
    }
}
