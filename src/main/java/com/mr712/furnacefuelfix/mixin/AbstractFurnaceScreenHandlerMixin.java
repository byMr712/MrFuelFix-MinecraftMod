package com.mr712.furnacefuelfix.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractFurnaceScreenHandler.class, priority = 800)
public abstract class AbstractFurnaceScreenHandlerMixin extends ScreenHandler {

    protected AbstractFurnaceScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Shadow
    protected abstract boolean isSmeltable(ItemStack itemStack);

    @Shadow
    protected abstract boolean isFuel(ItemStack itemStack);

    /**
     * Fixes vanilla Minecraft behavior where shift-clicking fuel fails if the input slot
     * is already occupied or full, particularly for items that are both smeltable and fuel
     * (logs, wood, planks, sticks, charcoal, or custom smelting recipes).
     */
    @Inject(method = "quickMove", at = @At("HEAD"), cancellable = true)
    private void onQuickMove(PlayerEntity player, int slotIndex, CallbackInfoReturnable<ItemStack> cir) {
        if (this.slots == null || slotIndex < 3 || slotIndex >= 39 || slotIndex >= this.slots.size()) {
            return;
        }

        Slot slot = this.slots.get(slotIndex);
        if (slot == null || !slot.hasStack()) {
            return;
        }

        ItemStack sourceStack = slot.getStack();
        boolean smeltable = this.isSmeltable(sourceStack);
        boolean fuel = this.isFuel(sourceStack);

        if (!smeltable && !fuel) {
            // Not a furnace-related item (e.g. dirt, sword); let vanilla handle inventory/hotbar swap.
            return;
        }

        ItemStack originalCopy = sourceStack.copy();

        // 1. If smeltable, try to insert into the input slot (slot 0)
        if (smeltable) {
            this.insertItem(sourceStack, 0, 1, false);
        }

        // 2. If it's a fuel, and either it wasn't smeltable OR the input slot couldn't accept all of it,
        // intelligently insert the remainder into the fuel slot (slot 1)
        if (fuel && !sourceStack.isEmpty()) {
            this.insertItem(sourceStack, 1, 2, false);
        }

        // 3. If neither furnace slot accepted any items (both full or occupied),
        // fallback to standard inventory/hotbar swapping behavior
        if (sourceStack.getCount() == originalCopy.getCount()) {
            if (slotIndex >= 3 && slotIndex < 30) {
                if (!this.insertItem(sourceStack, 30, 39, false)) {
                    cir.setReturnValue(ItemStack.EMPTY);
                    return;
                }
            } else if (slotIndex >= 30 && slotIndex < 39) {
                if (!this.insertItem(sourceStack, 3, 30, false)) {
                    cir.setReturnValue(ItemStack.EMPTY);
                    return;
                }
            }
        }

        if (sourceStack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        if (sourceStack.getCount() == originalCopy.getCount()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        slot.onTakeItem(player, sourceStack);
        cir.setReturnValue(originalCopy);
    }
}
