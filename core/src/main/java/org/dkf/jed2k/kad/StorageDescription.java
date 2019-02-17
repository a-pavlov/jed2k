package org.dkf.jed2k.kad;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by apavlov on 13.03.17.
 */
public class StorageDescription {
    @SerializedName("ip") private String ip;
    @SerializedName("ports") private List<Integer> ports;
    @SerializedName("description") private String description;

    public StorageDescription() {
    }

    public String getIp() {
        return this.ip;
    }

    public List<Integer> getPorts() {
        return this.ports;
    }

    public String getDescription() {
        return this.description;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPorts(List<Integer> ports) {
        this.ports = ports;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof StorageDescription)) return false;
        final StorageDescription other = (StorageDescription) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$ip = this.getIp();
        final Object other$ip = other.getIp();
        if (this$ip == null ? other$ip != null : !this$ip.equals(other$ip)) return false;
        final Object this$ports = this.getPorts();
        final Object other$ports = other.getPorts();
        if (this$ports == null ? other$ports != null : !this$ports.equals(other$ports)) return false;
        final Object this$description = this.getDescription();
        final Object other$description = other.getDescription();
        if (this$description == null ? other$description != null : !this$description.equals(other$description))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof StorageDescription;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $ip = this.getIp();
        result = result * PRIME + ($ip == null ? 43 : $ip.hashCode());
        final Object $ports = this.getPorts();
        result = result * PRIME + ($ports == null ? 43 : $ports.hashCode());
        final Object $description = this.getDescription();
        result = result * PRIME + ($description == null ? 43 : $description.hashCode());
        return result;
    }

    public String toString() {
        return "StorageDescription(ip=" + this.getIp() + ", ports=" + this.getPorts() + ", description=" + this.getDescription() + ")";
    }
}
