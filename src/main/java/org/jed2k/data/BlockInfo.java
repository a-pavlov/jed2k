package org.jed2k.data;

public class BlockInfo {
	enum BlockState {
		BS_NONE,
		BS_REQUESTED,
		BS_FINISHED
	}
	
	long lastTick = 0;
	
	// ?
	int lastRequest;
	int numPeers = 0;
	BlockState	state = BlockState.BS_NONE;
	public BlockInfo() {
	}
}
