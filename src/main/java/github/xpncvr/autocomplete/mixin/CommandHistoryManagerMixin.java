package github.xpncvr.autocomplete.mixin;

import net.minecraft.client.util.CommandHistoryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static github.xpncvr.autocomplete.Main.PREDICTOR;


@Mixin(CommandHistoryManager.class)
public class CommandHistoryManagerMixin {
    @Inject(method = "add", at = @At("HEAD"))
    private void onAdd(String command, CallbackInfo ci) {
        PREDICTOR.add(command);
    }
}
