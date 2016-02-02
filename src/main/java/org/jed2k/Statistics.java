package org.jed2k;

public class Statistics implements Tickable {
	
	private enum ChannelsNames {
		UPLOAD_PAYLOAD(0),
		UPLOAD_PROTOCOL(1),
		DOWNLOAD_PAYLOAD(2),
		DOWNLOAD_PROTOCOL(3),
		CHANNELS_COUNT(4);
		
        public final int value;

        ChannelsNames(int v) {
            this.value = v;
        }
	}
	
	StatChannel channels[] = new StatChannel[ChannelsNames.CHANNELS_COUNT.value];
	
	public Statistics() {
		for(int i = 0; i < channels.length; ++i) channels[i] = new StatChannel();
	}

	@Override
	public void secondTick(long tick_interval_ms) {
		for(int i = 0; i < channels.length; ++i) channels[i].secondTick(tick_interval_ms);		
	}
	
	public void receiveBytes(long protocolBytes, long payloadBytes) {
		channels[ChannelsNames.DOWNLOAD_PROTOCOL.value].add(protocolBytes);
		channels[ChannelsNames.DOWNLOAD_PAYLOAD.value].add(payloadBytes);
	}
	
	public void sendBytes(long protocolBytes, long payloadBytes) {
		channels[ChannelsNames.UPLOAD_PROTOCOL.value].add(protocolBytes);
		channels[ChannelsNames.UPLOAD_PAYLOAD.value].add(payloadBytes);
	}
	
	public long totalDownload() {
		return channels[ChannelsNames.DOWNLOAD_PAYLOAD.value].total() + 
				channels[ChannelsNames.DOWNLOAD_PROTOCOL.value].total();
	}
	
	public long totalUpload() {
		return channels[ChannelsNames.UPLOAD_PAYLOAD.value].total() + 
				channels[ChannelsNames.UPLOAD_PROTOCOL.value].total();
	}
	
	public long lastDownload() {
		return channels[ChannelsNames.DOWNLOAD_PAYLOAD.value].counter() + 
				channels[ChannelsNames.DOWNLOAD_PROTOCOL.value].counter();
	}
	
	public long lastUpload() {
		return channels[ChannelsNames.UPLOAD_PAYLOAD.value].counter() + 
				channels[ChannelsNames.UPLOAD_PROTOCOL.value].counter();
	}
	
	public long downloadRate() {
		return channels[ChannelsNames.DOWNLOAD_PAYLOAD.value].rate() + 
				channels[ChannelsNames.DOWNLOAD_PROTOCOL.value].rate();
	}
	
	public long uploadRate() {
		return channels[ChannelsNames.UPLOAD_PAYLOAD.value].rate() +
				channels[ChannelsNames.UPLOAD_PROTOCOL.value].rate();
	}
	
	public long lowPassUploadRate() {
		return channels[ChannelsNames.UPLOAD_PAYLOAD.value].lowPassRate() + 
				channels[ChannelsNames.UPLOAD_PROTOCOL.value].lowPassRate();
	}
	
	public long lowPassDownloadRate() {
		return channels[ChannelsNames.DOWNLOAD_PAYLOAD.value].lowPassRate() + 
				channels[ChannelsNames.DOWNLOAD_PROTOCOL.value].lowPassRate();
	}
}
