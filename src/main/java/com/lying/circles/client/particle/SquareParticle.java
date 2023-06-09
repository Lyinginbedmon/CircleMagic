package com.lying.circles.client.particle;

import com.lying.circles.reference.Reference;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class SquareParticle extends TextureSheetParticle
{
	private static final float ROTATION_VEL = 0.08F;
	private static final double SPEED = 0.02D;
	private static final float SIZE = 0.5F / 16F;
	private final SpriteSet sprites;
	private float pitch, yaw, roll;
	private double pitchD, yawD, rollD;
	
	private final Vec3 direction;
	
	protected SquareParticle(ClientLevel world, double posX, double posY, double posZ, double velX, double velY, double velZ, SpriteSet spritesIn)
	{
		super(world, posX, posY, posZ, velX, velY, velZ);
		this.setSpriteFromAge(spritesIn);
		this.sprites = spritesIn;
		this.hasPhysics = false;
		this.gravity = 0F;
		this.speedUpWhenYMotionIsBlocked = false;
		this.lifetime = (int)(Reference.Values.TICKS_PER_SECOND * (0.8F + random.nextFloat() * 0.2F));
		this.quadSize = SIZE;
		setSize(SIZE, SIZE);
		
		this.direction = new Vec3(velX, velY, velZ).normalize().scale(SPEED);
		setParticleSpeed(0D, 0D, 0D);
		updateVelocity();
		updateAlpha();
		
		this.pitchD = ROTATION_VEL * ((random.nextFloat() - 0.5F) * 2F);
		this.yawD = ROTATION_VEL * ((random.nextFloat() - 0.5F) * 2F);
		this.rollD = ROTATION_VEL * ((random.nextFloat() - 0.5F) * 2F);
		
		this.pitch = (float)(Math.asin(-velY));
		this.yaw = (float)(Math.atan2(velX, velZ));
		this.roll = 0F;
	}
	
	public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }
	
	public void updateVelocity()
	{
		if(age == 4)
			setParticleSpeed(direction.x, direction.y, direction.z);
		else
		{
			double speedX = xd == 0 ? 0D : SPEED * Math.signum(xd);
			double speedY = yd == 0 ? 0D : SPEED * Math.signum(yd);
			double speedZ = zd == 0 ? 0D : SPEED * Math.signum(zd);
			setParticleSpeed(speedX, speedY, speedZ);
		}
	}
	
	public void updateAlpha()
	{
		if(age < (lifetime - 7))
			setAlpha(Mth.clamp((float)age / 4F, 0F, 1F));
		else
		{
			float point = (float)(age - (lifetime - 7));
			setAlpha(1F - (point / 7F));
		}
	}
	
	public void tick()
	{
		setSpriteFromAge(sprites);
		
		updateAlpha();
		updateVelocity();
		
		if(age >= 4)
		{
			this.pitch += this.pitchD;
			this.yaw += this.yawD;
			this.roll += this.rollD;
		}
		
		super.tick();
	}
	
	public int getLightColor(float partialTicks)
	{
		return 255;
	}
	
	public void render(VertexConsumer consumer, Camera camera, float partialTicks)
	{
		Vec3 camPos = camera.getPosition();
		float posX = (float)(Mth.lerp((double)partialTicks, this.xo, this.x)- camPos.x());
		float posY = (float)(Mth.lerp((double)partialTicks, this.yo, this.y)- camPos.y());
		float posZ = (float)(Mth.lerp((double)partialTicks, this.zo, this.z)- camPos.z());
		
		Quaternion quaternion = Vector3f.YP.rotation(this.yaw);
		quaternion.mul(Vector3f.XP.rotation(this.pitch));
		quaternion.mul(Vector3f.ZP.rotation(this.roll));
		
		float quadSize = getQuadSize(partialTicks);
		Vector3f[] vertices = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0F), new Vector3f(-1.0F, 1.0F, 0F), new Vector3f(1.0F, 1.0F, 0F), new Vector3f(1.0F, -1.0F, 0F)};
		for(int i=0; i<4; ++i)
		{
			Vector3f vec = vertices[i];
			vec.transform(quaternion);
			vec.mul(quadSize);
			vec.add(posX, posY, posZ);
		}
		
		float u0 = this.getU0();
		float u1 = this.getU1();
		float v0 = this.getV0();
		float v1 = this.getV1();
		int light = this.getLightColor(partialTicks);
		
		consumer.vertex((double)vertices[0].x(), (double)vertices[0].y(), (double)vertices[0].z()).uv(u1, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
		consumer.vertex((double)vertices[1].x(), (double)vertices[1].y(), (double)vertices[1].z()).uv(u1, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
		consumer.vertex((double)vertices[2].x(), (double)vertices[2].y(), (double)vertices[2].z()).uv(u0, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
		consumer.vertex((double)vertices[3].x(), (double)vertices[3].y(), (double)vertices[3].z()).uv(u0, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
		
		consumer.vertex((double)vertices[3].x(), (double)vertices[3].y(), (double)vertices[3].z()).uv(u0, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
		consumer.vertex((double)vertices[2].x(), (double)vertices[2].y(), (double)vertices[2].z()).uv(u0, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
		consumer.vertex((double)vertices[1].x(), (double)vertices[1].y(), (double)vertices[1].z()).uv(u1, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
		consumer.vertex((double)vertices[0].x(), (double)vertices[0].y(), (double)vertices[0].z()).uv(u1, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
	}
	
	public static class Factory implements ParticleProvider<SimpleParticleType>
	{
		private final SpriteSet sprites;
		
		public Factory(SpriteSet spritesIn)
		{
			this.sprites = spritesIn;
		}
		
		public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
		{
			return new SquareParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
		}
	}
}
