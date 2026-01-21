package github.xpncvr.autocomplete.mixin;

import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
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

@Mixin(ChatInputSuggestor.class)
public abstract class ChatInputSuggestorMixin {

    @Shadow @Final
    private TextFieldWidget textField;

    @Shadow
    private boolean completingSuggestions;

    @Shadow @Nullable
    private ChatInputSuggestor.SuggestionWindow window;

    @Shadow
    private boolean windowActive;

    @Unique
    private Optional<String> commandPreview = Optional.empty();


    @Inject(method = "refresh", at = @At("TAIL"))
    private void predictCommandPreview(CallbackInfo ci) {
        if (this.completingSuggestions) return;
        if (this.window != null) return;
        if (!this.windowActive) return;

        String input = this.textField.getText();

        if (input.length() <= 1) return;

        this.commandPreview = PREDICTOR.predictCommand(input);

        this.commandPreview.ifPresent(preview -> {
            if (preview.startsWith(input)) {
                this.textField.setSuggestion(
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
            KeyInput input,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!input.isTab()) return;
        if (this.window != null) return;

        if (this.commandPreview.isPresent()) {
            String preview = this.commandPreview.get();

            this.completingSuggestions = true;
            this.textField.setText(preview);
            this.textField.setSelectionStart(preview.length());
            this.textField.setSelectionEnd(preview.length());
            this.textField.setSuggestion(null);
            this.commandPreview = Optional.empty();
            this.completingSuggestions = false;

            cir.setReturnValue(true);
        }
    }

    @Inject(method = "setWindowActive", at = @At("TAIL"))
    private void clearPreviewOnClose(boolean active, CallbackInfo ci) {
        if (!active) {
            this.commandPreview = Optional.empty();
            this.textField.setSuggestion(null);
        }
    }
}
