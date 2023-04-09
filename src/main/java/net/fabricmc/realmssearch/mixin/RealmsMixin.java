package net.fabricmc.realmssearch.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.gui.screen.RealmsSelectWorldTemplateScreen;
import net.minecraft.client.realms.util.TextRenderingUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import java.util.*;

import static net.minecraft.client.realms.gui.screen.RealmsScreen.row;

@Mixin (RealmsSelectWorldTemplateScreen.class)
public abstract class RealmsMixin extends Screen {
    @Shadow private RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList templateList;
    @Shadow private ButtonWidget trailerButton;
    @Shadow private ButtonWidget selectButton;
    @Shadow private final RealmsServer.WorldType worldType;
    @Shadow private ButtonWidget publisherButton;
    private ButtonWidget confirmSearchButton;
    @Shadow private boolean hoverWarning;
    @Shadow private String warningURL;
    private ButtonWidget buttonWidget;
    private TextFieldWidget searchBar;
    private Text minigamesString = Text.translatable("");
    private Text minigamesString2 = Text.translatable("Search for minigame(s):");
    @Shadow @Nullable Text tooltip;
    @Shadow @Nullable String currentLink;
    @Shadow @Nullable List<TextRenderingUtils.Line> noTemplatesMessage;
    @Shadow @Nullable private Text[] warning;
    @Shadow boolean displayWarning;

    private boolean hideBox;
    private boolean isItAuthorSearch;
    private boolean startChecking;
    private boolean hasSearched;
    private int updatedCurrentSize;
    @Shadow int clicks;

