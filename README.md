> [!IMPORTANT]
> This is an **unofficial Fabric 1.21.11 port** maintained by YU322142. It is
> not an official DragonsPlusMinecraft release. See
> [UNOFFICIAL_FORK_NOTICE.md](UNOFFICIAL_FORK_NOTICE.md) for attribution,
> licensing and the exact supported scope.

## Welcome to **Create Enchantment Industry**
A mod offering more tools and methods to handle experience & enchantment in Create.

## Build this Fabric branch

Java 21 is required. Clone all three public forks as sibling directories and use
their `fabric/1.21.11` branches:

```text
workspace/
  Create-Fly/
  CreateDragonsPlus/
  CreateEnchantmentIndustry/
```

Build in dependency order:

```bash
cd Create-Fly
./gradlew build
cd ../CreateDragonsPlus
./gradlew build
cd ../CreateEnchantmentIndustry
./gradlew build
```

The runtime also requires Fabric API and Forge Config API Port. JEI support is
optional.

## Contribute
Feel free to open a PR to either translate the mod or to add another feature! All help is appreciated!
### If you want to help us to translate...
Please find incomplete language file in `src/generated/assets/create_enchantment_industry/lang`, and **submit complete language file to`src/translations/assets/create_enchantment_industry/lang`**.

## Download
[<img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/curseforge_vector.svg">](https://www.curseforge.com/minecraft/mc-mods/create-enchantment-industry) [<img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg">](https://modrinth.com/mod/create-enchantment-industry)
