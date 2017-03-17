package org.dkf.jed2k;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import org.dkf.jed2k.kad.StorageDescription;

/**
 * Created by apavlov on 17.03.17.
 */
@Data
public class GithubConfigurator {
    @SerializedName("kadStorageDescription") StorageDescription kadStorageDescription;
}
