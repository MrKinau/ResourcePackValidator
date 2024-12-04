# Config
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
    "ModelParentValidator": {
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

## Ignore List
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

## Additional Data
Some validators may require additional data in order to work.

### ModelRequiresOverlayOverrideValidator
```json
{
  "validators": {
    "ModelRequiresOverlayOverrideValidator": {
      "enabled": true,
      "required": [
        {
          "path": "glob:**/minecraft/models/equipment/**",
          "overlays": [ "1_21_4" ],
          "replacements": [
            {
              "path": "/models/equipment/",
              "replacement": "/equipment/"
            }
          ]
        }
      ]
    }
  }
}
```