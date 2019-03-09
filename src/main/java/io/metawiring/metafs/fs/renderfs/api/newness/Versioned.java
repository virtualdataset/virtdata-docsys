package io.metawiring.metafs.fs.renderfs.api.newness;

public interface Versioned {
    long getVersion();
    default boolean isValidFor(Versioned other) {
        return getVersion()==other.getVersion();
    }
    default boolean isValidFor(long otherVersion) {
        return getVersion()==otherVersion;
    }
}
