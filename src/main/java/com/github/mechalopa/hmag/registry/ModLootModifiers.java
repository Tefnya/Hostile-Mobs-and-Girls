package com.github.mechalopa.hmag.registry;

import com.github.mechalopa.hmag.HMaG;
import com.github.mechalopa.hmag.world.level.storage.loot.modifiers.AdditionalEntityLootModifier;
import com.github.mechalopa.hmag.world.level.storage.loot.modifiers.ReplaceLootModifier;
import com.mojang.serialization.Codec;

import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModLootModifiers
{
	private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> REGISTRY = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, HMaG.MODID);

	public static final RegistryObject<Codec<AdditionalEntityLootModifier>> ADDITIONAL_ENTITY_LOOT = REGISTRY.register("additional_entity_loot", AdditionalEntityLootModifier.CODEC);
	public static final RegistryObject<Codec<ReplaceLootModifier>> REPLACE_LOOT = REGISTRY.register("replace_loot", ReplaceLootModifier.CODEC);

	@SubscribeEvent
	public static void register(IEventBus eventBus)
	{
		REGISTRY.register(eventBus);
	}
}