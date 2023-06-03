package com.lying.misc19.client.particle;

import com.lying.misc19.reference.Reference;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;

public class SquareParticle extends TextureSheetParticle
{
	private static final double ROTATION_VEL = 0.01D;
	private double pitch, yaw, roll;
	private double pitchD, yawD, rollD;
	
	protected SquareParticle(ClientLevel world, double posX, double posY, double posZ, double velX, double velY, double velZ)
	{
		super(world, posX, posY, posZ, velX, velY, velZ);
		this.hasPhysics = false;
		this.lifetime = (int)(Reference.Values.TICKS_PER_SECOND * (0.8F + random.nextFloat() * 0.2F));
		
		// TODO Orient square to face direction of travel initially
		
		this.pitchD = random.nextFloat() * ROTATION_VEL;
		this.yawD = random.nextFloat() * ROTATION_VEL;
		this.rollD = random.nextFloat() * ROTATION_VEL;
	}
	
	// TODO Render particle with rotation
	public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }
	
	public void tick()
	{
		this.alpha = (float)age / lifetime;
		
		Vec3 vel = new Vec3(xd, yd, zd);
		if(vel.length() > 1D)
			vel.normalize();
		vel = vel.scale(1F - alpha);
		this.xd = vel.x;
		this.yd = vel.y;
		this.zd = vel.z;
		
		this.pitch += this.pitchD;
		this.yaw += this.yawD;
		this.roll += this.rollD;
		
		super.tick();
	}
	
	public static class Factory implements ParticleProvider<SimpleParticleType>
	{
		public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
		{
			return new SquareParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
		}
	}
}
