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
public abstract class ChatInputSuggestorMixin {

    @Shadow @Final
    private EditBox input;

    @Shadow
    private boolean keepSuggestions;

    @Shadow @Nullable
    private CommandSuggestions.SuggestionsList suggestions;

    @Shadow
    private boolean allowSuggestions;

    @Unique
    private Optional<String> commandPreview = Optional.empty();


    @Inject(method = "updateCommandInfo", at = @At("TAIL"))
    private void predictCommandPreview(CallbackInfo ci) {
        if (this.keepSuggestions) return;
        if (this.suggestions != null) return;
        if (!this.allowSuggestions) return;

        String input = this.input.getValue();

        if (input.length() <= 1) return;

        this.commandPreview = PREDICTOR.predictCommand(input);

        this.commandPreview.ifPresent(preview -> {
            if (preview.startsWith(input)) {
                this.input.setSuggestion(
                        preview.substring(input.length())
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
            KeyEvent input,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!input.isCycleFocus()) return;
        if (this.suggestions != null) return;

        if (this.commandPreview.isPresent()) {
            String preview = this.commandPreview.get();

            this.keepSuggestions = true;
            this.input.setValue(preview);
            this.input.setCursorPosition(preview.length());
            this.input.setHighlightPos(preview.length());
            this.input.setSuggestion(null);
            this.commandPreview = Optional.empty();
            this.keepSuggestions = false;

            cir.setReturnValue(true);
        }
    }

    @Inject(method = "setAllowSuggestions", at = @At("TAIL"))
    private void clearPreviewOnClose(boolean active, CallbackInfo ci) {
        if (!active) {
            this.commandPreview = Optional.empty();
            this.input.setSuggestion(null);
        }
    }
}
