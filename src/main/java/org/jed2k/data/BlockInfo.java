package org.jed2k.data;

public class BlockInfo {
	enum BlockState {
		BS_NONE,
		BS_REQUESTED,
		BS_FINISHED
	}
	
	// ?
	int lastRequest;
	int numPeers = 0;
	BlockState	state = BlockState.BS_NONE;
	public BlockInfo() {
	}
		
}
