# Unofficial fork notice

This `fabric/1.21.11` branch is an unofficial derivative of
[DragonsPlusMinecraft/CreateEnchantmentIndustry](https://github.com/DragonsPlusMinecraft/CreateEnchantmentIndustry).
It is maintained by YU322142 for Minecraft 1.21.11, Fabric and Create Fly. It is
not endorsed by or affiliated with DragonsPlusMinecraft.

The port was modified during July and August 2026. It replaces NeoForge APIs
with Fabric equivalents, adapts Create 6 APIs and client/server separation,
ports recipes, data generation, fluids, rendering and machine behaviours, and
includes subsequent fixes for the Blaze Enchanter, Blaze Forger, enchanting
templates, experience cake feeding and lightning conversion. Git history and
the branch diff are the authoritative record of changes.

## Licensing and attribution

- The derivative remains licensed under `LGPL-3.0-or-later`; `LICENSE.txt` is
  retained unchanged.
- Existing source headers, authorship, copyright and attribution notices must
  be preserved.
- Create Fly is a separate dependency. The build also compiles CDP's
  `CreateRecipeBuilders` helper into the artifact; therefore the CDP LGPL,
  Create MIT and CDP attribution notices are bundled in generated JARs.
- No upstream trademark rights or endorsement are claimed. Project names are
  used only to identify upstream source and compatibility.

When distributing binaries, provide the corresponding source revision and
include `LICENSE.txt` and this notice.

## Scope

This branch covers the CEI 2.4.2 core port. The upstream NeoForge-only optional
integration source sets for Apotheosis, Apothic Enchanting, Sable and Touhou
Little Maid are not compiled into or represented as supported by this Fabric
artifact.
