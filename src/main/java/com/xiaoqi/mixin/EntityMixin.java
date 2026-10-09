package com.xiaoqi.mixin;

import com.xiaoqi.ducks.LazyEntityTracker;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public abstract class EntityMixin implements LazyEntityTracker {
	@Unique
	private boolean carpetVisualFix$isLazy = false;

	@Override
	public boolean carpetVisualFix$isLazy() {
		return this.carpetVisualFix$isLazy;
	}

	@Override
	public void carpetVisualFix$setLazy(boolean lazy) {
		this.carpetVisualFix$isLazy = lazy;
	}
}
