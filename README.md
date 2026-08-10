> [!IMPORTANT]
> This is an **unofficial Fabric 1.21.11 port** maintained by YU322142. It is
> not an official DragonsPlusMinecraft release. See
> [UNOFFICIAL_FORK_NOTICE.md](UNOFFICIAL_FORK_NOTICE.md) for attribution,
> licensing and supported scope.

## Welcome to **Create: Dragons Plus**
A library mod for DragonsPlusMinecraft Create-addons.

## Build this Fabric branch

Java 21 is required. Clone this repository and the accompanying Create Fly fork
as sibling directories, then build them in order:

```text
workspace/
  Create-Fly/
  CreateDragonsPlus/
```

```bash
cd Create-Fly
./gradlew build
cd ../CreateDragonsPlus
./gradlew build
```

Use the `fabric/1.21.11` branch in both repositories. The published source does
not depend on private Maven packages or locally patched binary-only libraries.

## Add Depenency
```groovy
repositories {
    maven { url "https://maven.dragons.plus/releases" } // DragonsPlusMinecraft Maven
    maven { url "https://maven.fallenbreath.me/releases" } // Conditional Mixin
}

dependencies {
    implementation("plus.dragons.createdragonsplus:create-dragons-plus-${minecraft_version}:${create_dragons_plus_version}")
}
```
Note that Create: Dragons Plus has exposed Create and it's dependencies as Gradle's `api` configuration, so you don't need to specify them again in your dependencies block unless you wish to use another version of these artifacts.

## Contribute
Feel free to open a PR to either provide localization or to add another feature! All help is appreciated!
### If you want to help us to translate...
Please find incomplete language file in `src/generated/assets/create_dragon_plus/lang`, and **submit complete language file to`src/translations/assets/create_dragon_plus/lang`**.
