package net.starcclans.apprentice.apprenticemod.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class CustomVillagerScreen extends Screen {

    public CustomVillagerScreen(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