    protected RealmsMixin(Text title, RealmsServer.WorldType worldType) {
        super(title);
        this.worldType = worldType;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void init() {
        this.searchBar = new TextFieldWidget(this.textRenderer, this.width/2-25, 6, 150, 20, Text.translatable("selectWorld.search"));
        this.searchBar.setMaxLength(50);
        this.searchBar.setDrawsBackground(true);
        this.searchBar.setVisible(true);
        this.searchBar.setEditableColor(0xFFFFFF);
        this.addSelectableChild(this.searchBar);
        this.templateList = ((RealmsSelectWorldTemplateScreen)(Object)this).new WorldTemplateObjectSelectionList(this.templateList.getValues());
        this.trailerButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.template.button.trailer"), (button) -> {
            this.onTrailer();
        }).dimensions(this.width / 2 - 206, this.height - 32, 100, 20).build());
        this.selectButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.template.button.select"), (button) -> {
            this.selectTemplate();
        }).dimensions(this.width / 2 - 100, this.height - 32, 100, 20).build());
        Text text = this.worldType == RealmsServer.WorldType.MINIGAME ? ScreenTexts.CANCEL : ScreenTexts.BACK;
        this.buttonWidget = ButtonWidget.builder(text, (button) -> {
            this.close();
        }).dimensions(this.width / 2 + 6, this.height - 32, 100, 20).build();
        this.addDrawableChild(this.buttonWidget);
        this.publisherButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("mco.template.button.publisher"), (button) -> {
            this.onPublish();
        }).dimensions(this.width / 2 + 112, this.height - 32, 100, 20).build());
        this.confirmSearchButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("\u2714"), (button) -> {
            this.onSearch();
        }).dimensions(this.width/2+132, 5, 22, 22).build());
        this.confirmSearchButton.active = false;
        this.selectButton.active = false;
        this.trailerButton.visible = false;
        this.publisherButton.visible = false;
        this.addSelectableChild(this.templateList);
        this.focusOn(this.templateList);
        if(hideBox){
            this.searchBar.setVisible(false);
            this.searchBar.setEditable(false);
            this.confirmSearchButton.setX(-1000);
            this.confirmSearchButton.setY(-1000);
            Text cancelSearchText = Text.translatable("Cancel Search");
            this.buttonWidget.setMessage(cancelSearchText);
        }
        this.startChecking=false;
        this.hasSearched=false;
        this.isItAuthorSearch=false;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void tick() {
        super.tick();
        --this.clicks;
        if (this.clicks < 0) {
            this.clicks = 0;
        }
        if(this.templateList.isFocused() && !startChecking){
            startChecking=true;
        }
        try{
            if (startChecking && !hasSearched) {
                if (this.templateList.getItem(this.templateList.children().size() - 1).id.equalsIgnoreCase("24") && !(this.searchBar.getText().equalsIgnoreCase(""))) {
                    this.confirmSearchButton.active = true;
                }
                if (this.searchBar.getText().equalsIgnoreCase("")) {
                    this.confirmSearchButton.active = false;
                }
            }
        }catch (IndexOutOfBoundsException e){
        }
    }

    public void onSearch(){
        this.startChecking=false;
        this.hasSearched=true;
        String query = this.searchBar.getText();
        int minigamesFound = 0;
        if(!(query.equalsIgnoreCase(""))){
            int currentSize = this.templateList.children().size();
            if(!(query.startsWith("@"))){
                for (int i = currentSize - 1; i >= 0; i--) {
                    if (!(this.templateList.getItem(i).name.toLowerCase().contains(query.toLowerCase()))) {
                        this.templateList.children().remove(i);
                        currentSize--;
                        this.updatedCurrentSize=currentSize;
                    }
                }
            }else{
                this.isItAuthorSearch=true;
                query=query.substring(1);
                for (int i = currentSize - 1; i >= 0; i--) {
                    if (!(this.templateList.getItem(i).author.toLowerCase().contains(query.toLowerCase()))) {
                        this.templateList.children().remove(i);
                        currentSize--;
                        this.updatedCurrentSize=currentSize;
                    }
                }
            }
        }
        this.searchBar.setVisible(false);
        this.searchBar.setEditable(false);
        this.confirmSearchButton.setX(-1000);
        this.confirmSearchButton.setY(-1000);
        this.hideBox=true;
        if((!(query.equalsIgnoreCase("")))){
            if(!isItAuthorSearch) {
                if (minigamesFound > 0) {
                    this.minigamesString = Text.translatable(this.updatedCurrentSize + " minigame(s) found matching search: " + '"' + query + '"');
                } else {
                    this.minigamesString = Text.translatable(this.updatedCurrentSize + " minigame(s) found matching search: " + '"' + query + '"');
                }
            }else{
                if (minigamesFound > 0) {
                    this.minigamesString = Text.translatable(this.updatedCurrentSize + " minigame(s) found matching author search: " + '"' + query + '"');
                } else {
                    this.minigamesString = Text.translatable(this.updatedCurrentSize + " minigame(s) found matching author search: " + '"' + query + '"');
                }
            }
            this.minigamesString2 = Text.translatable("");
            Text cancelSearchText = Text.translatable("Cancel Search");
            this.buttonWidget.setMessage(cancelSearchText);
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.hoverWarning && this.warningURL != null) {
            Util.getOperatingSystem().open("https://www.minecraft.net/realms/adventure-maps-in-1-9%22");
            return true;
        } else {
            if(!hasSearched){
                this.startChecking=true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.tooltip = null;
        this.currentLink = null;
        this.hoverWarning = false;
        this.renderBackground(matrices);
        this.templateList.render(matrices, mouseX, mouseY, delta);
        if (this.noTemplatesMessage != null) {
            this.renderMessages(matrices, mouseX, mouseY, this.noTemplatesMessage);
        }

        drawCenteredTextWithShadow(matrices, this.textRenderer, this.minigamesString, this.width / 2, 12, 16777215);
        drawTextWithShadow(matrices, this.textRenderer, this.minigamesString2, this.width/2-150, 12, 16777215);
        if (this.displayWarning) {
            Text[] texts = this.warning;

            int i;
            int k;
            for(i = 0; i < texts.length; ++i) {
                int j = this.textRenderer.getWidth(texts[i]);
                k = this.width / 2 - j / 2;
                int l = row(-1 + i);
                if (mouseX >= k && mouseX <= k + j && mouseY >= l) {
                    Objects.requireNonNull(this.textRenderer);
                    if (mouseY <= l + 9) {
                        this.hoverWarning = true;
                    }
                }
            }

            for(i = 0; i < texts.length; ++i) {
                Text text = texts[i];
                k = 10526880;
                if (this.warningURL != null) {
                    if (this.hoverWarning) {
                        k = 7107012;
                        text = ((Text)text).copy().formatted(Formatting.STRIKETHROUGH);
                    } else {
                        k = 3368635;
                    }
                }

                drawCenteredTextWithShadow(matrices, this.textRenderer, (Text)text, this.width / 2, row(-1 + i), k);
            }
            this.hideBox=false;
        }

        super.render(matrices, mouseX, mouseY, delta);
        this.renderMousehoverTooltip(matrices, this.tooltip, mouseX, mouseY);
        this.searchBar.render(matrices, mouseX, mouseY, delta);
    }

    @Shadow public abstract void renderMousehoverTooltip(MatrixStack matrices, Text tooltip, int mouseX, int mouseY);
    @Shadow public abstract void renderMessages(MatrixStack matrices, int mouseX, int mouseY, List<TextRenderingUtils.Line> noTemplatesMessage);
    @Shadow public abstract void close();
    @Shadow public abstract void onPublish();
    @Shadow public abstract void onTrailer();
    @Shadow public abstract void selectTemplate();
}

