package red.jackf.chesttracker.mixins;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.chesttracker.api.providers.ProviderUtils;

@Debug(export = true)
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin extends ClientCommonPacketListenerImpl {
    protected ClientPacketListenerMixin(Minecraft minecraft, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraft, connection, commonListenerCookie);
        throw new AssertionError("mixin apply failed");
    }

    @Inject(method = "handleRespawn", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V",
            shift = At.Shift.AFTER))
    private void getDimensionChange(ClientboundRespawnPacket packet,
                                    CallbackInfo ci,
                                    @Share("ct_result") LocalRef<Pair<ResourceKey<Level>, ResourceKey<Level>>> result) {
        //noinspection DataFlowIssue
        ResourceKey<Level> oldLevel = this.minecraft.player.level().dimension();
        ResourceKey<Level> newLevel = packet.commonPlayerSpawnInfo().dimension();
        result.set(Pair.of(oldLevel, newLevel));
    }

    @Inject(method = "handleRespawn", at = @At("RETURN"))
    private void pushRespawnEvent(ClientboundRespawnPacket packet,
                                  CallbackInfo ci,
                                  @Share("ct_result") LocalRef<Pair<ResourceKey<Level>, ResourceKey<Level>>> resultShare) {
        Pair<ResourceKey<Level>, ResourceKey<Level>> result = resultShare.get();
        if (result == null) return;
        ProviderUtils.getCurrentProvider().ifPresent(provider -> {
            provider.onRespawn(result.getFirst(), result.getSecond());
        });
    }
}
