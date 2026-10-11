# Developer & Agent Guidelines — MrFuelFix (1.21.11)

## Версия и стек
- **Minecraft:** 1.21.11
- **Маппинги:** Официальные Mojang Mappings (loom.officialMojangMappings())
- **Поддерживаемые загрузчики:** Fabric и NeoForge
- **Fabric Loader:** 0.19.5
- **Fabric Loom:** 1.18-SNAPSHOT
- **Fabric API:** 0.141.6+1.21.11
- **Java:** 21 (таргет и рантайм Java 21)

## Мультилоадерная архитектура (Fabric + NeoForge)

Мод собирается параллельно под два загрузчика из единого исходного кода:
1. **Единый исходный код на официальных Mojang-маппингах:**
   - Компилирует байткод, напрямую совместимый со средой выполнения NeoForge.
   - Для Fabric плагин Fabric Loom автоматически выполняет ремаппинг байткода из Mojang-маппингов в Intermediary-маппинги при формировании Fabric JAR (`remapJar`).
2. **Метаданные:**
   - **Fabric:** `src/main/resources/fabric.mod.json` (декларирует точку входа и миксины для Fabric).
   - **NeoForge:** `src/main/resources/META-INF/neoforge.mods.toml` (декларирует метаданные мода и подключение миксина `furnacefuelfix.mixins.json` через языковой загрузчик `javafml`).
   - `furnacefuelfix.mixins.json` универсален и используется обоими загрузчиками.
3. **Требования NeoForge к формату версии (Maven ArtifactVersion):**
   - NeoForge валидирует версию мода строго по спецификации Maven (`ArtifactVersion`): версия **обязана начинаться с цифры** (например, `1.1`), иначе загрузчик выбрасывает `InvalidModFileException: Illegal version number specified v1.1` и крашит клиент в `broken mod state`.
   - В `build.gradle` для `META-INF/neoforge.mods.toml` ведущий символ `v` автоматически отсекается (`version.startsWith('v') ? version.substring(1) : version`).
   - При этом в `fabric.mod.json` и в именах файлов JAR остаётся полная версия `v1.1`.
4. **Разделение артефактов в build.gradle:**
   - **remapJar:** Собирает `MrFuelFix-Fabric-1.21.11-byMr712-v1.1.jar` с ремаппингом в Intermediary, исключая `META-INF/neoforge.mods.toml`.
   - **neoforgeJar:** Собирает `MrFuelFix-NeoForge-1.21.11-byMr712-v1.1.jar` с оригинальным Mojang-байткодом, включая `neoforge.mods.toml` и исключая специфичные для Fabric файлы (`fabric.mod.json`, `FurnaceFuelFixMod.class`).
   - Задача `build` автоматически запускает оба таска (`assemble.dependsOn neoforgeJar`), создавая оба JAR за один запуск.

## Особенности реализации миксина
- **Целевой класс инжекции:** `net.minecraft.world.inventory.AbstractFurnaceMenu` (наследует `AbstractContainerMenu`)
- **Переопределяемый метод:** `quickMoveStack(Player player, int slotIndex)`
- **Вспомогательные методы:**
  - `@Shadow protected abstract boolean canSmelt(ItemStack itemStack);`
  - `@Shadow protected abstract boolean isFuel(ItemStack itemStack);`
- **Манипуляции со слотами (`net.minecraft.world.inventory.Slot`):**
  - Проверка содержимого: `slot.hasItem()`, получение: `slot.getItem()`.
  - Установка предмета: `slot.set(ItemStack.EMPTY)`.
  - Обновление состояния: `slot.setChanged()`.
  - Взятие предмета игроком: `slot.onTake(player, sourceStack)`.
  - Перемещение: `this.moveItemStackTo(sourceStack, from, to, fromLast)`.

## Логика фикса
1. При `quickMoveStack` (Shift-клик) в слотах инвентаря игрока (индексы 3..38):
2. Если предмет может плавиться (`canSmelt`) — сначала пытается зайти в слот 0 (плавка).
3. Если предмет также является топливом (`isFuel`) и остался непустым — остаток отправляется в слот 1 (топливо).
4. Если оба слота печи не приняли предмет — выполняется стандартный обмен между инвентарём и хотбаром.

## Команды сборки
- Полная сборка обоих JAR: `./gradlew build`
- Выходные файлы:
  - `build/libs/MrFuelFix-Fabric-1.21.11-byMr712-v1.1.jar` (для Fabric)
  - `build/libs/MrFuelFix-NeoForge-1.21.11-byMr712-v1.1.jar` (для NeoForge)
