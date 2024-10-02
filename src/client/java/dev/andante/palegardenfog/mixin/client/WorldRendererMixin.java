package dev.andante.palegardenfog.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;
import java.util.function.Supplier;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
	@Unique
	private static final RegistryKey<Biome> PALE_GARDEN_KEY = RegistryKey.of(RegistryKeys.BIOME, Identifier.ofVanilla("pale_garden"));

	@Redirect(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/BackgroundRenderer;applyFog(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/BackgroundRenderer$FogType;Lorg/joml/Vector4f;FZF)Lnet/minecraft/client/render/Fog;"
			)
	)
	private Fog onRenderFog(Camera camera, BackgroundRenderer.FogType fogType, Vector4f color, float viewDistance, boolean thickenFog, float tickDelta) {
		return useThickFog(() -> BackgroundRenderer.applyFog(camera, fogType, color, viewDistance, thickenFog, tickDelta), camera, fogType, color, tickDelta).get();
	}

	@Unique
	private Supplier<Fog> useThickFog(Supplier<Fog> original, Camera camera, BackgroundRenderer.FogType fogType, Vector4f color, float tickDelta) {
		MinecraftClient client = MinecraftClient.getInstance();
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
			return () -> BackgroundRenderer.applyFog(camera, fogType, new Vector4f(138 / 255.0f, 131 / 255.0f, 127 / 255.0f, 1.0f), 1.5f * 16, true, tickDelta);
		}

		return original;
	}
}
