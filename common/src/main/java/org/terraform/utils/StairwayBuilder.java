package org.terraform.utils;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.ArrayList;
import java.util.Random;

/**
 * Used to generate 1-block wide stairways from a starting point
 *
 */
public class StairwayBuilder {
	
	private Material[] stairTypes;
	private BlockFace stairDirection = BlockFace.DOWN;
	private Material[] downTypes;
	private boolean stopAtWater = false;
	private boolean angled = false;
	private int maxExtensionForward = 10;
	
	public StairwayBuilder(Material... stairTypes) {
		this.stairTypes = stairTypes;

		//Infer downTypes
		ArrayList<Material> downTypes = new ArrayList<Material>();
		for(Material mat:stairTypes) {
			downTypes.add(Material.matchMaterial(
					mat.toString().replace("_STAIRS", ""))
			);
		}
		this.downTypes = new Material[downTypes.size()];
		
		for(int i = 0; i < downTypes.size(); i++)
			this.downTypes[i] = downTypes.get(i);
		
	}
	
	public StairwayBuilder build(Wall start) {
		if(stairDirection == BlockFace.DOWN) { //Stairway extends downwards
			int threshold = 5;
	        BlockFace extensionDir = start.getDirection();
	    	while (continueCondition(start)) {
	    		
	    		if(threshold == 0) {
	    			start.setType(downTypes);
		    		start.getRelative(0,-1,0).downUntilSolid(new Random(), downTypes);
	    			extensionDir = BlockUtils.getTurnBlockFace(new Random(), extensionDir);
	    			start = start.getRelative(extensionDir);
	    		}
	    		new StairBuilder(stairTypes)
	                    .setFacing(extensionDir.getOppositeFace())
	                    .apply(start);
	            
	    		start.getRelative(0,-1,0).downUntilSolid(new Random(), downTypes);

	    		if(angled) 
	        		threshold--;
	    		start = start.getRelative(extensionDir).getRelative(0,-1,0);
	        }

	    	//If it is on water, build a pathway forward. 
	    	//Hope that there's something there.
	    	//Stop on oak slabs specifically too, because that's the path type
	    	if(stopAtWater 
	    			&& start.get().getType() != Material.OAK_SLAB 
	    			&& BlockUtils.isWet(start.get())) {
	    		for(int i = 0; i < maxExtensionForward; i++) {
	    			if(start.getType().isSolid())
	    				break;
	    			
	    			start.downUntilSolid(new Random(), downTypes);
	    			start = start.getFront();
	    		}
	    	}
		}else if(stairDirection == BlockFace.UP){ //Stairway extends upwards
			
			int threshold = 5;
	        BlockFace extensionDir = start.getDirection();
	    	while (continueCondition(start)) {
	    		
	    		if(threshold == 0) {
	    			start = start.getRelative(0,-1,0);
		    		start.getRelative(0,1,0).Pillar(3, new Random(), Material.AIR);
	    			start.setType(downTypes);
		    		start.getRelative(0,-1,0).downUntilSolid(new Random(), downTypes);
	    			extensionDir = BlockUtils.getTurnBlockFace(new Random(), extensionDir);
	    			start = start.getRelative(extensionDir).getRelative(0,1,0);
	    		}
	    		
	    		new StairBuilder(stairTypes)
	                    .setFacing(extensionDir)
	                    .apply(start);
	            
	    		start.getRelative(0,-1,0).downUntilSolid(new Random(), downTypes);
	    		
	    		//This space is required for movement
	    		start.getRelative(0,1,0).Pillar(3, new Random(), Material.AIR);
	    		start.getRelative(0,2,0).getRelative(extensionDir).setType(Material.AIR);
	    		
	    		if(angled) 
	        		threshold--;
	    		start = start.getRelative(extensionDir).getRelative(0, 1, 0);
	        }
			
		}else {
			TerraformGeneratorPlugin.logger.error("StairwayBuilder was told to spawn stairway with non up/down stair direction!");
		}
        
    	
    	return this;
    }
	
	private boolean continueCondition(Wall target) {
		if(this.stairDirection == BlockFace.DOWN) {
			if(stopAtWater && BlockUtils.isWet(target.get()))
				return false;
			
			return !target.getType().isSolid();
		}else {			
			//Continue carving upwards until the area isn't solid anymore.
			return target.getType().isSolid();
		}
	}

	/**
	 * Only used when stopAtWater is true.
	 * Refers to the maximum length of the pathway generated when
	 * the stairway hits water.
	 * @param extension
	 * @return
	 */
	public StairwayBuilder setMaxExtensionForward(int extension) {
		this.maxExtensionForward = extension;
		return this;
	}

	public StairwayBuilder setStairwayDirection(BlockFace stairDirection) {
		this.stairDirection = stairDirection;
		return this;
	}
	
	public StairwayBuilder setStopAtWater(boolean stopAtWater) {
		this.stopAtWater = stopAtWater;
		return this;
	}
	
	public StairwayBuilder setAngled(boolean angled) {
		this.angled = angled;
		return this;
	}

}
