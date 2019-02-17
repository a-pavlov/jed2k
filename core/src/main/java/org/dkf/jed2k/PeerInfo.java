package org.dkf.jed2k;

import org.dkf.jed2k.protocol.BitField;
import org.dkf.jed2k.protocol.Endpoint;

/**
 * Created by inkpot on 25.08.2016.
 */
public class PeerInfo {
    public static final byte INCOMING = 0x1;
    public static final byte SERVER = 0x2;
    public static final byte DHT = 0x4;
    public static final byte RESUME = 0x8;

    private int downloadSpeed   = 0;
    private int payloadDownloadSpeed = 0;
    private long downloadPayload    = 0;
    private long downloadProtocol   = 0;
    private BitField remotePieces;
    private int failCount   = 0;
    private Endpoint endpoint;
    private String modName;
    private int version;
    private int modVersion;
    private String strModVersion;
    private int sourceFlag;

    public PeerInfo() {
    }

    public int getDownloadSpeed() {
        return this.downloadSpeed;
    }

    public int getPayloadDownloadSpeed() {
        return this.payloadDownloadSpeed;
    }

    public long getDownloadPayload() {
        return this.downloadPayload;
    }

    public long getDownloadProtocol() {
        return this.downloadProtocol;
    }

    public BitField getRemotePieces() {
        return this.remotePieces;
    }

    public int getFailCount() {
        return this.failCount;
    }

    public Endpoint getEndpoint() {
        return this.endpoint;
    }

    public String getModName() {
        return this.modName;
    }

    public int getVersion() {
        return this.version;
    }

    public int getModVersion() {
        return this.modVersion;
    }

    public String getStrModVersion() {
        return this.strModVersion;
    }

    public int getSourceFlag() {
        return this.sourceFlag;
    }

    public void setDownloadSpeed(int downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    public void setPayloadDownloadSpeed(int payloadDownloadSpeed) {
        this.payloadDownloadSpeed = payloadDownloadSpeed;
    }

    public void setDownloadPayload(long downloadPayload) {
        this.downloadPayload = downloadPayload;
    }

    public void setDownloadProtocol(long downloadProtocol) {
        this.downloadProtocol = downloadProtocol;
    }

    public void setRemotePieces(BitField remotePieces) {
        this.remotePieces = remotePieces;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public void setModName(String modName) {
        this.modName = modName;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setModVersion(int modVersion) {
        this.modVersion = modVersion;
    }

    public void setStrModVersion(String strModVersion) {
        this.strModVersion = strModVersion;
    }

    public void setSourceFlag(int sourceFlag) {
        this.sourceFlag = sourceFlag;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof PeerInfo)) return false;
        final PeerInfo other = (PeerInfo) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getDownloadSpeed() != other.getDownloadSpeed()) return false;
        if (this.getPayloadDownloadSpeed() != other.getPayloadDownloadSpeed()) return false;
        if (this.getDownloadPayload() != other.getDownloadPayload()) return false;
        if (this.getDownloadProtocol() != other.getDownloadProtocol()) return false;
        final Object this$remotePieces = this.getRemotePieces();
        final Object other$remotePieces = other.getRemotePieces();
        if (this$remotePieces == null ? other$remotePieces != null : !this$remotePieces.equals(other$remotePieces))
            return false;
        if (this.getFailCount() != other.getFailCount()) return false;
        final Object this$endpoint = this.getEndpoint();
        final Object other$endpoint = other.getEndpoint();
        if (this$endpoint == null ? other$endpoint != null : !this$endpoint.equals(other$endpoint)) return false;
        final Object this$modName = this.getModName();
        final Object other$modName = other.getModName();
        if (this$modName == null ? other$modName != null : !this$modName.equals(other$modName)) return false;
        if (this.getVersion() != other.getVersion()) return false;
        if (this.getModVersion() != other.getModVersion()) return false;
        final Object this$strModVersion = this.getStrModVersion();
        final Object other$strModVersion = other.getStrModVersion();
        if (this$strModVersion == null ? other$strModVersion != null : !this$strModVersion.equals(other$strModVersion))
            return false;
        if (this.getSourceFlag() != other.getSourceFlag()) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof PeerInfo;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getDownloadSpeed();
        result = result * PRIME + this.getPayloadDownloadSpeed();
        final long $downloadPayload = this.getDownloadPayload();
        result = result * PRIME + (int) ($downloadPayload >>> 32 ^ $downloadPayload);
        final long $downloadProtocol = this.getDownloadProtocol();
        result = result * PRIME + (int) ($downloadProtocol >>> 32 ^ $downloadProtocol);
        final Object $remotePieces = this.getRemotePieces();
        result = result * PRIME + ($remotePieces == null ? 43 : $remotePieces.hashCode());
        result = result * PRIME + this.getFailCount();
        final Object $endpoint = this.getEndpoint();
        result = result * PRIME + ($endpoint == null ? 43 : $endpoint.hashCode());
        final Object $modName = this.getModName();
        result = result * PRIME + ($modName == null ? 43 : $modName.hashCode());
        result = result * PRIME + this.getVersion();
        result = result * PRIME + this.getModVersion();
        final Object $strModVersion = this.getStrModVersion();
        result = result * PRIME + ($strModVersion == null ? 43 : $strModVersion.hashCode());
        result = result * PRIME + this.getSourceFlag();
        return result;
    }

    public String toString() {
        return "PeerInfo(downloadSpeed=" + this.getDownloadSpeed() + ", payloadDownloadSpeed=" + this.getPayloadDownloadSpeed() + ", downloadPayload=" + this.getDownloadPayload() + ", downloadProtocol=" + this.getDownloadProtocol() + ", remotePieces=" + this.getRemotePieces() + ", failCount=" + this.getFailCount() + ", endpoint=" + this.getEndpoint() + ", modName=" + this.getModName() + ", version=" + this.getVersion() + ", modVersion=" + this.getModVersion() + ", strModVersion=" + this.getStrModVersion() + ", sourceFlag=" + this.getSourceFlag() + ")";
    }
}
