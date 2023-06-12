# Resource Pack Validator
[![GitHub issues](https://img.shields.io/github/issues/MrKinau/ResourcePackValidator)](https://github.com/MrKinau/ResourcePackValidator/issues)
[![License](https://img.shields.io/github/license/MrKinau/ResourcePackValidator)](https://github.com/MrKinau/ResourcePackValidator/blob/master/LICENSE)
[![Discord](https://img.shields.io/discord/550764567282712583?logo=discord)](https://discord.gg/xHpCDYf)

A commandline tool to validate a Minecraft Java Edition resource pack. It runs several validations, that normally run while loading the resource pack in the vanilla client as well as some extra validations to identify issues.

## Current Validators
### AnyNamespacePresentValidator
Checks if the resource pack has a namespace folder (e.g. assets/**minecraft**). Although it's not required to have a namespace folder, it's most likely what you want.

### ModelIsJsonObjectValidator
Checks if all json files located in a namespaces models directory are JsonObjects. This is a basic check, which should never fail, but it's a requirement for all other model based checks.

### ModelHasAnyTextureValidator
Checks if a model has at least one texture assigned to it. This check does not fail if the model has an existing parent model.

### ModelTexturesExistsValidator
Checks if the texture files referenced in the model exists.

### ModelOverridesExistsValidator
Checks if item overrides are correct and the referenced model exists.

### UnusedFileValidator
Checks every file if it has been referenced in any json file. Most files need to be referenced somewhere, but this check may falsely detect files, which are in use. This will not flag for vanilla texture/model overrides. Files can be ignored with the `ignore` option using shell globs or regex.

## Commandline Arguments
- `-help` Show all available commandline arguments
- `-resourcepack <directory>`, `-rp <directory>` Specifies the path of the resource pack to validate. By default, it assumes the resource pack is located at `./resourcepack`
- `-verbose`, `-v` Enables verbose log output
- `-config` Specifies the path to the config file. By default, it assumes it is located at `./config.json`

## Config
All Validators can be configured with a configuration file, which is read from `./config.json`, but the location can be changed with a commandline argument.
This is the default config:
```json
{
  "validators": {
    "AnyNamespacePresentValidator": {
      "enabled": true,
      "logLevel": "WARN"
    },
    "ModelIsJsonObjectValidator": {
      "enabled": true,
      "logLevel": "ERROR",
      "ignore": []
    },
    "ModelHasAnyTextureValidator": {
      "enabled": true,
      "logLevel": "WARN",
      "ignore": []
    },
    "ModelTexturesExistsValidator": {
      "enabled": true,
      "logLevel": "ERROR",
      "ignore": []
    },
    "ModelOverridesExistsValidator": {
      "enabled": true,
      "logLevel": "ERROR",
      "ignore": []
    },
    "UnusedFileValidator": {
      "enabled": true,
      "logLevel": "ERROR",
      "ignore": []
    }
  }
}
```
Default values do not need to be specified in the config.
An example containing a custom ignore list looks like this:
```json
{
  "validators": {
    "UnusedFileValidator": {
      "ignore": [
        "glob:**/lang/**",
        "glob:**/font/**",
        "glob:**/shaders/**",
        "glob:**_e.png",
        "regex:.*\\.mcmeta$"
      ]
    }
  }
}
```

## Ideas
### Mipmap level dropping atlas validation
Check if the mipmap level would drop with the current atlas configuration (would be nice to have, but quite complicated to implement)

### Animated texture frames missing / too many sprite frames
Checks if an animated texture has too many / too few frames.

## Discord
To follow the project, get support or request features or bugs you can join my Discord: https://discord.gg/xHpCDYf

## Contribution
You are free to create a fork, or a pull request to participate. You also can report bugs or request a new feature in the [issues](https://github.com/MrKinau/FishingBot/issues) tab or on my [Discord](https://discord.gg/xHpCDYf) (I will answer them as soon as possible)