package org.jed2k;

public class Statistics {
	
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
}
