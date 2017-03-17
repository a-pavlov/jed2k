package org.dkf.jed2k.kad;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * Created by apavlov on 13.03.17.
 */
@Data
public class StorageDescription {
    @SerializedName("ip") private String ip;
    @SerializedName("ports") private List<Integer> ports;
    @SerializedName("description") private String description;
}
