package github.xpncvr.autocomplete.mixin;

import net.minecraft.client.CommandHistory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static github.xpncvr.autocomplete.Main.PREDICTOR;

@Mixin(CommandHistory.class)
public class CommandHistoryMixin {
    @Inject(method = "addCommand", at = @At("HEAD"))
    private void onAdd(String command, CallbackInfo ci) {
        PREDICTOR.add(command);
    }
}
