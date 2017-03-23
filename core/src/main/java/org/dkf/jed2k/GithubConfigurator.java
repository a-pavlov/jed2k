package org.dkf.jed2k;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.StorageDescription;

/**
 * Created by apavlov on 17.03.17.
 */
@Data
public class GithubConfigurator {
    @SerializedName("kadStorageDescription") StorageDescription kadStorageDescription;

    public void validate() throws JED2KException {
        if (kadStorageDescription == null) return;
        if (kadStorageDescription.getIp() == null) throw new JED2KException(ErrorCode.GITHUB_CFG_IP_IS_NULL);
        if (kadStorageDescription.getPorts() == null) throw new JED2KException(ErrorCode.GITHUB_CFG_PORTS_ARE_NULL);
        if (kadStorageDescription.getPorts().isEmpty()) throw new JED2KException(ErrorCode.GITHUB_CFG_PORTS_ARE_EMPTY);
    }
}
