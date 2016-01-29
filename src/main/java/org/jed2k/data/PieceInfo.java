package org.jed2k.data;

import java.util.ArrayList;

import org.jed2k.data.BlockInfo.BlockState;

public class PieceInfo {
	private ArrayList<BlockInfo>	blocks;
	private int finishedCount;
	public PieceInfo(int numBlocks) {
		finishedCount = 0;
		blocks = new ArrayList<BlockInfo>(numBlocks);
		for(int i = 0; i != numBlocks; ++i) blocks.add(new BlockInfo());		
	}
	
	public int blocksCount() {
		return blocks.size();
	}
	
	public int finishedBlocksCount() {
		return finishedCount;
	}
	
	public boolean finished() {
		return finishedCount == blocks.size();
	}
	
	/**
	 * 
	 * @return index of interested block
	 */
	public int requestBlock() {
		assert(!finished());
		for(int i = 0; i < blocks.size(); ++i) {
			BlockInfo bi = blocks.get(i);
			if (bi.state == BlockState.BS_NONE) {
				bi.state = BlockState.BS_REQUESTED;
				return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * finish 
	 * @param index - block index in peace
	 */
	public void finishBlock(int index) {
		assert(index < blocks.size());
		blocks.get(index).state = BlockState.BS_FINISHED;
		finishedCount++;
	}
}
