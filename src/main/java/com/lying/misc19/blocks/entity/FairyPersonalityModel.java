package com.lying.misc19.blocks.entity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.compress.utils.Lists;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.lying.misc19.reference.Reference;
import com.mojang.datafixers.util.Pair;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

public class FairyPersonalityModel
{
	private static final List<MutableComponent> RANDOM_NAMES = Lists.newArrayList();
	private int emotionCool = Reference.Values.TICKS_PER_SECOND;
	private Emotion currentExpression = Emotion.NEUTRAL;
	
	private Map<Emotion, Float> emotionMap = new HashMap<>();
	
	private Map<EmotiveEvent, Pair<Emotion,Float>> model = new HashMap<>();
	
	private boolean isDirty = true;
	
	/*
	 * Randomised creator epithet
	 * Randomised speech style
	 */
	private MutableComponent name;
	
	public FairyPersonalityModel(RandomSource rand)
	{
		name = RANDOM_NAMES.get(rand.nextInt(RANDOM_NAMES.size()));
		for(EmotiveEvent event : EmotiveEvent.values())
		{
			Emotion emote = Emotion.ACTIVE_EMOTIONS[rand.nextInt(Emotion.ACTIVE_EMOTIONS.length)];
			float intensity = rand.nextFloat();
			model.put(event, Pair.of(emote, intensity));
		}
	}
	
	public void tick()
	{
		if(this.emotionCool > 0)
		{
			--this.emotionCool;
			markDirty();
		}
		
		float strongest = getIntensity(this.currentExpression);
		Emotion highest = strongest > 0 ? this.currentExpression : Emotion.NEUTRAL;
		for(Emotion emotion : currentExpression.getMoveOptions())
		{
			float intensity = getIntensity(emotion);
			if(intensity > strongest)
			{
				highest = emotion;
				strongest = intensity;
			}
		}
		degradeImpulses();
		
		if(this.emotionCool <= 0 && highest != currentExpression)
			setExpression(highest);
	}
	
	public Pair<Emotion,Float> getImpulseFor(EmotiveEvent event) { return this.model.getOrDefault(event, Pair.of(Emotion.NEUTRAL, 0F)); }
	
	public void degradeImpulses()
	{
		EnumSet<Emotion> changed = EnumSet.noneOf(Emotion.class);
		for(Emotion emote : this.emotionMap.keySet())
		{
			float value = this.emotionMap.get(emote);
			if(value > 0)
			{
				this.emotionMap.put(emote, Math.max(0F, this.emotionMap.get(emote) - 0.05F));
				changed.add(emote);
			}
		}
		if(!changed.isEmpty())
			markDirty();
	}
	
	public void addImpulse(Emotion emoteIn, float intensityIn)
	{
		if(intensityIn == 0)
			return;
		
		float intensity = getIntensity(emoteIn);
		float totalIntensity = 0;
		for(Emotion emote : this.emotionMap.keySet())
			totalIntensity += this.emotionMap.get(emote);
		float weight = totalIntensity == 0 ? 1F : 1F - (intensity / totalIntensity);
		intensity += (float)(weight * intensityIn);
		
		emotionMap.put(emoteIn, intensity);
	}
	
	public float getIntensity(Emotion emote) { return emotionMap.getOrDefault(emote, 0F); }
	
	public Emotion currentExpression() { return this.currentExpression; }
	
	public void setExpression(Emotion emote)
	{
		this.currentExpression = emote;
		this.emotionCool = Reference.Values.TICKS_PER_SECOND;
		markDirty();
	}
	
	public boolean isDirty() { return this.isDirty; }
	
	public void markDirty() { this.isDirty = true; }
	public void markClean() { this.isDirty = false; }
	
	public Map<EmotiveEvent, Pair<Emotion,Float>> getModel() { return this.model; }
	
	public CompoundTag saveToNbt(CompoundTag nbt)
	{
		nbt.putString("Name", Component.Serializer.toJson(this.name));
		nbt.putString("Expression", this.currentExpression.getSerializedName());
		nbt.putInt("ChangeCool", this.emotionCool);
		ListTag impulses = new ListTag();
		for(Emotion emote : Emotion.values())
		{
			CompoundTag tag = new CompoundTag();
			tag.putString("Name", emote.getSerializedName());
			tag.putFloat("Value", getIntensity(emote));
		}
		nbt.put("Emotions", impulses);
		
		ListTag personality = new ListTag();
		for(EmotiveEvent event : model.keySet())
		{
			CompoundTag tag = new CompoundTag();
			tag.putString("Event", event.getSerializedName());
			
			Pair<Emotion,Float> reaction = model.get(event);
			tag.putString("Reaction", reaction.getFirst().getSerializedName());
			tag.putFloat("Intensity", reaction.getSecond());
			
			personality.add(tag);
		}
		nbt.put("Personality", personality);
		return nbt;
	}
	
