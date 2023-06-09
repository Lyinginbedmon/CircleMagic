package com.lying.circles.blocks.entity;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class FairyLookHelper
{
	/** How quickly the fairy turns to look at something */
	private static final float FACE_SPEED = 8F;
	
	private static final AABB LOOK_RANGE = new AABB(-1, -1, -1, 1, 1, 1).inflate(16D);
	private static final Predicate<LivingEntity> VALID_LOOK = (entity) -> entity.isAlive() && !entity.isSpectator();
	
	private float lookPitch, lookYaw = 0F;
	
	private float oldPitch, oldYaw = 0F;
	
	/** Ticks until the fairy can look somewhere else */
	private int lookTicks = 0;
	private float targetPitch = 0F;
	private float targetYaw = 0F;
	
	/** Current entity look target */
	private Optional<LivingEntity> lookTarget = Optional.empty();
	
	public FairyLookHelper(RandomSource random)
	{
		setRandomLook(random);
		lookPitch = oldPitch = targetPitch;
		lookYaw = oldYaw = targetYaw;
	}
	
	public void tick(Vec3 orbPos, Level world, RandomSource random)
	{
		oldPitch = lookPitch;
		oldYaw = lookYaw;
		
		lookPitch = getLimited(oldPitch, targetPitch, 1F);
		lookYaw = getLimited(oldYaw, targetYaw, 1F);
		
		if(lookTicks > 0)
		{
			lookTicks--;
			
			this.lookTarget.ifPresent((entity) -> 
			{
				if(VALID_LOOK.apply(entity))
					setLookDir(entity.getEyePosition().subtract(orbPos));
				else
					clearLookTarget();
			});
			if(lookTicks == 0)
				targetPitch = 0;
		}
		else if(random.nextInt(50) == 0)
		{
			// Set a random look target
			lookTarget = Optional.empty();
			
			// Look at random nearby mob
			if(random.nextInt(3) > 0)
			{
				List<LivingEntity> mobs = world.getEntitiesOfClass(LivingEntity.class, LOOK_RANGE.move(orbPos), VALID_LOOK);
				if(!mobs.isEmpty())
				{
					this.lookTarget = Optional.of(mobs.get(random.nextInt(mobs.size())));
					this.lookTicks = random.nextInt(30) + 60;
				}
			}
			
			// Look at random position
			if(!lookTarget.isPresent())
			{
				setRandomLook(random);
				this.lookTicks = random.nextInt(20) + 30;
			}
		}
	}
		
	private void clearLookTarget()
	{
		this.lookTarget = Optional.empty();
	}
	
	private void setRandomLook(RandomSource random)
	{
		setLookDir((random.nextFloat() - 0.5F) * 60F, random.nextFloat() * 360F);
	}
	
	private float getLimited(float current, float target, float partialTicks)
	{
		float delta = target - current;
		while(delta < -180F)
			delta += 360F;
		while(delta > 180F)
			delta -= 360F;
		
		return current + (Mth.clamp(delta, -FACE_SPEED, FACE_SPEED) * partialTicks);
	}
	
	public float yaw(float partialTicks) { return getLimited(lookYaw, targetYaw, partialTicks); }
	
	public float pitch(float partialTicks) { return getLimited(lookPitch, targetPitch, partialTicks); }
	
	/** Converts a direction vector to corresponding pitch and yaw values */
	public void setLookDir(@Nullable Vec3 vecIn)
	{
		if(vecIn == null) return;
		vecIn = vecIn.normalize();
		setLookDir((float)Math.toDegrees(Math.asin(-vecIn.y)), (float)Math.toDegrees(Math.atan2(vecIn.x, vecIn.z)));
	}
	
	public void setLookDir(float pitch, float yaw)
	{
		this.targetPitch = Mth.clamp(pitch, -30, 80);
		this.targetYaw = yaw;
	}
	
	/** Forces the fairy to look in the given direction for 4 seconds */
	public void forceLookAt(Vec3 direction)
	{
		setLookDir(direction);
		this.lookTicks = 80;
		this.lookTarget = Optional.empty();
	}
}