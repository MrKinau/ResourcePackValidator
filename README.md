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

### TextureLimitMipLevelValidator
Checks every PNG file, that will be loaded into the texture atlas, if it limits the mipmap levels. If the mip level drops below 4, the game displays certain textures with lower quality (e.g. leaves).

### TextureIsNotCorruptedValidator
Checks every PNG file, that is located in any resource pack directory, if the texture is an actual image.

### FontTextureExistsValidator
Checks if the texture file referenced in the font provider exists.

### FontCharacterUsageValidator
Checks if a font file defines a character multiple times, which overrides the character.

### ModelRequiresOverlayOverrideValidator
Checks if a model json, which is present in the default pack, is also present in a specified overlay. This Validator requires some additional configuration to run and may be used to make sure every model in a specified path is overridden in a specific overlay (e.g. to fix certain models in certain game versions).


## Commandline Arguments
- `-help` Show all available commandline arguments
- `--resourcepack <directory or zip>`, `-rp <directory or zip>` Specifies the path of the resource pack to validate. By default, it assumes the resource pack is located at `./resourcepack` or `./resourcepack.zip` (if `./resourcepack` does not exist)
- `--verbose`, `-v` Enables verbose log output
- `-config` Specifies the path to the config file. By default, it assumes it is located at `./config.json`
- `-report <file>` Specifies the path the report file. By default, it does not generate a XML report

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
    },
    "TextureIsNotCorruptedValidator": {
      "enabled": true,
      "logLevel": "ERROR",
      "ignore": []
    },
    "FontTextureExistsValidator": {
      "enabled": true,
      "logLevel": "ERROR",
      "ignore": []
    },
    "FontCharacterUsageValidator": {
      "enabled": true,
      "logLevel": "ERROR",
      "ignore": []
    },
    "ModelRequiresOverlayOverrideValidator": {
      "enabled": false,
      "logLevel": "ERROR",
      "required": []
    }
  }
}
```
Default values do not need to be specified in the config.

### Ignore List
Every Validator supports ignore lists and will skip every ignored file.
Ignore lists can contain regex and shell glob matchers as seen in the example.

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

### Additional Data
Some validators may require additional data in order to work.

#### ModelRequiresOverlayOverrideValidator
```json
{
  "validators": {
    "ModelRequiresOverlayOverrideValidator": {
      "enabled": true,
      "required": [
        {
          "path": "glob:**/cosmetics/back/**",
          "overlays": [ "1_20_2" ]
        }
      ]
    }
  }
}
```

## Ideas
### Extend Unused Files Detection
Extend detection, to find models which do not have an override or override a vanilla item. 
Also check all textures if they are used by a model which already is unused?

### Parent Validator
Validate parent entries: Does the parent model exist

### Animated texture frames missing / too many sprite frames
Checks if an animated texture has too many / too few frames.

## Discord
To follow the project, get support or request features or bugs you can join my Discord: https://discord.gg/xHpCDYf

## Contribution
You are free to create a fork, or a pull request to participate. You also can report bugs or request a new feature in the [issues](https://github.com/MrKinau/FishingBot/issues) tab or on my [Discord](https://discord.gg/xHpCDYf) (I will answer them as soon as possible)