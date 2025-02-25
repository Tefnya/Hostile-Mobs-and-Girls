package com.github.mechalopa.hmag.client.renderer;

import com.github.mechalopa.hmag.HMaG;
import com.github.mechalopa.hmag.client.ModModelLayers;
import com.github.mechalopa.hmag.client.model.AbstractGirlModel;
import com.github.mechalopa.hmag.client.model.SkeletonGirlArmorModel;
import com.github.mechalopa.hmag.client.model.SkeletonGirlModel;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkeletonGirlRenderer extends AbstractGirlRenderer<AbstractSkeleton, AbstractGirlModel<AbstractSkeleton>>
{
	private static final ResourceLocation TEX = new ResourceLocation(HMaG.MODID, "textures/entity/skeleton_girl.png");

	public SkeletonGirlRenderer(EntityRendererProvider.Context context)
	{
		this(context, ModModelLayers.SKELETON_GIRL, ModModelLayers.SKELETON_GIRL_INNER_ARMOR, ModModelLayers.SKELETON_GIRL_OUTER_ARMOR);
	}

	public SkeletonGirlRenderer(EntityRendererProvider.Context context, ModelLayerLocation location, ModelLayerLocation location1, ModelLayerLocation location2)
	{
		this(context, new SkeletonGirlModel<>(context.bakeLayer(location)), new SkeletonGirlArmorModel<>(context.bakeLayer(location1)), new SkeletonGirlArmorModel<>(context.bakeLayer(location2)));
	}

	public SkeletonGirlRenderer(EntityRendererProvider.Context context, SkeletonGirlModel<AbstractSkeleton> model, AbstractGirlModel<AbstractSkeleton> model1, AbstractGirlModel<AbstractSkeleton> model2)
	{
		super(context, model, 0.5F);
		this.addLayer(new HumanoidArmorLayer<>(this, model1, model2, context.getModelManager()));
	}

	@Override
	public ResourceLocation getTextureLocation(AbstractSkeleton entity)
	{
		return TEX;
	}

	@Override
	protected boolean isShaking(AbstractSkeleton entity)
	{
		return entity.isShaking();
	}
}