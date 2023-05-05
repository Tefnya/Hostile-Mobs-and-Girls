package com.github.mechalopa.hmag.world.entity;

import javax.annotation.Nonnull;

import com.github.mechalopa.hmag.registry.ModSoundEvents;
import com.github.mechalopa.hmag.world.entity.ai.goal.MeleeAttackGoal2;
import com.github.mechalopa.hmag.world.entity.projectile.InkSpitEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.network.NetworkHooks;

public class SwamperEntity extends Monster implements RangedAttackMob
{
	private static final EntityDataAccessor<Integer> SHOULD_SPIT_TIMER = SynchedEntityData.defineId(SwamperEntity.class, EntityDataSerializers.INT);

	public SwamperEntity(EntityType<? extends SwamperEntity> type, Level level)
	{
		super(type, level);
		this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
		this.xpReward = 12;
	}

	@Override
	protected void defineSynchedData()
	{
		super.defineSynchedData();
		this.entityData.define(SHOULD_SPIT_TIMER, 0);
	}

	@Override
	protected void registerGoals()
	{
		this.goalSelector.addGoal(1, new SwamperEntity.SwamperFloatGoal(this));
		this.goalSelector.addGoal(3, new SwamperEntity.SwamperMeleeAttackGoal(this, 1.2D, false, 8.0F / 9.0F, 6.0F));
		this.goalSelector.addGoal(4, new RangedAttackGoal(this, 1.0D, 40, 60, 8.0F));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
		this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Mob.class, 8.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
	}

	public static AttributeSupplier.Builder createAttributes()
	{
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 40.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.17D)
				.add(Attributes.ATTACK_DAMAGE, 6.0D)
				.add(Attributes.ARMOR, 4.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
	}

	@Override
	public boolean canBreatheUnderwater()
	{
		return true;
	}

	@Override
	public void aiStep()
	{
		if (this.level.isClientSide && this.isInWater() && this.getRandom().nextInt(4) == 0)
		{
			this.level.addParticle(ParticleTypes.BUBBLE, this.getRandomX(0.75D), this.getY(this.getRandom().nextDouble() * 0.75D) - 0.1D, this.getRandomZ(0.75D), 0.0D, 0.0D, 0.0D);
		}

		super.aiStep();

		if (!this.level.isClientSide)
		{
			int i = Mth.floor(this.getX());
			int j = Mth.floor(this.getY());
			int k = Mth.floor(this.getZ());
			BlockPos blockpos = new BlockPos(i, j, k);

			if (!this.isInWaterRainOrBubble() && this.level.getBiome(blockpos).value().shouldSnowGolemBurn(blockpos))
			{
				this.hurt(DamageSource.ON_FIRE, 1.0F);
			}

			if (this.getTarget() != null && this.getTarget().isAlive() && this.getTarget().getHealth() <= 1.0F)
			{
				this.setShouldSpitTimer(Math.max(20, this.getShouldSpitTimer() - 1));
			}
			else if (this.getShouldSpitTimer() > 0)
			{
				if (this.getTarget() != null && this.getTarget().isAlive())
				{
					this.setShouldSpitTimer(this.getShouldSpitTimer() - 1);
				}
				else
				{
					this.setShouldSpitTimer(0);
				}
			}
		}
	}

	@Override
	public boolean doHurtTarget(Entity entity)
	{
		if (super.doHurtTarget(entity))
		{
			if (entity instanceof LivingEntity)
			{
				int i = 0;

				if (this.level.getDifficulty() == Difficulty.NORMAL)
				{
					i = 7;
				}
				else if (this.level.getDifficulty() == Difficulty.HARD)
				{
					i = 15;
				}

				if (i > 0)
				{
					((LivingEntity)entity).addEffect(new MobEffectInstance(MobEffects.POISON, i * 20, 0));
					((LivingEntity)entity).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, i * 20, 0));
				}
			}

			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public boolean isPushedByFluid()
	{
		return false;
	}

