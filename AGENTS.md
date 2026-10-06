# Developer & Agent Guidelines — MrFuelFix (1.21.5)

## Версия и стек
- **Minecraft:** 1.21.5
- **Yarn Mappings:** 1.21.5+build.1
- **Fabric Loader:** 0.19.5
- **Fabric Loom:** 1.18-SNAPSHOT
- **Fabric API:** 0.128.2+1.21.5
- **Java:** 21 (сборка на JDK 25 с elease = 21)

## Особенности реализации в данной версии

В Minecraft 1.21.5 используются классические Yarn-маппинги и архитектура интерфейсов 
et.minecraft.screen.*:
- **Целевой класс инжекции:** 
et.minecraft.screen.AbstractFurnaceScreenHandler
- **Переопределяемый метод:** quickMove(PlayerEntity player, int slotIndex)
- **Вспомогательные методы:**
  - @Shadow protected abstract boolean isSmeltable(ItemStack itemStack);
  - @Shadow protected abstract boolean isFuel(ItemStack itemStack);
- **Манипуляции со слотами (
et.minecraft.screen.slot.Slot):**
  - Проверка содержимого: slot.hasStack(), получение: slot.getStack().
  - Установка предмета: slot.setStack(ItemStack.EMPTY).
  - Обновление состояния: slot.markDirty().
  - Взятие предмета игроком: slot.onTakeItem(player, sourceStack).
  - Перемещение: 	his.insertItem(sourceStack, from, to, fromLast).

## Логика фикса
1. При quickMove (Shift-клик) в слотах инвентаря игрока (индексы 3..38):
2. Если предмет переплавляемый (isSmeltable) — сначала предпринимается попытка поместить его в слот 0 (плавка).
3. Если предмет является топливом (isFuel) и остался непустым (верхний слот полон или занят другим ресурсом) — остаток умно отправляется в слот 1 (топливо).
4. Если оба слота печи не приняли предмет — выполняется стандартный обмен между основным инвентарём и хотбаром.

## Команды сборки
- Полная сборка JAR: `./gradlew build`
- Выходной файл: `build/libs/MrFuelFix-Fabric-1.21.5-byMr712-v1.1.jar`