	public void readFromNbt(CompoundTag nbt)
	{
		if(nbt.contains("Name", Tag.TAG_STRING))
			try
			{
				this.name = Component.Serializer.fromJson(nbt.getString("Name"));
			}
			catch(Exception e) { }
		
		this.currentExpression = Emotion.fromName(nbt.getString("Expression"));
		this.emotionCool = nbt.getInt("ChangeCool");
		
		this.emotionMap.clear();
		ListTag impulses = nbt.getList("Emotions", Tag.TAG_COMPOUND);
		for(int i=0; i<impulses.size(); i++)
		{
			CompoundTag tag = impulses.getCompound(i);
			emotionMap.put(Emotion.fromName(tag.getString("Name")), tag.getFloat("Value"));
		}
		
		this.model.clear();
		ListTag personality = nbt.getList("Personality", Tag.TAG_COMPOUND);
		for(int i=0; i<personality.size(); i++)
		{
			CompoundTag tag = personality.getCompound(i);
			EmotiveEvent event = EmotiveEvent.fromName(tag.getString("Event"));
			if(event == null)
				continue;
			
			Emotion reaction = Emotion.fromName(tag.getString("Reaction"));
			float intensity = tag.getFloat("Intensity");
			model.put(event, Pair.of(reaction, intensity));
		}
	}
	
	public MutableComponent name() { return this.name; }
	
	public void setName(MutableComponent nameIn)
	{
		this.name = nameIn;
		this.markDirty();
	}
	
	public CompoundTag saveToBlockTag(CompoundTag nbt)
	{
		CompoundTag tag = saveToNbt(new CompoundTag());
		nbt.put("Personality", tag);
		return nbt;
	}
	
	private static void addRandomName(String literal) { RANDOM_NAMES.add(Component.literal(literal)); }
	
	static
	{
		addRandomName("Col Tom Blue");
		addRandomName("Thistledown");
		addRandomName("Caliban");
		addRandomName("Aoife");
		addRandomName("Angus");
		addRandomName("Bran");
		addRandomName("Brighid");
		addRandomName("Dana");
		addRandomName("Diana");
		addRandomName("Diarmuid");
		addRandomName("Morrigan");
		addRandomName("Oisin");
		addRandomName("Rhiannon");
		addRandomName("Dagda");
		addRandomName("Danu");
		addRandomName("Lugh");
		addRandomName("Aife");
		addRandomName("CuChulainn");
		addRandomName("Aengus");
		addRandomName("Medb");
		addRandomName("Macha");
		addRandomName("Deirde");
		addRandomName("Cian");
		addRandomName("Brian");
		addRandomName("Cormac");
		addRandomName("Cernunnos");
		addRandomName("Édaín");
		addRandomName("Clíodhna");
		addRandomName("Caoimhe");
		addRandomName("Bébinn");
		addRandomName("Manannán mac Lir");
		addRandomName("Lir");
		addRandomName("Diarmuid Ua Duibhne");
		addRandomName("Epona");
		addRandomName("Ogma");
	}
	
	public static enum EmotiveEvent implements StringRepresentable
	{
		SHAKE_JAR_OWN,
		SHAKE_JAR_OTHER,
		MOB_HURT,
		OWNER_HURT,
		PLAYER_HURT,
		TRAVEL;
		
		public String getSerializedName() { return this.name().toLowerCase(); }
		
		@Nullable
		public static EmotiveEvent fromName(String nameIn)
		{
			for(EmotiveEvent emote : values())
				if(emote.getSerializedName().equalsIgnoreCase(nameIn))
					return emote;
			return null;
		}
	}
	
	public static enum Emotion implements StringRepresentable
	{
		NEUTRAL,
		HAPPY,
		ANGRY,
		SAD,
		SCARED;
		
		public static Emotion[] ACTIVE_EMOTIONS = new Emotion[] {HAPPY, ANGRY, SAD, SCARED};
		
		private static final Map<Emotion, EnumSet<Emotion>> MOVE_MAP = new HashMap<>();
		
		public String getSerializedName() { return this.name().toLowerCase(); }
		
		public ResourceLocation getTexture() { return new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/fairy_jar/"+getSerializedName()+".png"); }
		
		public boolean canMoveInto(Emotion emote) { return getMoveOptions().contains(emote); }
		
		public EnumSet<Emotion> getMoveOptions() { return MOVE_MAP.get(this); }
		
		@NonNull
		public static Emotion fromName(String nameIn)
		{
			for(Emotion emote : values())
				if(emote.getSerializedName().equalsIgnoreCase(nameIn))
					return emote;
			return NEUTRAL;
		}
		
		private static void addMoves(Emotion emote, Emotion... moves)
		{
			EnumSet<Emotion> moveSet = EnumSet.noneOf(Emotion.class);
			for(Emotion emotion : moves)
				moveSet.add(emotion);
			
			MOVE_MAP.put(emote, moveSet);
		}
		
		static
		{
			addMoves(NEUTRAL, HAPPY, ANGRY, SAD, SCARED);
			addMoves(HAPPY, NEUTRAL, SCARED, ANGRY);
			addMoves(SCARED, NEUTRAL, ANGRY);
			addMoves(ANGRY, NEUTRAL, SCARED);
			addMoves(SAD, NEUTRAL, SCARED, ANGRY);
		}
	}
}