	@Override
	protected float getWaterSlowDown()
	{
		return 0.98F;
	}

	@Override
	public void performRangedAttack(LivingEntity target, float distance)
	{
		InkSpitEntity inkspit = new InkSpitEntity(this.level, this);
		double d0 = target.getEyeY() - 1.1F;
		double d1 = target.getX() - this.getX();
		double d2 = d0 - inkspit.getY();
		double d3 = target.getZ() - this.getZ();
		double d4 = Math.sqrt(d1 * d1 + d3 * d3) * 0.2D;
		inkspit.shoot(d1, d2 + d4, d3, 1.4F, 12.0F);
		inkspit.setDamage(2.0F);
		this.level.addFreshEntity(inkspit);
		this.playSound(ModSoundEvents.SWAMPER_SQUIRT.get(), this.getSoundVolume(), this.getVoicePitch());
		this.setShouldSpitTimer(0);
	}

	@Override
	public boolean hurt(DamageSource source, float amount)
	{
		if (source.isFire())
		{
			amount = amount * 1.5F;
		}
		else if (source == DamageSource.FALL)
		{
			amount = amount * 0.5F;
		}

		if (super.hurt(source, amount))
		{
			if (!this.level.isClientSide && !this.isNoAi() && this.getShouldSpitTimer() <= 0 && source.getDirectEntity() != null && source.getDirectEntity() instanceof LivingEntity && this.getTarget() != null && source.getDirectEntity().equals(this.getTarget()) && !this.getTarget().hasEffect(MobEffects.BLINDNESS))
			{
				this.setShouldSpitTimer(60);
			}

			return true;
		}
		else
		{
			return false;
		}
	}

	public int getShouldSpitTimer()
	{
		return this.entityData.get(SHOULD_SPIT_TIMER);
	}

	private void setShouldSpitTimer(int value)
	{
		this.entityData.set(SHOULD_SPIT_TIMER, value);
	}

	@Override
	public int getMaxSpawnClusterSize()
	{
		return 3;
	}

	@Override
	public int getMaxHeadXRot()
	{
		return 6;
	}

	@Override
	public int getMaxHeadYRot()
	{
		return 12;
	}

	@Override
	public double getMyRidingOffset()
	{
		return 0.1D;
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions size)
	{
		return 0.9F;
	}

	@Override
	protected SoundEvent getAmbientSound()
	{
		return ModSoundEvents.SWAMPER_AMBIENT.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource)
	{
		return ModSoundEvents.SWAMPER_HURT.get();
	}

	@Override
	protected SoundEvent getDeathSound()
	{
		return ModSoundEvents.SWAMPER_DEATH.get();
	}

	@Nonnull
	@Override
	public Packet<?> getAddEntityPacket()
	{
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	private class SwamperFloatGoal extends FloatGoal
	{
		private final SwamperEntity mob;

		public SwamperFloatGoal(SwamperEntity mob)
		{
			super(mob);
			this.mob = mob;
		}

		@Override
		public boolean canUse()
		{
			return super.canUse() && this.mob.getTarget() != null && this.mob.getTarget().isAlive() && !this.mob.getTarget().isEyeInFluidType(ForgeMod.WATER_TYPE.get());
		}
	}

	private class SwamperMeleeAttackGoal extends MeleeAttackGoal2
	{
		private final SwamperEntity mob;

		public SwamperMeleeAttackGoal(SwamperEntity mob, double speed, boolean useLongMemory, float reachScale, float maxAttackDistance)
		{
			super(mob, speed, useLongMemory, reachScale, maxAttackDistance);
			this.mob = mob;
		}

		@Override
		public boolean canUse()
		{
			return super.canUse() && this.mob.getShouldSpitTimer() <= 0;
		}

		@Override
		public boolean canContinueToUse()
		{
			return super.canContinueToUse() && this.mob.getShouldSpitTimer() <= 0;
		}
	}
}