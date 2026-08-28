package github.xpncvr.autocomplete.mixin;

import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static github.xpncvr.autocomplete.Main.PREDICTOR;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin {

    @Shadow @Final
    private EditBox input;

    @Shadow @Nullable
    private CommandSuggestions.SuggestionsList suggestions;

    @Unique
    private Optional<String> commandPreview = Optional.empty();

    @Unique
    private boolean acceptingPreview = false;

    @Inject(method = "updateCommandInfo", at = @At("TAIL"))
    private void predictCommandPreview(CallbackInfo ci) {
        if (this.acceptingPreview) return;
        if (this.suggestions != null) return;

        String userInput = this.input.getValue();

        if (userInput.length() <= 1) return;

        this.commandPreview = PREDICTOR.predictCommand(userInput);

        this.commandPreview.ifPresent(preview -> {
            if (preview.startsWith(userInput)) {
                this.input.setSuggestion(
                        preview.substring(userInput.length())
                );
            }
        });
    }

    @Inject(
            method = "keyPressed",
            at = @At("HEAD"),
            cancellable = true
    )
    private void acceptPreviewOnTab(
            KeyEvent event,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!event.isCycleFocus()) return;
        if (this.suggestions != null) return;

        if (this.commandPreview.isPresent()) {
            String preview = this.commandPreview.get();

            this.acceptingPreview = true;
            this.input.setValue(preview);
            this.input.setCursorPosition(preview.length());
            this.input.setHighlightPos(preview.length());
            this.input.setSuggestion(null);
            this.commandPreview = Optional.empty();
            this.acceptingPreview = false;

            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hide", at = @At("TAIL"))
    private void clearPreviewOnHide(CallbackInfo ci) {
        this.commandPreview = Optional.empty();
        this.input.setSuggestion(null);
    }
}
