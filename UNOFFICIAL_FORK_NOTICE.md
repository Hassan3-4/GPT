# Unofficial fork notice

This `fabric/1.21.11` branch is an unofficial derivative of
[DragonsPlusMinecraft/CreateDragonsPlus](https://github.com/DragonsPlusMinecraft/CreateDragonsPlus).
It is maintained by YU322142 to provide the library layer required by the
accompanying Create: Enchantment Industry Fabric port. It is not endorsed by or
affiliated with DragonsPlusMinecraft.

The port was modified during July and August 2026. It replaces NeoForge
platform APIs with Fabric equivalents, separates client-only code from the
dedicated server, adapts registration, recipes, fluids, rendering and Create
Fly APIs, and excludes NeoForge-only optional integrations from the Fabric
runtime artifact. Git history and the branch diff are the authoritative record
of changes.

## Licensing and attribution

- The derivative remains licensed under `LGPL-3.0-or-later`; `LICENSE.txt` is
  retained unchanged.
- `LICENSING.txt` identifies material derived from Create. The corresponding
  MIT license and copyright notice remain in `LICENSE-CREATE.txt`.
- Existing source headers, `@CodeReference` annotations, author credits and
  copyright notices must be preserved.
- No upstream trademark rights or endorsement are claimed. Names are used only
  to identify upstream source and compatibility.

When distributing binaries, provide the corresponding source revision and
include `LICENSE.txt`, `LICENSING.txt`, `LICENSE-CREATE.txt`, and this notice.
