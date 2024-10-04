package dev.andante.palegardenfog.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
	@Unique
	private static final RegistryKey<Biome> PALE_GARDEN_KEY = RegistryKey.of(RegistryKeys.BIOME, Identifier.ofVanilla("pale_garden"));

	@Unique
	private static final Vector4f PALE_COLOR_VECTOR = new Vector4f(138 / 255.0f, 131 / 255.0f, 127 / 255.0f, 1.0f);

	@Unique
	private static final int PALE_COLOR_DECIMAL = 0x8A837F;

	@ModifyExpressionValue(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/BackgroundRenderer;applyFog(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/BackgroundRenderer$FogType;Lorg/joml/Vector4f;FZF)Lnet/minecraft/client/render/Fog;",
					ordinal = 0
			)
	)
	private Fog onTerrainFog(Fog original) {
		return useThickFog(original, BackgroundRenderer.FogType.FOG_TERRAIN);
	}

	@ModifyExpressionValue(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/BackgroundRenderer;applyFog(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/BackgroundRenderer$FogType;Lorg/joml/Vector4f;FZF)Lnet/minecraft/client/render/Fog;",
					ordinal = 1
			)
	)
	private Fog onSkyFog(Fog original) {
		return useThickFog(original, BackgroundRenderer.FogType.FOG_SKY);
	}

	@ModifyExpressionValue(
			method = "method_62215",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/DimensionEffects;getSkyColor(F)I"
			)
	)
	private int onDimensionSkyColor(int original) {
		return isInPaleGarden() ? PALE_COLOR_DECIMAL : original;
	}

	@ModifyExpressionValue(
			method = "method_62215",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/world/ClientWorld;getSkyColor(Lnet/minecraft/util/math/Vec3d;F)I"
			)
	)
	private int onWorldSkyColor(int original) {
		return isInPaleGarden() ? PALE_COLOR_DECIMAL : original;
	}

	@ModifyExpressionValue(
			method = "method_62215",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/WorldRenderer;isSkyDark(F)Z"
			)
	)
	private boolean onIsSkyDark(boolean original) {
		return !isInPaleGarden() && original;
	}

	@Unique
	private Fog useThickFog(Fog original, BackgroundRenderer.FogType fogType) {
		if (isInPaleGarden()) {
			MinecraftClient client = MinecraftClient.getInstance();
			GameRenderer renderer = client.gameRenderer;
			Camera camera = renderer.getCamera();
			float tickDelta = client.getRenderTickCounter().getTickDelta(false);
			return BackgroundRenderer.applyFog(camera, fogType, PALE_COLOR_VECTOR, 1.5f * 16, true, tickDelta);
		}

		return original;
	}

	@Unique
	private boolean isInPaleGarden() {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientWorld world = client.world;
		ClientPlayerEntity player = client.player;

		if (player == null || world == null) {
			return false;
		}

		BlockPos blockPos = player.getBlockPos();
		RegistryEntry<Biome> biomeEntry = world.getBiome(blockPos);
		Optional<RegistryKey<Biome>> biomeKeyOptional = biomeEntry.getKey();

		if (biomeKeyOptional.isEmpty()) {
			return false;
		}

		RegistryKey<Biome> biomeKey = biomeKeyOptional.get();

		return biomeKey == PALE_GARDEN_KEY;
	}
}
