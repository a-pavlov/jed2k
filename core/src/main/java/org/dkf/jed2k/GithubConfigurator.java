package org.dkf.jed2k;

import com.google.gson.annotations.SerializedName;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.StorageDescription;

/**
 * Created by apavlov on 17.03.17.
 */
public class GithubConfigurator {
    @SerializedName("kadStorageDescription") StorageDescription kadStorageDescription;

    public GithubConfigurator() {
    }

    public void validate() throws JED2KException {
        if (kadStorageDescription == null) return;
        if (kadStorageDescription.getIp() == null) throw new JED2KException(ErrorCode.GITHUB_CFG_IP_IS_NULL);
        if (kadStorageDescription.getPorts() == null) throw new JED2KException(ErrorCode.GITHUB_CFG_PORTS_ARE_NULL);
        if (kadStorageDescription.getPorts().isEmpty()) throw new JED2KException(ErrorCode.GITHUB_CFG_PORTS_ARE_EMPTY);
    }

    public StorageDescription getKadStorageDescription() {
        return this.kadStorageDescription;
    }

    public void setKadStorageDescription(StorageDescription kadStorageDescription) {
        this.kadStorageDescription = kadStorageDescription;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof GithubConfigurator)) return false;
        final GithubConfigurator other = (GithubConfigurator) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$kadStorageDescription = this.getKadStorageDescription();
        final Object other$kadStorageDescription = other.getKadStorageDescription();
        if (this$kadStorageDescription == null ? other$kadStorageDescription != null : !this$kadStorageDescription.equals(other$kadStorageDescription))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof GithubConfigurator;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $kadStorageDescription = this.getKadStorageDescription();
        result = result * PRIME + ($kadStorageDescription == null ? 43 : $kadStorageDescription.hashCode());
        return result;
    }

    public String toString() {
        return "GithubConfigurator(kadStorageDescription=" + this.getKadStorageDescription() + ")";
    }
}
