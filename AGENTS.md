# Developer & Agent Guidelines — MrFuelFix (26.3)

## Версия и стек
- **Minecraft:** 26.3
- **Маппинги:** Официальные Mojang Mappings (Minecraft 26.x поставляется деобфусцированным, для Fabric Loom используется локальный `empty-mappings.jar`)
- **Fabric Loader:** 0.19.5
- **Fabric Loom:** 1.18-SNAPSHOT
- **Fabric API:** 0.162.0+26.3
- **Java:** 25 (таргет и рантайм Java 25)

## Особенности реализации в данной версии

Начиная с версии Minecraft 26.x произошёл глобальный переход архитектуры на официальные названия пакетов и методов Mojang, а также обновление стандарта Java до 25:
- **Целевой класс инжекции:** `net.minecraft.world.inventory.AbstractFurnaceMenu` (наследует `AbstractContainerMenu`)
- **Переопределяемый метод:** `quickMoveStack(Player player, int slotIndex)` (вместо устаревшего `quickMove`)
- **Вспомогательные методы:**
  - `@Shadow protected abstract boolean canSmelt(ItemStack itemStack);` (вместо `isSmeltable`)
  - `@Shadow protected abstract boolean isFuel(ItemStack itemStack);`
- **Манипуляции со слотами (`net.minecraft.world.inventory.Slot`):**
  - Проверка содержимого: `slot.hasItem()`, получение: `slot.getItem()`.
  - Установка предмета: `slot.set(ItemStack.EMPTY)`.
  - Обновление состояния: `slot.setChanged()` (вместо `markDirty()`).
  - Взятие предмета игроком: `slot.onTake(player, sourceStack)` (вместо `onTakeItem()`).
  - Перемещение: `this.moveItemStackTo(sourceStack, from, to, fromLast)` (вместо `insertItem`).
- **Специфика Fabric Loom:**
  - В `build.gradle` включено `loom { noIntermediateMappings() }`.
  - Используется локальный `empty-mappings.jar` для корректной резолюции конфигурации `mappings`.
  - В `furnacefuelfix.mixins.json` задан уровень `"compatibilityLevel": "JAVA_25"`.

## Логика фикса
1. При `quickMoveStack` (Shift-клик) в слотах инвентаря игрока (индексы 3..38):
2. Если предмет может плавиться (`canSmelt`) — сначала пытается зайти в слот 0 (плавка).
3. Если предмет также является топливом (`isFuel`) и остался непустым (верхний слот полон или занят другим ресурсом) — остаток направляется в слот 1 (топливо).
4. Если оба слота печи не приняли предмет — выполняется обмен между основным инвентарём и хотбаром.

## Команды сборки
- Полная сборка JAR: `./gradlew build`
- Выходной файл: `build/libs/MrFuelFix-Fabric-26.3-byMr712-v1.0.jar`