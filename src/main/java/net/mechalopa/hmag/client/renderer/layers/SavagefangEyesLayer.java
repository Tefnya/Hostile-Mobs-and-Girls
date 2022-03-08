package net.mechalopa.hmag.client.renderer.layers;

import net.mechalopa.hmag.HMaG;
import net.mechalopa.hmag.client.model.SavagefangModel;
import net.mechalopa.hmag.entity.SavagefangEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.AbstractEyesLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SavagefangEyesLayer<T extends SavagefangEntity, M extends SavagefangModel<T>> extends AbstractEyesLayer<T, M>
{
	private static final RenderType RENDER_TYPE = RenderType.eyes(new ResourceLocation(HMaG.MODID, "textures/entity/savagefang_eyes.png"));

	public SavagefangEyesLayer(IEntityRenderer<T, M> rendererIn)
	{
		super(rendererIn);
	}

	@Override
	public RenderType renderType()
	{
		return RENDER_TYPE;
	}
}