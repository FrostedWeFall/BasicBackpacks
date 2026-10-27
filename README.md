# BasicBackpacks

A personal backpack plugin originally created by **FrostedWeFall** in 2017 and modernised as a portfolio project.

## Features

- A separate 27-slot backpack for each player.
- Backpack contents persist when players leave and when the server shuts down.
- Periodic saving while players are online.
- `/backpack` and `/bp` open a player's backpack.
- `/bbp` displays plugin information.
- `/bbhelp` displays the available commands.
- Best-effort migration of the original `BasicBackpacks.<uuid>.<letter>.type/amount` configuration format.

## Compatibility

- Minecraft Java Edition **26.3**
- Paper server API
- Java **25**

This is a new build target; test it on a staging server before installing it on a live server. Older Minecraft versions will require a matching API/build target.

## Build

Install JDK 25 and Maven, then run from this directory:

```bash
mvn clean package
```

The plugin JAR will be created at:

```text
target/BasicBackpacks-1.0.0.jar
```

Copy that JAR into your Paper server's `plugins` folder and restart the server.

## Permissions

| Permission | Default | Description |
|---|---|---|
| `basicbackpacks.use` | Everyone | Allows use of `/backpack` and `/bp` |

## Commands

| Command | Description |
|---|---|
| `/backpack` | Open your personal backpack |
| `/bp` | Alias for `/backpack` |
| `/bbp` | Plugin information |
| `/bbhelp` | Help menu |

## Data and migration notes

Player contents are stored in `plugins/BasicBackpacks/config.yml` under their UUID. ItemStacks are stored using Bukkit/Paper's configuration serialization so metadata such as enchantments and display names can be retained.

The migration code can read the original project's basic material and amount fields. The old code's durability field was incomplete in the supplied source, so legacy durability and exact original slot positions cannot be guaranteed during migration. Back up your old server files before testing any migration.

## Development status

This modernised source has been prepared for review. It has **not yet been compiled against Paper or tested on a live server** in this environment. Test with a backup/staging server before release.
