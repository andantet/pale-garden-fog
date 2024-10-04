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

	@Unique
	private Fog useThickFog(Fog original, BackgroundRenderer.FogType fogType) {
		MinecraftClient client = MinecraftClient.getInstance();
		GameRenderer renderer = client.gameRenderer;
		ClientWorld world = client.world;
		ClientPlayerEntity player = client.player;

		if (player == null || world == null) {
			return original;
		}

		BlockPos blockPos = player.getBlockPos();
		RegistryEntry<Biome> biomeEntry = world.getBiome(blockPos);
		Optional<RegistryKey<Biome>> biomeKeyOptional = biomeEntry.getKey();

		if (biomeKeyOptional.isEmpty()) {
			return original;
		}

		RegistryKey<Biome> biomeKey = biomeKeyOptional.get();

		if (biomeKey == PALE_GARDEN_KEY) {
			Camera camera = renderer.getCamera();
			float tickDelta = client.getRenderTickCounter().getTickDelta(false);
			return BackgroundRenderer.applyFog(camera, fogType, new Vector4f(138 / 255.0f, 131 / 255.0f, 127 / 255.0f, 1.0f), 1.5f * 16, true, tickDelta);
		}

		return original;
	}
}
