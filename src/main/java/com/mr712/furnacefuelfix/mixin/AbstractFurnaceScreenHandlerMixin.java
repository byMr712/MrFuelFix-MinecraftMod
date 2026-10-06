package com.mr712.furnacefuelfix.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractFurnaceMenu.class, priority = 800)
public abstract class AbstractFurnaceScreenHandlerMixin extends AbstractContainerMenu {

    protected AbstractFurnaceScreenHandlerMixin(@Nullable MenuType<?> type, int syncId) {
        super(type, syncId);
    }

    @Shadow
    protected abstract boolean canSmelt(ItemStack itemStack);

    @Shadow
    protected abstract boolean isFuel(ItemStack itemStack);

    /**
     * Fixes vanilla Minecraft behavior where shift-clicking fuel fails if the input slot
     * is already occupied or full, particularly for items that are both smeltable and fuel
     * (logs, wood, planks, sticks, charcoal, or custom smelting recipes).
     */
    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void onQuickMoveStack(Player player, int slotIndex, CallbackInfoReturnable<ItemStack> cir) {
        if (this.slots == null || slotIndex < 3 || slotIndex >= 39 || slotIndex >= this.slots.size()) {
            return;
        }

        Slot slot = this.slots.get(slotIndex);
        if (slot == null || !slot.hasItem()) {
            return;
        }

        ItemStack sourceStack = slot.getItem();
        boolean smeltable = this.canSmelt(sourceStack);
        boolean fuel = this.isFuel(sourceStack);

        if (!smeltable && !fuel) {
            // Not a furnace-related item (e.g. dirt, sword); let vanilla handle inventory/hotbar swap.
            return;
        }

        ItemStack originalCopy = sourceStack.copy();

        // 1. If smeltable, try to insert into the input slot (slot 0)
        if (smeltable) {
            this.moveItemStackTo(sourceStack, 0, 1, false);
        }

        // 2. If it's a fuel, and either it wasn't smeltable OR the input slot couldn't accept all of it,
        // intelligently insert the remainder into the fuel slot (slot 1)
        if (fuel && !sourceStack.isEmpty()) {
            this.moveItemStackTo(sourceStack, 1, 2, false);
        }

        // 3. If neither furnace slot accepted any items (both full or occupied),
        // fallback to standard inventory/hotbar swapping behavior
        if (sourceStack.getCount() == originalCopy.getCount()) {
            if (slotIndex >= 3 && slotIndex < 30) {
                if (!this.moveItemStackTo(sourceStack, 30, 39, false)) {
                    cir.setReturnValue(ItemStack.EMPTY);
                    return;
                }
            } else if (slotIndex >= 30 && slotIndex < 39) {
                if (!this.moveItemStackTo(sourceStack, 3, 30, false)) {
                    cir.setReturnValue(ItemStack.EMPTY);
                    return;
                }
            }
        }

        if (sourceStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (sourceStack.getCount() == originalCopy.getCount()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        slot.onTake(player, sourceStack);
        cir.setReturnValue(originalCopy);
    }
}
